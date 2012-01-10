package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.ui.wicket.WicketUtils;

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.collect.ImmutableList;

@MountPath(path = "/projects")
@MountIndexedParam
public class ProjectsPage extends GreenhousePage {

    private final class ProjectsListModel extends AbstractReadOnlyModel<List<Project>> {
        @Override
        public List<Project> getObject() {
            return ImmutableList.copyOf(repo.getProjects().values());
        }
    }

    public ProjectsPage(PageParameters params) {
        if (params != null) {
            String key = params.getString("0", "");
            Project project = repo.getProjects().get(key);
            if (project != null) {
                String page = params.getString("1", "");
                if (page.equals("features")) {
                    setResponsePage(FeaturesPage.class, params);
                } else if (page.equals("tags")) {
                    setResponsePage(TagsPage.class, params);
                } else if (page.equals("create")) {
                    setResponsePage(CreatePage.class, params);
                } else if (page.equals("history")) {
                    setResponsePage(HistoryPage.class, params);
                } else {
                    setResponsePage(ProjectPage.class, params);
                }
            } else if (!"".equals(key)) {
                setResponsePage(ProjectsPage.class, null);
                setRedirect(true);
            }
        }
        add(new ListView<Project>("projects", new ProjectsListModel()) {
            @Override
            protected void populateItem(ListItem<Project> item) {
                Project project = item.getModelObject();
                Link<Void> link = new BookmarkablePageLink<Void>("link", ProjectsPage.class, WicketUtils.indexed(project.getKey()));
                item.add(link.add(new Label("name", project.getName())));
            }
        });
    }

}
