package greenhouse.ui.wicket.page;

import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.index.Index;
import greenhouse.index.IndexRepository;
import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;
import greenhouse.ui.wicket.WicketUtils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;

abstract class ExecutingLink extends IndicatingAjaxFallbackLink<Void> {

    @SpringBean
    protected ProjectRepository repo;

    @SpringBean
    protected ScenarioExecutor executor;

    @SpringBean
    protected IndexRepository indices;

    protected final String projectKey;
    private final OutputDialog dialog;

    public ExecutingLink(String id, String projectKey, OutputDialog dialog) {
        super(id);
        this.dialog = dialog;
        this.projectKey = projectKey;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        dialog.begin(execute(), target);
    }

    protected Index index() {
        return indices.getIndex(projectKey);
    }

    protected Project project() {
        return repo.getProject(projectKey);
    }

    protected String contextKey() {
        Project project = project();
        String contextKey = WicketUtils.getContextKey(getWebRequest(), project);
        if (contextKey == null) {
            WicketUtils.setContextKey(getWebRequest(), (WebResponse) getResponse(), project, project.getContexts().keySet().iterator().next());
        }
        return contextKey;
    }

    protected Context context() {
        return project().getContexts().get(contextKey());
    }

    protected abstract ExecutionKey execute();
}