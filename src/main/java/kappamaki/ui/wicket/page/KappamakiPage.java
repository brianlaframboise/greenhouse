package kappamaki.ui.wicket.page;

import kappamaki.index.Index;
import kappamaki.project.Project;
import kappamaki.project.ProjectRepository;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.google.common.collect.ImmutableMap;

public class KappamakiPage extends WebPage {

    @SpringBean
    private ProjectRepository repo;

    public KappamakiPage() {
    }

    public KappamakiPage(PageParameters params) {
        super(params);
    }

    protected String getProjectKey() {
        return getPageParameters().getString("0");
    }

    protected Index index() {
        ImmutableMap<String, Project> projects = repo.getProjects();
        Project project = projects.get(getProjectKey());
        return project.index();
    }

}
