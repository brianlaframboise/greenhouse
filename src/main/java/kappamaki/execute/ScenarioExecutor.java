package kappamaki.execute;

import kappamaki.index.IndexedScenario;

public interface ScenarioExecutor {

    String execute(IndexedScenario scenario);

    String executeExample(IndexedScenario outline, int line);
}
