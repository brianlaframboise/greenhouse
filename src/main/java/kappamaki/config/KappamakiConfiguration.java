package kappamaki.config;

import java.io.File;

import kappamaki.project.ProjectRepository;
import kappamaki.project.PropsProjectRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KappamakiConfiguration {

    @Value("#{systemProperties['kappamaki.repo']}")
    private File repo;

    @Bean
    public ProjectRepository projectRepository() {
        return new PropsProjectRepository(repo);
    }
}
