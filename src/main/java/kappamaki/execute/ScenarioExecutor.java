package kappamaki.execute;

import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;

public interface ScenarioExecutor {

    String execute(IndexedFeature feature);

    String execute(IndexedScenario scenario);

    String executeExample(IndexedScenario outline, int line);
}
