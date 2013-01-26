package greenhouse.ui.wicket.page;

import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ExecutionRequest;

import org.apache.wicket.model.IModel;

class ExecuteGherkinLink extends ExecutingLink {
    private final IModel<String> gherkinModel;

    public ExecuteGherkinLink(String id, String projectKey, IModel<String> gherkinModel) {
        super(id, projectKey);
        this.gherkinModel = gherkinModel;
    }

    @Override
    protected ExecutionKey execute() {
        return executor.execute(ExecutionRequest.gherkin(projectKey, context(), gherkinModel.getObject()));

    }
}