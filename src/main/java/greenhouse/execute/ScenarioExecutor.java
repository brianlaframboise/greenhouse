package greenhouse.execute;

import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;

public interface ScenarioExecutor {

    TaskId execute(Project project, IndexedFeature feature);

    TaskId execute(Project project, IndexedScenario scenario);

    TaskId executeExample(Project project, IndexedScenario outline, int line);

    TaskId execute(Project project, String gherkin);

    String getOutput(TaskId taskId);

    String getPartialOutput(TaskId taskId);

    boolean isComplete(TaskId taskId);
}
