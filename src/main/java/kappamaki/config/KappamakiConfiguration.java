package kappamaki.config;

import java.io.File;

import kappamaki.execute.ProcessExecutor;
import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.Indexer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KappamakiConfiguration {

    @Value("#{systemProperties['kappamaki.features']}")
    private File features;

    @Value("#{systemProperties['kappamaki.project']}")
    private File project;

    @Bean
    public Index index() {
        return new Indexer(features).index();
    }

    @Bean
    public ScenarioExecutor scenarioExecutor() {
        return new ProcessExecutor(index(), project);
    }
}
