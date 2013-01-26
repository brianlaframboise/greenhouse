package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.ui.wicket.WicketUtils;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.google.common.collect.ImmutableList;

@MountPath("/projects")
public class ProjectsPage extends GreenhousePage {

    private final class ProjectsListModel extends AbstractReadOnlyModel<List<Project>> {
        @Override
        public List<Project> getObject() {
            return ImmutableList.copyOf(repo.getAllProjects());
        }
    }

    public ProjectsPage(PageParameters params) {
        super(params);
        if (params != null) {
            String key = params.get("0").toString("");
            Project project = repo.getProject(key);
            if (project != null) {
                String page = params.get("1").toString("");
                if (page.equals("features")) {
                    setResponsePage(FeaturesPage.class, params);
                } else if (page.equals("tags")) {
                    setResponsePage(TagsPage.class, params);
                } else if (page.equals("create")) {
                    setResponsePage(CreatePage.class, params);
                } else if (page.equals("history")) {
                    setResponsePage(HistoryPage.class, params);
                } else if (page.equals("settings")) {
                    setResponsePage(SettingsPage.class, params);
                } else if (page.equals("update")) {
                    setResponsePage(UpdatePage.class, params);
                } else {
                    setResponsePage(ProjectPage.class, params);
                }
            } else if (!"".equals(key)) {
                setResponsePage(ProjectsPage.class, null);
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
