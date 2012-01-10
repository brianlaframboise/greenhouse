package greenhouse.ui.wicket.page;

import greenhouse.project.Project;
import greenhouse.ui.wicket.WicketUtils;

import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

@MountIndexedParam
public class ProjectPage extends GreenhousePage {

    public ProjectPage(PageParameters params) {
        super(params);
        Project project = project();
        add(new Label("name", project.getName()));
        add(newPageLink(project, FeaturesPage.class));
        add(newPageLink(project, TagsPage.class));
        add(newPageLink(project, CreatePage.class));
        add(newPageLink(project, HistoryPage.class));
    }

    public static BookmarkablePageLink<Void> newPageLink(Project project, Class<? extends GreenhousePage> clazz) {
        String simpleName = clazz.getSimpleName();
        String name = simpleName.substring(0, simpleName.indexOf("Page")).toLowerCase(Locale.ENGLISH);
        return new BookmarkablePageLink<Void>(name, ProjectsPage.class, WicketUtils.indexed(project.getKey(), name));
    }
}
