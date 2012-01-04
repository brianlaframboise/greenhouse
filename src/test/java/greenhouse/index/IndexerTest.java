package greenhouse.index;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerTest {

    private static InMemoryIndex index;

    @BeforeClass
    public static void build_index() {
        URL resource = IndexerTest.class.getResource(".");
        String projectRoot = resource.getPath().toString() + "../../../../demo/example/files";
        index = new Indexer(projectRoot).index();
    }

    @Test
    public void correct_tag_counts() {
        assertThat(index.findByTag("@hello").size(), is(1));
        assertThat(index.findByTag("@goodbye").size(), is(1));
        assertThat(index.findByTag("@world").size(), is(2));
    }

    @Test
    public void correct_number_of_steps_indexed() {
        assertThat(index.steps().size(), is(3));
    }
}
