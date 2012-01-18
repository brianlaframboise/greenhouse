package greenhouse.config;

import greenhouse.execute.ProcessExecutor;
import greenhouse.execute.ScenarioExecutor;
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
    public ProjectRepository projectRepository() {
        return new PropsProjectRepository(projects, greenhouseSettings());
    }

    @Bean
    public ScenarioExecutor scenarioExecutor() {
        ProcessExecutor executor = new ProcessExecutor(projectRepository(), greenhouseSettings());
        executor.reset(projects);
        return executor;
    }

    @Bean
    public GreenhouseSettings greenhouseSettings() {
        GreenhouseSettings settings = new GreenhouseSettings();

        if (Utils.file(projects.getAbsolutePath(), "greenhouse.properties").exists()) {
            settings.setMvn(Utils.load(projects, "greenhouse.properties").getProperty("mvn", settings.getMvn()));
        }

        return settings;
    }
}
