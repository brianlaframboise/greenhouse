package greenhouse.config;

import greenhouse.execute.ProcessExecutor;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.index.InMemoryIndexRepository;
import greenhouse.index.IndexRepository;
import greenhouse.project.ProjectRepository;
import greenhouse.project.PropsProjectRepository;
import greenhouse.util.Utils;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreenhouseConfiguration {

    @Value("#{systemProperties['greenhouse.projects']}")
    private File projects;

    @Bean
    public IndexRepository indexRepository() {
        return new InMemoryIndexRepository(greenhouseSettings());
    }

    @Bean
    public ProjectRepository projectRepository() {
        return new PropsProjectRepository(projects, indexRepository());
    }

    @Bean
    public GreenhouseSettings greenhouseSettings() {
        GreenhouseSettings settings = new GreenhouseSettings();

        if (Utils.file(projects.getAbsolutePath(), "greenhouse.properties").exists()) {
            settings.setMvn(Utils.load(projects, "greenhouse.properties").getProperty("mvn", settings.getMvn()));
        }

        return settings;
    }

    @Bean
    public ScenarioExecutor scenarioExecutor() {
        ProcessExecutor executor = new ProcessExecutor(projectRepository(), indexRepository(), greenhouseSettings());
        executor.reset(projects);
        return executor;
    }

}
