package greenhouse.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import greenhouse.TestUtils;
import greenhouse.config.GreenhouseSettings;
import greenhouse.index.InMemoryIndexRepository;

import java.io.File;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PropsProjectRepositoryTest {

    @Test
    public void load() {
        GreenhouseSettings settings = new GreenhouseSettings();
        InMemoryIndexRepository indices = new InMemoryIndexRepository(settings);
        PropsProjectRepository repository = new PropsProjectRepository(new File(TestUtils.DEMO_PROJECTS), indices);

        ImmutableList<Project> projects = ImmutableList.copyOf(repository.getAllProjects());
        assertEquals(2, projects.size());

        Project cjvm = projects.get(0);
        assertNotNull(cjvm);
        assertEquals("Cucumber-JVM Example Project", cjvm.getName());
        assertFalse(cjvm.getRoot().getAbsolutePath().equals(""));

        Project cuke4duke = projects.get(1);
        assertNotNull(cuke4duke);
        assertEquals("Cuke4Duke Example Project", cuke4duke.getName());
        assertFalse(cuke4duke.getRoot().getAbsolutePath().equals(""));

    }
}
