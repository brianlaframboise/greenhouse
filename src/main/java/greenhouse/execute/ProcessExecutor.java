package greenhouse.execute;

import static greenhouse.util.Utils.file;
import gherkin.formatter.Formatter;
import gherkin.parser.Parser;
import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;
import greenhouse.util.DirectoryFilter;
import greenhouse.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * TODO: Overhaul this documentation.
 * 
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

    /** Arbitrarily chosen small number of threads. */
    private static final int NUM_THREADS = 4;

    /**
     * The ExecutorService that executes the tasks associated with each
     * Execution.
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    /** Maps an ExecutionKey to its Execution. */
    private final ConcurrentHashMap<ExecutionKey, Execution> tasks = new ConcurrentHashMap<ExecutionKey, Execution>(16, 0.75f, NUM_THREADS);
    /** Maps a project key to that project's next execution number. */
    private final Map<String, AtomicInteger> executionIds = new HashMap<String, AtomicInteger>();

    /**
     * Resets and reloads the stored project state.
     * 
     * @param repo
     * @param projectsDir
     */
    public void reset(ProjectRepository repo, File projectsDir) {
        tasks.clear();
        executionIds.clear();

        DirectoryFilter directoryFilter = new DirectoryFilter();
        for (File file : projectsDir.listFiles(directoryFilter)) {
            String projectKey = file.getName();
            File executionsDirectory = Utils.file(file.getAbsolutePath(), "executions");
            if (executionsDirectory.exists()) {
                Project project = repo.getProjects().get(projectKey);
                int max = 1;
                for (File executionDir : executionsDirectory.listFiles(directoryFilter)) {
                    int executionNumber = Integer.valueOf(executionDir.getName());
                    Properties props = Utils.load(executionDir, "execution.properties");
                    Execution execution = new Execution(project, executionNumber, ExecutionType.valueOf(props.getProperty("type")),
                            props.getProperty("details"));

                    execution.setCommand(props.getProperty("command"));
                    execution.setEnd(Long.valueOf(props.getProperty("end", "0")));
                    execution.setStart(Long.valueOf(props.getProperty("start", "0")));
                    execution.setState(ExecutionState.COMPLETE);

                    tasks.put(execution.getKey(), execution);
                    max = Math.max(max, executionNumber);
                }
                AtomicInteger nextId = getNextId(project);
                nextId.set(max + 1);
                executionIds.put(projectKey, nextId);
            }
        }
    }

    private Execution nextExecution(Project project, ExecutionType type, String details) {
        int id = getNextId(project).getAndIncrement();
        return new Execution(project, id, type, details);
    }

    private AtomicInteger getNextId(Project project) {
        synchronized (executionIds) {
            String key = project.getKey();
            AtomicInteger id = executionIds.get(key);
            if (id == null) {
                id = new AtomicInteger(1);
                executionIds.put(key, id);
            }
            return id;
        }
    }

    @Override
    public ExecutionKey execute(Project project, IndexedFeature feature) {
        LOGGER.info("Executing " + project.getKey() + " feature \"" + feature.getName() + "\"");
        return execute(project, feature, null, true);
    }

    @Override
    public ExecutionKey execute(Project project, String gherkin) {
        LOGGER.info("Executing " + project.getKey() + " raw gherkin");
        return execute(project, null, gherkin, false);
    }

    @Override
    public ExecutionKey execute(Project project, IndexedScenario scenario) {
        LOGGER.info("Executing " + project.getKey() + " scenario \"" + scenario.getName() + "\"");
        Execution execution = nextExecution(project, ExecutionType.SCENARIO, scenario.getName());
        return executeWithLine(project, execution, scenario, -1);
    }

    @Override
    public ExecutionKey executeExample(Project project, IndexedScenario outline, int line) {
        LOGGER.info("Executing " + project.getKey() + " scenario outline \"" + outline.getName() + "\" line " + line);
        Execution execution = nextExecution(project, ExecutionType.EXAMPLE, Integer.toString(line));
        return executeWithLine(project, execution, outline, line);
    }

    private ExecutionKey executeWithLine(Project project, Execution execution, IndexedScenario scenario, int line) {
        copyScenarios(project, execution.getExecutionDirectory(), scenario, line);
        return executeScenarios(project, execution);
    }

    private ExecutionKey execute(Project project, IndexedFeature feature, String gherkin, boolean useFeature) {
        Execution execution;
        if (useFeature) {
            execution = nextExecution(project, ExecutionType.FEATURE, feature.getName());
            copyFeature(project, execution.getExecutionDirectory(), feature);
        } else {
            execution = nextExecution(project, ExecutionType.GHERKIN, "");
            copyGherkin(project, execution.getExecutionDirectory(), gherkin);
        }
        return executeScenarios(project, execution);
    }

    private void copyFeature(Project project, File tempDir, IndexedFeature feature) {
        try {
            // Setup tagged destination file
            File featureFile = tempFeatureFile(project, tempDir, feature);
            featureFile.getParentFile().mkdirs();
            Writer tempFile = new FileWriter(featureFile);

            // Load source file
            String gherkin = Utils.readContents(feature.getUri());

            // Parse to copy source into destination
            Parser parser = new Parser(new GreenhouseTagger(tempFile));
            LOGGER.info("Copying " + feature.getUri() + " into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy scenarios", e);
        }
    }

    private void copyGherkin(Project project, File tempDir, String gherkin) {
        try {
            // Setup tagged destination file
            File featureFile = file(tempDir.getPath(), "gherkin.feature");
            Writer tempFile = new FileWriter(featureFile);

            // Parse to copy source into destination
            Parser parser = new Parser(new GreenhouseTagger(tempFile));
            LOGGER.info("Copying raw gherkin into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy gherkin", e);
        }
    }

    private File tempFeatureFile(Project project, File tempDir, IndexedFeature feature) {
        String root = project.index().getFeaturesRoot().getAbsolutePath();
        String subPath = feature.getUri().substring(root.length() + 1);
        return file(tempDir.getPath(), subPath);
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
            LOGGER.info("Tagging " + feature.getUri() + " into " + featureFile.getPath());
            parser.parse(gherkin, featureFile.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy scenarios", e);
        }
    }

    private ExecutionKey executeScenarios(Project project, final Execution execution) {
        String features = "-Dcucumber.features=\"" + execution.getExecutionDirectory().getAbsolutePath() + "\"";
        String tags = "-Dcucumber.tagsArg=\"--tags=@greenhouse\"";
        String format = "-Dcucumber.format=html";
        String out = "-Dcucumber.out=" + file(execution.getExecutionDirectory().getAbsolutePath(), "report.html").getAbsolutePath();

        try {
            ArrayList<String> argsList = Lists.newArrayList(Splitter.on(' ').split(project.getCommand()));
            argsList.addAll(Lists.newArrayList(features, tags, format, out, ">", execution.getOutputFile().getAbsolutePath()));
            final ProcessBuilder builder = Utils.mavenProcess(project.getFiles(), argsList);

            String command = Joiner.on(' ').join(builder.command());
            execution.setCommand(command);
            LOGGER.info("Executing: " + command);

            final ExecutionKey executionKey = execution.getKey();
            Future<Void> task = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        execution.setStart(System.currentTimeMillis());
                        execution.setState(ExecutionState.RUNNING);
                        save(execution);
                        LOGGER.info("Running: " + executionKey);

                        builder.start().waitFor();

                        complete(execution);
                        LOGGER.info("Task " + executionKey + " complete.");
                    } catch (RuntimeException e) {
                        complete(execution);
                        LOGGER.error("Task " + executionKey + " threw error", e);
                    }
                    return null;
                }

                private void complete(final Execution execution) {
                    execution.setEnd(System.currentTimeMillis());
                    execution.setState(ExecutionState.COMPLETE);
                    save(execution);
                }
            });

            execution.setResult(task);
            tasks.put(executionKey, execution);
            return executionKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void save(Execution execution) {
        final Properties props = new Properties();
        props.setProperty("command", execution.getCommand());
        props.setProperty("details", execution.getDetails());
        props.setProperty("end", Long.toString(execution.getEnd()));
        props.setProperty("type", execution.getType().name());
        props.setProperty("start", Long.toString(execution.getStart()));

        final File executionPropsFile = Utils.file(execution.getExecutionDirectory().getAbsolutePath(), "execution.properties");
        Utils.save(executionPropsFile, props);
    }

    @Override
    public Execution getExecution(ExecutionKey taskId) {
        return tasks.get(taskId);
    }

    @Override
    public Iterable<Execution> getAllExecutions() {
        return tasks.values();
    }

}
