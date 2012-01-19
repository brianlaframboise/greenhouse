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
        assertEquals(1, projects.size());

        Project project = projects.get(0);
        assertNotNull(project);
        assertEquals("Hello World Example Project", project.getName());
        assertFalse(project.getRoot().getAbsolutePath().equals(""));
    }
}
