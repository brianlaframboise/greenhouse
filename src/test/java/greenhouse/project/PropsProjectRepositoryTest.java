package greenhouse.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import greenhouse.TestUtils;
import greenhouse.config.GreenhouseSettings;

import java.io.File;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PropsProjectRepositoryTest {

    @Test
    public void load() {
        File repo = new File(TestUtils.DEMO_PROJECTS);
        PropsProjectRepository repository = new PropsProjectRepository(repo, new GreenhouseSettings());
        ImmutableList<Project> projects = ImmutableList.copyOf(repository.getAllProjects());
        assertEquals(1, projects.size());

        Project project = projects.get(0);
        assertNotNull(project);
        assertEquals("Hello World Example Project", project.getName());
        assertFalse(project.getRoot().getAbsolutePath().equals(""));
    }
}
