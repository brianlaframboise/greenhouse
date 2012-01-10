package greenhouse.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import greenhouse.TestUtils;

import java.io.File;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class PropsProjectRepositoryTest {

    @Test
    public void load() {
        File repo = new File(TestUtils.DEMO_PROJECTS);
        PropsProjectRepository repository = new PropsProjectRepository(repo);
        ImmutableMap<String, Project> projects = repository.getProjects();
        assertEquals(1, projects.size());

        Project project = projects.get("HELLO");
        assertNotNull(project);
        assertEquals("Hello World Example Project", project.getName());
        assertFalse(project.getRoot().getAbsolutePath().equals(""));
    }
}
