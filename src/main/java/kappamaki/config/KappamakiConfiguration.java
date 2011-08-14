package kappamaki.config;

import static kappamaki.util.Utils.USER_DIR;

import java.io.File;

import kappamaki.execute.ProcessExecutor;
import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.Indexer;
import kappamaki.util.Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KappamakiConfiguration {

    @Bean
    public Index index() {
        File features = Utils.joinPaths(USER_DIR, "example", "features");
        return new Indexer(features).index();
    }

    @Bean
    public ScenarioExecutor scenarioExecutor() {
        File project = Utils.joinPaths(USER_DIR, "example");
        return new ProcessExecutor(index(), project);
    }
}
