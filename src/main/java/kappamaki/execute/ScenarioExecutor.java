package kappamaki.execute;

import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;

public interface ScenarioExecutor {

    int execute(IndexedFeature feature);

    int execute(IndexedScenario scenario);

    int executeExample(IndexedScenario outline, int line);

    int execute(String gherkin);

    String getOutput(int taskId);

    String getPartialOutput(int taskId);

    boolean isComplete(int taskId);
}
