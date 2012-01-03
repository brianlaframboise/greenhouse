package greenhouse.execute;

import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;

public interface ScenarioExecutor {

    int execute(IndexedFeature feature);

    int execute(IndexedScenario scenario);

    int executeExample(IndexedScenario outline, int line);

    int execute(String gherkin);

    String getOutput(int taskId);

    String getPartialOutput(int taskId);

    boolean isComplete(int taskId);

    void setPhase(String command);
}
