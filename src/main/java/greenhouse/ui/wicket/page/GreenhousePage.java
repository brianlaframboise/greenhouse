package greenhouse.ui.wicket.page;

import greenhouse.index.Index;
import greenhouse.index.IndexRepository;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class GreenhousePage extends WebPage {

    @SpringBean
    protected ProjectRepository repo;

    @SpringBean
    protected IndexRepository indices;

    public GreenhousePage(PageParameters params) {
        super(params);
        add(new BookmarkablePageLink<Void>("home", ProjectsPage.class));
    }

    protected String getProjectKey() {
        String key = getPageParameters().get("project").toString("");
        if ("".equals(key)) {
            throw new RestartResponseException(ProjectsPage.class);
        }
        return key;
    }

    protected Index index() {
        return indices.getIndex(getProjectKey());
    }

    protected Project project() {
        return repo.getProject(getProjectKey());
    }

}
