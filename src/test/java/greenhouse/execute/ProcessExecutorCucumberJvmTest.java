package greenhouse.execute;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import greenhouse.TestUtils;
import greenhouse.config.GreenhouseSettings;
import greenhouse.index.InMemoryIndexRepository;
import greenhouse.index.Index;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.project.PropsProjectRepository;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ProcessExecutorCucumberJvmTest {

    private static Project project;
    private static ScenarioExecutor executor;
    private static Index index;
    private static Context context;

    @BeforeClass
    public static void build_index() {
        GreenhouseSettings settings = new GreenhouseSettings();
        InMemoryIndexRepository indices = new InMemoryIndexRepository(settings);
        PropsProjectRepository repository = new PropsProjectRepository(new File(TestUtils.DEMO_PROJECTS), indices);
        executor = new ProcessExecutor(repository, indices, settings);
        project = repository.getProject("CJVM");
        index = indices.getIndex("CJVM");
        context = project.getContexts().get("default");
    }

    @After
    public void clearHistory() {
        project.clearHistory();
    }

    @Test
    public void executes_feature() {
        ExecutionRequest request = ExecutionRequest.feature(project.getKey(), context, "Hello World Feature");
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("Tests run: 12, Failures: 0, Errors: 0, Skipped: 0"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario() throws IOException {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.scenario(project.getKey(), context, scenario.getName());
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@hello @world @greenhouse"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline() {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.scenario(project.getKey(), context, scenario.getName());
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@goodbye @greenhouse @world"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline_single_example() {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        ExecutionRequest request = ExecutionRequest.example(project.getKey(), context, scenario.getName(), 22);
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@goodbye @greenhouse @world"));
        assertTrue(output.contains("Given the Action is Aurevoir"));
        assertTrue(output.contains("When the Subject is Monde"));
        assertTrue(output.contains("Then the Greeting is Aurevoir, Monde"));
        assertTrue(output.contains("Tests run: 4, Failures: 0, Errors: 0, Skipped: 0"));
    }

    @Test
    public void executes_tag() {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        ExecutionRequest request = ExecutionRequest.tag(project.getKey(), context, "@hello");
        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("@hello @world"));
        assertFalse(output.contains("@greenhouse"));
        assertTrue(output.contains("Tests run: 4, Failures: 0, Errors: 0, Skipped: 0"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_gherkin() {
        ExecutionRequest request = ExecutionRequest.gherkin(project.getKey(), context, "Feature: Hello World\n" + "\tScenario: Hello World Scenario\n"
                + "\t\tGiven the Action is Hello\n" + "\t\tWhen the Subject is World\n" + "\t\tThen the Greeting is Hello, World\n");

        ExecutionKey executionKey = executor.execute(request);
        String output = executor.getExecution(executionKey).getCompletedOutput();

        assertTrue(output.contains("Tests run: 4, Failures: 0, Errors: 0, Skipped: 0"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }
}
