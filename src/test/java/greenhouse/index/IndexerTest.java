package greenhouse.index;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import greenhouse.TestUtils;
import greenhouse.config.GreenhouseSettings;

import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerTest {

    private static InMemoryIndex index;

    @BeforeClass
    public static void build_index() {
        index = new Indexer("hello-world", TestUtils.HELLO_WORLD_FILES, "greenhouse.example", new GreenhouseSettings()).index();
    }

    @Test
    public void correct_tag_counts() {
        assertThat(index.findByTag("@hello").size(), is(1));
        assertThat(index.findByTag("@goodbye").size(), is(1));
        assertThat(index.findByTag("@world").size(), is(2));
        assertThat(index.findByTag("@feature").size(), is(2));
    }

    @Test
    public void correct_number_of_steps_indexed() {
        assertThat(index.steps().size(), is(3));
    }
}
