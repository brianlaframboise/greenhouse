package kappamaki.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class PropsProjectRepositoryTest {

    @Test
    public void load() {
        File repo = new File("D:\\workspaces\\default\\kappamaki\\src\\main\\resources\\kappamaki\\project\\test.properties");
        PropsProjectRepository repository = new PropsProjectRepository(repo);
        ImmutableMap<String, Project> projects = repository.getProjects();
        assertEquals(1, projects.size());

        Project project = projects.get("foo");
        assertNotNull(project);
        assertEquals("Foo", project.getName());
        assertEquals("C:\\foo", project.getRoot().getAbsolutePath());
    }
}
