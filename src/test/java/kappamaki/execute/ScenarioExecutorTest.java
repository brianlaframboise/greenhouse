package kappamaki.execute;

import static kappamaki.util.Utils.joinPaths;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import kappamaki.index.InMemoryIndex;
import kappamaki.index.IndexedScenario;
import kappamaki.index.Indexer;
import kappamaki.util.Utils;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ScenarioExecutorTest {

    private static InMemoryIndex index;
    private static final String PROJECT_ROOT = Utils.USER_DIR;

    @BeforeClass
    public static void build_index() {
        String features = joinPaths(PROJECT_ROOT, "example", "features");
        index = new Indexer(features).index();
    }

    @Test
    public void executes_tagged_scenario() throws IOException {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();

        String project = joinPaths(PROJECT_ROOT, "example");
        ProcessExecutor executor = new ProcessExecutor(index, project);

        String outputString = executor.execute(scenario);

        assertTrue(outputString.contains("@hello @world @kappamaki"));
        assertTrue(outputString.contains("BUILD SUCCESS"));
    }
}
