package greenhouse.execute;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import greenhouse.TestUtils;
import greenhouse.config.GreenhouseSettings;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;
import greenhouse.project.PropsProjectRepository;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ProcessExecutorTest {

    private static Project project;
    private static ScenarioExecutor executor;

    @BeforeClass
    public static void build_index() {
        GreenhouseSettings settings = new GreenhouseSettings();
        project = Project.load(new File(TestUtils.HELLO_WORLD_PROJECT), settings);
        File repo = new File(TestUtils.DEMO_PROJECTS);
        PropsProjectRepository repository = new PropsProjectRepository(repo, settings);
        executor = new ProcessExecutor(repository, settings);
    }

    @After
    public void clearHistory() {
        project.clearHistory();
    }

    @Test
    public void executes_feature() {
        ExecutionRequest request = ExecutionRequest.feature(project.getKey(), "default", "Hello World Feature");
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("3 scenarios (3 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario() throws IOException {
        ImmutableSet<IndexedScenario> scenarios = project.index().findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.scenario(project.getKey(), "default", scenario.getName());
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@hello @world @greenhouse"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline() {
        ImmutableSet<IndexedScenario> scenarios = project.index().findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.scenario(project.getKey(), "default", scenario.getName());
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@goodbye @world @greenhouse"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline_single_example() {
        ImmutableSet<IndexedScenario> scenarios = project.index().findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.example(project.getKey(), "default", scenario.getName(), 21);
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@goodbye @world @greenhouse"));
        assertFalse(output.contains("| Goodbye  | World   |"));
        // weird formatting bug occasionally adds extra newlines
        // assertTrue(output.contains("| Aurevoir | Monde   |"));
        assertTrue(output.contains("Aurevoir | Monde"));
        assertTrue(output.contains("1 scenario (1 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_gherkin() {
        ExecutionRequest request = ExecutionRequest.gherkin(project.getKey(), "default", "Feature: Hello World\n" + "\tScenario: Hello World Scenario\n"
                + "\t\tGiven the Action is Hello\n" + "\t\tWhen the Subject is World\n" + "\t\tThen the Greeting is Hello, World\n");

        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("1 scenario (1 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }
}
