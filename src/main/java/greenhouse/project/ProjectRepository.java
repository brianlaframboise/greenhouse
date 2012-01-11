package greenhouse.project;

public interface ProjectRepository {

    Iterable<Project> getAllProjects();

    Project getProject(String projectKey);

    void add(Project project);
}
