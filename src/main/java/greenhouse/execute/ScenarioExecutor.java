package greenhouse.execute;

import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.project.Project;

public interface ScenarioExecutor {

    ExecutionKey execute(Project project, IndexedFeature feature);

    ExecutionKey execute(Project project, IndexedScenario scenario);

    ExecutionKey executeExample(Project project, IndexedScenario outline, int line);

    ExecutionKey execute(Project project, String gherkin);

    Execution getExecution(ExecutionKey executionKey);

    Iterable<Execution> getAllExecutions();
}
