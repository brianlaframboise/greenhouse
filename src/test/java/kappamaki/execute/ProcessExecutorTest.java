package kappamaki.execute;

import static kappamaki.util.Utils.joinPaths;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import kappamaki.index.InMemoryIndex;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;
import kappamaki.index.Indexer;
import kappamaki.util.Utils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ProcessExecutorTest {

    private static InMemoryIndex index;
    private static final String PROJECT_ROOT = Utils.USER_DIR;

    private ProcessExecutor executor;

    @BeforeClass
    public static void build_index() {
        File root = joinPaths(PROJECT_ROOT, "example");
        index = new Indexer(root.getAbsolutePath()).index();
    }

    @Before
    public void create_executor() {
        File project = joinPaths(PROJECT_ROOT, "example");
        executor = new ProcessExecutor(index, project);
    }

    @Test
    public void executes_feature() {
        IndexedFeature feature = index.featureByName("Hello World Feature");

        int taskId = executor.execute(feature);
        String output = executor.getOutput(taskId);

        assertTrue(output.contains("3 scenarios (3 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario() throws IOException {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        int taskId = executor.execute(scenario);
        String output = executor.getOutput(taskId);

        assertTrue(output.contains("@hello @world @kappamaki"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline() {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        int taskId = executor.execute(scenario);
        String output = executor.getOutput(taskId);

        assertTrue(output.contains("@goodbye @world @kappamaki"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_tagged_scenario_outline_single_example() {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@goodbye");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        int taskId = executor.executeExample(scenario, 21);
        String output = executor.getOutput(taskId);

        assertTrue(output.contains("@goodbye @world @kappamaki"));
        assertFalse(output.contains("| Goodbye  | World   |"));
        assertTrue(output.contains("| Aurevoir | Monde   |"));
        assertTrue(output.contains("1 scenario (1 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }

    @Test
    public void executes_gherkin() {
        int taskId = executor.execute("Feature: Hello World\n" + "\tScenario: Hello World Scenario\n" + "\t\tGiven the Action is Hello\n"
                + "\t\tWhen the Subject is World\n" + "\t\tThen the Greeting is Hello, World\n");
        String output = executor.getOutput(taskId);

        assertTrue(output.contains("1 scenario (1 passed)"));
        assertTrue(output.contains("BUILD SUCCESS"));
    }
}
