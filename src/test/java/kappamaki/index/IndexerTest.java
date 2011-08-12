package kappamaki.index;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import kappamaki.index.Index;
import kappamaki.index.Indexer;

import org.junit.BeforeClass;
import org.junit.Test;

public class IndexerTest {

    private static Index index;

    @BeforeClass
    public static void build_index() {
        String projectRoot = System.getProperty("user.dir");
        String uri = projectRoot + "\\example\\features";
        index = new Indexer(uri).index();
    }

    @Test
    public void correct_tag_counts() {
        assertThat(index.findByTag("@hello").size(), is(1));
        assertThat(index.findByTag("@goodbye").size(), is(1));
        assertThat(index.findByTag("@world").size(), is(2));
    }
}
