package greenhouse.ui.wicket.page;

import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.index.Index;
import greenhouse.index.IndexRepository;
import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;
import greenhouse.ui.wicket.WicketUtils;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;

abstract class ExecutingLink extends Link<Void> {

    @SpringBean
    protected ProjectRepository repo;

    @SpringBean
    protected ScenarioExecutor executor;

    @SpringBean
    protected IndexRepository indices;

    protected final String projectKey;

    public ExecutingLink(String id, String projectKey) {
        super(id);
        this.projectKey = projectKey;
    }

    @Override
    public void onClick() {
        ExecutionKey key = execute();
        PageParameters params = new PageParameters();
        params.add("0", projectKey);
        params.add("1", BaseProjectPage.simpleName(HistoryPage.class));
        params.add("2", Integer.toString(key.getNumber()));
        setResponsePage(ProjectsPage.class, params);
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