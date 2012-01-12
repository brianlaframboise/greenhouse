package greenhouse.ui.wicket.page;

import greenhouse.index.Index;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class GreenhousePage extends WebPage {

    @SpringBean
    protected ProjectRepository repo;

    public GreenhousePage() {
        this(null);
    }

    public GreenhousePage(PageParameters params) {
        super(params);
        add(new BookmarkablePageLink<Void>("home", ProjectsPage.class));
    }

    protected String getProjectKey() {
        String key = getPageParameters().getString("0", "");
        if ("".equals(key)) {
            throw new RestartResponseException(ProjectsPage.class);
        }
        return key;
    }

    protected Index index() {
        return project().index();
    }

    protected Project project() {
        return repo.getProject(getProjectKey());
    }

}
