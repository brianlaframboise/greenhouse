package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.ui.wicket.page.features.FeaturesPage;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.ImmutableList;

public class ProjectsPage extends GreenhousePage {

    private final class ProjectsListModel extends AbstractReadOnlyModel<List<Project>> {
        @Override
        public List<Project> getObject() {
            return ImmutableList.copyOf(repo.getAllProjects());
        }
    }

    public ProjectsPage(PageParameters params) {
        super(params);
        add(new ListView<Project>("projects", new ProjectsListModel()) {
            @Override
            protected void populateItem(ListItem<Project> item) {
                Project project = item.getModelObject();
                PageParameters pp = new PageParameters();
                pp.add("project", project.getKey());
                Link<Void> link = new BookmarkablePageLink<Void>("link", FeaturesPage.class, pp);
                item.add(link.add(new Label("name", project.getName())));
            }
        });
    }

}
