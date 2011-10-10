package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;

import java.util.List;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.google.common.collect.ImmutableList;

@MountPath(path = "/projects")
public class ProjectsPage extends GreenhousePage {

    private final class ProjectsListModel extends AbstractReadOnlyModel<List<Project>> {
        @Override
        public List<Project> getObject() {
            return ImmutableList.copyOf(projects.getProjects().values());
        }
    }

    @SpringBean
    private ProjectRepository projects;

    public ProjectsPage() {
        add(new ListView<Project>("projects", new ProjectsListModel()) {
            @Override
            protected void populateItem(ListItem<Project> item) {
                Project project = item.getModelObject();
                item.add(new Label("name", project.getName()));
                item.add(new BookmarkablePageLink<Void>("features", FeaturesPage.class, new PageParameters("0=" + project.getKey())));
                item.add(new BookmarkablePageLink<Void>("tags", TagsPage.class, new PageParameters("0=" + project.getKey())));
                item.add(new BookmarkablePageLink<Void>("steps", StepsPage.class, new PageParameters("0=" + project.getKey())));
            }
        });
    }
}
