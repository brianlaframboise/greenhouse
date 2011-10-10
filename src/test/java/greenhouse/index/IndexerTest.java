package greenhouse.index;

import static greenhouse.util.Utils.USER_DIR;
import static greenhouse.util.Utils.joinPaths;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import greenhouse.index.InMemoryIndex;
import greenhouse.index.Indexer;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerTest {

    private static InMemoryIndex index;

    @BeforeClass
    public static void build_index() {
        File root = joinPaths(USER_DIR, "example");
        index = new Indexer(root.getAbsolutePath()).index();
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
