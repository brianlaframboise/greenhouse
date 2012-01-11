package greenhouse.project;

import greenhouse.util.DirectoryFilter;

import java.io.File;

import com.google.common.collect.ImmutableMap;

public class PropsProjectRepository implements ProjectRepository {

    private ImmutableMap<String, Project> projects = ImmutableMap.<String, Project> of();

    public PropsProjectRepository(File projectsDir) {
        File[] directories = projectsDir.listFiles(new DirectoryFilter());

        for (File root : directories) {
            add(Project.load(root));
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
