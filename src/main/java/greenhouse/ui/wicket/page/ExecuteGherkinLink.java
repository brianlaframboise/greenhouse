package greenhouse.ui.wicket.page;

import greenhouse.execute.TaskId;

import org.apache.wicket.model.IModel;

class ExecuteGherkinLink extends ExecutingLink {
    private final IModel<String> gherkinModel;

    public ExecuteGherkinLink(String id, String projectKey, OutputDialog dialog, IModel<String> gherkinModel) {
        super(id, projectKey, dialog);
        this.gherkinModel = gherkinModel;
    }

    @Override
    protected TaskId execute() {
        return executor.execute(project(), gherkinModel.getObject());
    }
}