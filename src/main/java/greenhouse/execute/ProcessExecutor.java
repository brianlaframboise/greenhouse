package greenhouse.execute;

import static greenhouse.util.Utils.file;
import gherkin.formatter.Formatter;
import gherkin.parser.Parser;
import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;
import greenhouse.project.Project.Execution;
import greenhouse.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Executes Features, Scenarios, Scenario Outlines, and individul Scenario
 * Outline Examples by:
 * 
 * <ol>
 * <li>Creating a new task id for the cucumber execution</li>
 * <li>Creating a greenhouse/TASK_ID directory in the temp directory</li>
 * <li>Copying the scenario's feature file to that directory while tagging the
 * specific scenario with @greenhouse</li>
 * <li>Scheduling a Callable task to execute a Maven Process against a cuke4duke
 * project to run the (possibly filtered) feature file, executing only scenarios
 * tagged @greenhouse</li>
 * </ol>
 * 
 * The output of the process execution may be redirected to a provided file.
 * 
 * The cuke4duke project must accept a parameter "cucumber.tagsArg" that will
 * pass the cucumber tags argument (ex: "--tags=@tagName") through to the
 * underlying Cucumber execution.
 */
public class ProcessExecutor implements ScenarioExecutor {

    private final ExecutorService executorService = Executors.newScheduledThreadPool(8);
    private final Map<Integer, CucumberTask> tasks = new ConcurrentHashMap<Integer, CucumberTask>();

    /**
     * Creates a new ScenarioExecutor. By default output is inherited from the
     * current Process, which probably means output is directed to standard out.
     * 
     */
    public ProcessExecutor() {
    }

    @Override
    public int execute(Project project, IndexedFeature feature) {
        return execute(project, feature, null, true);
    }

    @Override
    public int execute(Project project, String gherkin) {
        return execute(project, null, gherkin, false);
    }

    private int execute(Project project, IndexedFeature feature, String gherkin, boolean useFeature) {
        Execution execution = project.nextExecution();
        if (useFeature) {
            copyFeature(project, execution.getDirectory(), feature);
        } else {
            copyGherkin(project, execution.getDirectory(), gherkin);
        }
        return executeScenarios(project, execution);
    }

    private void copyGherkin(Project project, File tempDir, String gherkin) {
        try {
            // Setup tagged destination file
            File featureFile = file(tempDir.getPath(), "gherkin.feature");
            Writer tempFile = new FileWriter(featureFile);

            // Parse to copy source into destination
            Parser parser = new Parser(new GreenhouseTagger(tempFile));
            System.out.println("Copying raw gherkin into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy gherkin", e);
        }
    }

    private void copyFeature(Project project, File tempDir, IndexedFeature feature) {
        try {
            // Setup tagged destination file
            File featureFile = tempFeatureFile(project, tempDir, feature);
            Writer tempFile = new FileWriter(featureFile);

            // Load source file
            String gherkin = Utils.readContents(feature.getUri());

            // Parse to copy source into destination
            Parser parser = new Parser(new GreenhouseTagger(tempFile));
            System.out.println("Copying " + feature.getUri() + " into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy scenarios", e);
        }
    }

    private File tempFeatureFile(Project project, File tempDir, IndexedFeature feature) {
        String root = project.index().getFeaturesRoot().getAbsolutePath();
        String subPath = feature.getUri().substring(root.length() + 1);
        return file(tempDir.getPath(), subPath);
    }

    @Override
    public int execute(Project project, IndexedScenario scenario) {
        return executeWithLine(project, scenario, -1);
    }

    @Override
    public int executeExample(Project project, IndexedScenario outline, int line) {
        return executeWithLine(project, outline, line);
    }

    private int executeWithLine(Project project, IndexedScenario scenario, int line) {
        Execution exeuction = project.nextExecution();
        copyScenarios(project, exeuction.getDirectory(), scenario, line);
        return executeScenarios(project, exeuction);
    }

    private void copyScenarios(Project project, File tempDir, IndexedScenario scenario, int line) {
        try {
            // Setup tagged destination file
            IndexedFeature feature = project.index().findByScenario(scenario);
            File featureFile = tempFeatureFile(project, tempDir, feature);
            Files.createParentDirs(featureFile);
            Writer tempFile = new FileWriter(featureFile);

            // Load source file
            String gherkin = Utils.readContents(feature.getUri());

            // Parse and tag source into destination
            Formatter formatter = new ExampleFilterer(new ScenarioTagger(scenario, tempFile), line);
            Parser parser = new Parser(formatter);
            System.out.println("Tagging " + feature.getUri() + " into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy scenarios", e);
        }
    }

    private int executeScenarios(Project project, Execution execution) {
        String features = "-Dcucumber.features=\"" + execution.getDirectory().getAbsolutePath() + "\"";
        String tags = "-Dcucumber.tagsArg=\"--tags=@greenhouse\"";
        final File output = file(execution.getDirectory().getAbsolutePath(), "output.txt");
        try {
            ArrayList<String> argsList = Lists.newArrayList(Splitter.on(' ').split(project.getCommand()));
            argsList.addAll(Lists.newArrayList(features, tags, ">", output.getAbsolutePath()));
            final ProcessBuilder builder = Utils.mavenProcess(project.getFiles(), argsList);
            System.out.println("Executing: " + Joiner.on(' ').join(builder.command()));
            final int taskId = execution.getTaskId();
            Future<String> submittedTask = executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    System.out.println("Task " + taskId + " running...");
                    builder.start().waitFor();
                    String gherkin = Utils.readContents(output.getAbsolutePath());
                    System.out.println("Task " + taskId + " complete");
                    return gherkin;
                }
            });
            tasks.put(taskId, new CucumberTask(output, submittedTask));
            return taskId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getOutput(int taskId) {
        try {
            Future<String> gherkinFuture = tasks.get(taskId).getResult();
            String gherkin = gherkinFuture.get();
            tasks.remove(taskId);
            return gherkin;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get resulting gherkin string", e);
        }
    }

    @Override
    public String getPartialOutput(int taskId) {
        CucumberTask task = tasks.get(taskId);
        File outputFile = task.getOutput();
        return Utils.readContents(outputFile.getAbsolutePath());
    }

    @Override
    public boolean isComplete(int taskId) {
        return tasks.get(taskId).getResult().isDone();
    }

}
