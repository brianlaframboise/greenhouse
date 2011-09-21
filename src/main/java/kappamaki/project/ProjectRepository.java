package kappamaki.project;

import com.google.common.collect.ImmutableMap;

public interface ProjectRepository {

    ImmutableMap<String, Project> getProjects();

    void add(Project project);
}
