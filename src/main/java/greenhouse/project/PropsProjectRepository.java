package greenhouse.project;

import greenhouse.config.GreenhouseSettings;
import greenhouse.util.DirectoryFilter;

import java.io.File;

import com.google.common.collect.ImmutableMap;

public class PropsProjectRepository implements ProjectRepository {

    private ImmutableMap<String, Project> projects = ImmutableMap.<String, Project> of();

    public PropsProjectRepository(File projectsDir, GreenhouseSettings settings) {
        File[] directories = projectsDir.listFiles(new DirectoryFilter());

        for (File root : directories) {
            add(Project.load(root, settings));
        }
    }

    @Override
    public Project getProject(String projectKey) {
        return projects.get(projectKey);
    }

    @Override
    public Iterable<Project> getAllProjects() {
        return projects.values();
    }

    @Override
    public void add(Project project) {
        projects = ImmutableMap.<String, Project> builder().putAll(projects).put(project.getKey(), project).build();
    }

}
