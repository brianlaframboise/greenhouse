package greenhouse.execute;

import static greenhouse.util.Utils.file;
import gherkin.formatter.Formatter;
import gherkin.parser.Parser;
import greenhouse.config.GreenhouseSettings;
import greenhouse.index.Index;
import greenhouse.index.IndexRepository;
import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Context;
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
import com.google.common.collect.Iterables;
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

    private final ProjectRepository repository;
    private final IndexRepository indices;
    private final GreenhouseSettings settings;

    public ProcessExecutor(ProjectRepository repository, IndexRepository indices, GreenhouseSettings settings) {
        this.repository = repository;
        this.indices = indices;
        this.settings = settings;
    }

    /**
     * Resets and reloads the stored project state.
     * 
     * @param repo
     * @param projectsDir
     */
    public void reset(File projectsDir) {
        tasks.clear();
        executionIds.clear();

        for (File file : projectsDir.listFiles(new DirectoryFilter())) {
            String projectKey = file.getName();
            File executionsDirectory = Utils.file(file.getAbsolutePath(), "executions");
            if (executionsDirectory.exists()) {
                loadExecutions(projectKey, executionsDirectory);
            }
        }
    }

    private void loadExecutions(String projectKey, File executionsDirectory) {
        Project project = repository.getProject(projectKey);
        int max = 1;
        try {
            for (File executionDir : executionsDirectory.listFiles(new DirectoryFilter())) {
                int executionNumber = Integer.valueOf(executionDir.getName());
                Properties props = Utils.load(executionDir, "execution.properties");

                ExecutionType type = ExecutionType.valueOf(props.getProperty("type"));
                ExecutionRequest request = null;

                String key = props.getProperty("context.key");
                String name = props.getProperty("context.name");
                String command = props.getProperty("context.command");
                Context context = new Context(key, name, command);

                if (type == ExecutionType.FEATURE) {
                    request = ExecutionRequest.feature(projectKey, context, props.getProperty("feature"));
                } else if (type == ExecutionType.SCENARIO) {
                    request = ExecutionRequest.scenario(projectKey, context, props.getProperty("scenario"));
                } else if (type == ExecutionType.EXAMPLE) {
                    request = ExecutionRequest.example(projectKey, context, props.getProperty("scenario"), Integer.valueOf(props.getProperty("line")));
                } else if (type == ExecutionType.GHERKIN) {
                    request = ExecutionRequest.gherkin(projectKey, context, props.getProperty("gherkin"));
                }

                Execution execution = new Execution(request, project.getRoot(), executionNumber);

                execution.setCommand(props.getProperty("command"));
                execution.setEnd(Long.valueOf(props.getProperty("end", "0")));
                execution.setStart(Long.valueOf(props.getProperty("start", "0")));
                execution.setState(ExecutionState.COMPLETE);

                tasks.put(execution.getKey(), execution);
                max = Math.max(max, executionNumber);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error while loading execution at " + executionsDirectory.getAbsolutePath(), e);
        }
        AtomicInteger nextId = getNextId(project);
        nextId.set(max + 1);
        executionIds.put(projectKey, nextId);
    }

    private Execution nextExecution(ExecutionRequest request, Project project) {
        int number = getNextId(project).getAndIncrement();
        return new Execution(request, project.getRoot(), number);
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
    public ExecutionKey execute(ExecutionRequest request) {
        ExecutionType type = request.getType();
        String projectKey = request.getProjectKey();
        Project project = repository.getProject(projectKey);
        Index index = indices.getIndex(projectKey);
        if (ExecutionType.FEATURE == type) {

            IndexedFeature feature = index.featureByName(request.getFeature());
            LOGGER.info("Executing " + project.getKey() + " feature \"" + feature.getName() + "\"");
            return execute(request, project, true);

        } else if (ExecutionType.GHERKIN == type) {

            LOGGER.info("Executing " + project.getKey() + " raw gherkin");
            return execute(request, project, false);

        } else if (ExecutionType.SCENARIO == type) {

            IndexedScenario scenario = index.scenarioByName(request.getScenario());
            LOGGER.info("Executing " + project.getKey() + " scenario \"" + scenario.getName() + "\"");
            Execution execution = nextExecution(request, project);
            return executeWithLine(project, execution, scenario, -1);

        } else if (ExecutionType.EXAMPLE == type) {

            IndexedScenario outline = index.scenarioByName(request.getScenario());
            int line = request.getLine();
            LOGGER.info("Executing " + project.getKey() + " scenario outline \"" + outline.getName() + "\" line " + line);
            Execution execution = nextExecution(request, project);
            return executeWithLine(project, execution, outline, line);

        }
        return null;
    }

    private ExecutionKey execute(ExecutionRequest request, Project project, boolean useFeature) {
        Execution execution;
        if (useFeature) {
            execution = nextExecution(request, project);
            Index index = indices.getIndex(project.getKey());
            IndexedFeature feature = index.featureByName(request.getFeature());
            copyFeature(project, execution.getExecutionDirectory(), feature);
        } else {
            execution = nextExecution(request, project);
            copyGherkin(project, execution.getExecutionDirectory(), request.getGherkin());
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
        Index index = indices.getIndex(project.getKey());
        String root = index.getFeaturesRoot().getAbsolutePath();
        String subPath = feature.getUri().substring(root.length() + 1);
        return file(tempDir.getPath(), subPath);
    }

    private ExecutionKey executeWithLine(Project project, Execution execution, IndexedScenario scenario, int line) {
        copyScenarios(project, execution.getExecutionDirectory(), scenario, line);
        return executeScenarios(project, execution);
    }

    private void copyScenarios(Project project, File tempDir, IndexedScenario scenario, int line) {
        try {
            // Setup tagged destination file
            Index index = indices.getIndex(project.getKey());
            IndexedFeature feature = index.findByScenario(scenario);
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
            String command = execution.getContext().getCommand();
            ArrayList<String> argsList = Lists.newArrayList(Splitter.on(' ').split(command));
            argsList.addAll(Lists.newArrayList(features, tags, format, out, ">", execution.getOutputFile().getAbsolutePath()));
            final ProcessBuilder builder = Utils.mavenProcess(settings.getMvn(), project.getFiles(), argsList);

            String builderCommand = Joiner.on(' ').join(builder.command());
            execution.setCommand(builderCommand);
            LOGGER.info("Executing: " + builderCommand);

            final ExecutionKey executionKey = execution.getKey();
            Future<Void> task = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        execution.setStart(System.currentTimeMillis());
                        execution.setState(ExecutionState.RUNNING);
                        save(execution);
                        LOGGER.info("Running: " + executionKey);
                        builder.redirectErrorStream(true);
                        builder.start().waitFor();

                        complete(execution);
                        LOGGER.info("Task " + executionKey + " complete.");
                    } catch (RuntimeException e) {
                        try {
                            complete(execution);
                            LOGGER.error("Task " + executionKey + " threw error", e);
                        } catch (RuntimeException e2) {
                            LOGGER.error("Error while handling error", e2);
                        }
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

        props.setProperty("type", execution.getType().name());
        props.setProperty("feature", String.valueOf(execution.getFeatureName()));
        props.setProperty("scenario", String.valueOf(execution.getScenarioName()));
        props.setProperty("line", Integer.toString(execution.getExampleLine()));
        props.setProperty("gherkin", String.valueOf(execution.getGherkin()));

        props.setProperty("context.key", execution.getContext().getKey());
        props.setProperty("context.name", execution.getContext().getName());
        props.setProperty("context.command", execution.getContext().getCommand());

        props.setProperty("command", execution.getCommand());
        props.setProperty("end", Long.toString(execution.getEnd()));
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
        return Iterables.unmodifiableIterable(tasks.values());
    }

}
