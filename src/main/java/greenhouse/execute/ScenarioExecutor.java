package greenhouse.execute;

import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;

public interface ScenarioExecutor {

    int execute(Project project, IndexedFeature feature);

    int execute(Project project, IndexedScenario scenario);

    int executeExample(Project project, IndexedScenario outline, int line);

    int execute(Project project, String gherkin);

    String getOutput(int taskId);

    String getPartialOutput(int taskId);

    boolean isComplete(int taskId);
}
