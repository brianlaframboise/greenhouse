package kappamaki.execute;

import static kappamaki.util.Utils.joinPaths;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import kappamaki.extract.Extractor;
import kappamaki.index.Index;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

import com.google.common.base.Joiner;

/**
 * Executes Features, Scenarios, Scenario Outlines, and individul Scenario
 * Outline Examples by:
 * 
 * <ol>
 * <li>Creating a new task id for the cucumber execution</li>
 * <li>Creating a kappamaki/TASK_ID directory in the temp directory</li>
 * <li>Copying the scenario's feature file to that directory while tagging the
 * specific scenario with @kappamaki</li>
 * <li>Scheduling a Callable task to execute a Maven Process against a cuke4duke
 * project to run the (possibly filtered) feature file, executing only scenarios
 * tagged @kappamaki</li>
 * </ol>
 * 
 * The output of the process execution may be redirected to a provided file.
 * 
 * The cuke4duke project must accept a parameter "cucumber.tagsArg" that will
 * pass the cucumber tags argument (ex: "--tags=@tagName") through to the
 * underlying Cucumber execution.
 */
public class ProcessExecutor implements ScenarioExecutor {

    private final Extractor extractor;
    private final File projectRoot;

    private String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";
    private String phase = "integration-test";

    private final ExecutorService executorService = Executors.newScheduledThreadPool(8);
    private final AtomicInteger taskIds = new AtomicInteger(0);
    private final Map<Integer, CucumberTask> tasks = new ConcurrentHashMap<Integer, CucumberTask>();

    /**
     * Creates a new ScenarioExecutor. By default output is inherited from the
     * current Process, which probably means output is directed to standard out.
     * 
     * @param index An Index containing the scenario to run
     * @param projectRoot The root directory of a cuke4duke project
     */
    public ProcessExecutor(Index index, File projectRoot) {
        extractor = new Extractor(index);
        this.projectRoot = projectRoot;
    }

    @Override
    public int execute(IndexedFeature feature) {
        String gherkin = extractor.extract(feature);
        return execute(gherkin);
    }

    @Override
    public int execute(IndexedScenario scenario) {
        String gherkin = extractor.extract(scenario);
        return execute(gherkin);
    }

    @Override
    public int executeExample(IndexedScenario outline, int line) {
        String gherkin = extractor.extract(outline, line);
        return execute(gherkin);
    }

    @Override
    public int execute(String gherkin) {
        int taskId = taskIds.getAndIncrement();
        File tempDir = makeTempDir(taskId);
        copyGherkin(tempDir, gherkin);
        taskId = executeScenarios(tempDir, taskId);
        return taskId;
    }

    private void copyGherkin(File tempDir, String gherkin) {
        try {
            File featureFile = joinPaths(tempDir.getPath(), "gherkin.feature");
            Writer tempFile = new FileWriter(featureFile);
            tempFile.write(gherkin);
            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy gherkin", e);
        }
    }

    private int executeScenarios(File tempDir, final int taskId) {
        String features = "-Dcucumber.features=\"" + tempDir.getAbsolutePath() + "\"";
        //String tags = "-Dcucumber.tagsArg=\"--tags=@kappamaki\"";
        final File output = joinPaths(Utils.TEMP_DIR, "kappamaki", Integer.toString(taskId), "output");
        System.out.println("Executing: " + Joiner.on(' ').join(projectRoot, mvn, features, ">", output.getAbsolutePath()));
        try {
            final ProcessBuilder builder = new ProcessBuilder();
            builder.directory(projectRoot);
            builder.command(mvn, phase, features, ">", output.getAbsolutePath());

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
            File output = tasks.get(taskId).getOutput();
            delete(output.getParentFile());
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

    private File makeTempDir(int taskId) {
        File tempScenarioDir = Utils.tempFile("kappamaki", Integer.toString(taskId));
        if (!tempScenarioDir.mkdirs()) {
            throw new RuntimeException("Could not create temp directory: " + tempScenarioDir.getAbsolutePath());
        }
        return tempScenarioDir;
    }

    private void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        System.out.println("Deleting: " + f);
        if (!f.delete()) {
            throw new RuntimeException("Could not delete " + f.getPath());
        }
    }

    /**
     * Sets the Maven executable name to use. By default this is "mvn.bat" on
     * Windows and "mvn" on all other platforms, which therefore assumes Maven
     * is in the path. If that is not the case, the absolute path to the Maven
     * executable can be provided here.
     * 
     * @param mvn The maven executable path/name
     */
    public void setMvn(String mvn) {
        this.mvn = mvn;
    }

    /**
     * Sets the Maven phase to execute. This is "integration-test" by default.
     * 
     * @param phase the new Maven phase to execute
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

}
