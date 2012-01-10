package greenhouse.config;

import greenhouse.execute.ProcessExecutor;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.project.ProjectRepository;
import greenhouse.project.PropsProjectRepository;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreenhouseConfiguration {

    @Value("#{systemProperties['greenhouse.projects']}")
    private File projects;

    @Bean
    public ProjectRepository projectRepository() {
        return new PropsProjectRepository(projects);
    }

    @Bean
    public ScenarioExecutor scenarioExecutor() {
        ProcessExecutor executor = new ProcessExecutor();
        executor.reset(projectRepository(), projects);
        return executor;
    }
}
