package execute;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import kappamaki.index.Index;
import kappamaki.index.IndexedScenario;
import kappamaki.index.Indexer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ScenarioExecutorTest {

    private static Index index;

    @BeforeClass
    public static void build_index() {
        String projectRoot = System.getProperty("user.dir");
        String uri = projectRoot + "\\example\\features";
        index = new Indexer(uri).index();
    }

    @Test
    public void executes_tagged_scenario() throws IOException {
        ImmutableSet<IndexedScenario> scenarios = index.findByTag("@hello");
        assertThat(scenarios.size(), is(1));

        IndexedScenario scenario = scenarios.iterator().next();
        String root = System.getProperty("user.dir") + "\\example";
        ScenarioExecutor executor = new ScenarioExecutor(index, scenario, root);

        File output = executor.makeOutputFile();
        executor.setOutput(output);
        executor.execute();

        FileReader outputReader = new FileReader(output);
        String outputString = FixJava.readReader(outputReader);
        outputReader.close();

        assertTrue(outputString.contains("@hello @world @kappamaki"));
        assertTrue(outputString.contains("BUILD SUCCESS"));
    }
}
