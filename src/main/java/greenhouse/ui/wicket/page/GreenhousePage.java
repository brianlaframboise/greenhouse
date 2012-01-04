package greenhouse.ui.wicket.page;

import greenhouse.index.Index;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.google.common.collect.ImmutableMap;

public class GreenhousePage extends WebPage {

    @SpringBean
    private ProjectRepository repo;

    public GreenhousePage() {
    }

    public GreenhousePage(PageParameters params) {
        super(params);
    }

    protected String getProjectKey() {
        String key = getPageParameters().getString("0", "");
        if ("".equals(key)) {
            throw new RestartResponseException(ProjectsPage.class);
        }
        return key;
    }

    protected Index index() {
        ImmutableMap<String, Project> projects = repo.getProjects();
        Project project = projects.get(getProjectKey());
        return project.index();
    }

}