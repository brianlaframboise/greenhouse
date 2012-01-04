package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;

import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@MountPath(path = "/projects")
@MountIndexedParam
public class ProjectsPage extends GreenhousePage {

    private final class ProjectsListModel extends AbstractReadOnlyModel<List<Project>> {
        @Override
        public List<Project> getObject() {
            return ImmutableList.copyOf(projects.getProjects().values());
        }
    }

    @SpringBean
    private ProjectRepository projects;

    public ProjectsPage(PageParameters params) {
        if (params != null) {
            String key = params.getString("0", "");
            Project project = projects.getProjects().get(key);
            if (project != null) {
                String page = params.getString("1", "");
                if (page.equals("tags")) {
                    setResponsePage(TagsPage.class, params);
                } else if (page.equals("create")) {
                    setResponsePage(StepsPage.class, params);
                } else {
                    setResponsePage(FeaturesPage.class, params);
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
                item.add(new Label("name", project.getName()));
                item.add(newPageLink(project, "features"));
                item.add(newPageLink(project, "tags"));
                item.add(newPageLink(project, "create"));
            }
        });
    }

    public BookmarkablePageLink<Void> newPageLink(Project project, String name) {
        return new BookmarkablePageLink<Void>(name, ProjectsPage.class, indexed(project.getKey(), name));
    }

    public static PageParameters indexed(String... args) {
        Map<String, String> params = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            params.put(Integer.toString(i), args[i]);
        }
        return new PageParameters(params);
    }
}
