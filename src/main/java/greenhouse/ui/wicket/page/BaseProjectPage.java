package greenhouse.ui.wicket.page;

import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;
import greenhouse.ui.wicket.WicketUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Provides common layout for all Project-specific pages.
 */
abstract class BaseProjectPage extends GreenhousePage {

    @SuppressWarnings("unchecked")
    public BaseProjectPage(PageParameters params) {
        super(params);
        WebMarkupContainer base = new WebMarkupContainer("base");
        Project project = project();
        final String projectKey = getProjectKey();
        Link<Void> projectLink = new BookmarkablePageLink<Void>("project", ProjectsPage.class, WicketUtils.indexed(projectKey));
        base.add(projectLink.add(new Label("name", project.getName())));

        base.add(new ListView<Class<? extends GreenhousePage>>("pages", Arrays.asList(FeaturesPage.class, TagsPage.class, CreatePage.class, HistoryPage.class,
                SettingsPage.class)) {
            @Override
            protected void populateItem(ListItem<Class<? extends GreenhousePage>> item) {
                Class<? extends GreenhousePage> clazz = item.getModelObject();
                if (BaseProjectPage.this.getClass().equals(clazz)) {
                    item.add(new SimpleAttributeModifier("class", "active"));
                }
                String simpleName = simpleName(clazz);
                BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", ProjectsPage.class, params(projectKey, simpleName));
                link.add(new Label("name", StringUtils.capitalize(simpleName)));
                item.add(link);
            }
        });
        base.add(new Label("currentContext", getCurrentContext(project()).getName()));

        base.add(new ListView<Context>("contexts", new ContextsModel(repo, projectKey)) {
            @Override
            protected void populateItem(ListItem<Context> item) {
                Context context = item.getModelObject();
                final String contextKey = context.getKey();
                Link<Void> link = new Link<Void>("link") {
                    @Override
                    public void onClick() {
                        WicketUtils.setContextKey(getWebRequest(), (WebResponse) getResponse(), repo.getProject(projectKey), contextKey);
                        setResponsePage(ProjectsPage.class, params(projectKey, simpleName(BaseProjectPage.this.getClass())));
                        setRedirect(true);
                    }
                };
                item.add(link.add(new Label("name", context.getName())));
            }
        });
        add(base);

        get("home").setVisible(false);
    }

    protected BookmarkablePageLink<Void> pageLink(String id, Class<? extends BaseProjectPage> clazz, Object... params) {
        PageParameters pageParams = new PageParameters();
        pageParams.add("0", getProjectKey());
        pageParams.add("1", simpleName(clazz));
        for (int i = 0; i < params.length; i++) {
            pageParams.add(Integer.toString(i + 2), params[i].toString());
        }
        return new BookmarkablePageLink<Void>(id, ProjectsPage.class, pageParams);
    }

    private Context getCurrentContext(Project project) {
        String contextKey = WicketUtils.getContextKey((WebRequest) getRequest(), project);
        ImmutableMap<String, Context> contexts = project.getContexts();
        Context context = contexts.get(contextKey);
        if (contextKey == null) {
            context = contexts.values().iterator().next();
            WicketUtils.setContextKey((WebRequest) getRequest(), (WebResponse) getResponse(), project, context.getKey());
        }
        return context;
    }

    public BookmarkablePageLink<Void> newPageLink(Project project, Class<? extends GreenhousePage> clazz) {
        String name = simpleName(clazz);
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(name, ProjectsPage.class, params(project.getKey(), name));
        if (getClass().equals(clazz)) {
            link.add(new SimpleAttributeModifier("class", "active"));
        }
        return link;
    }

    public static String simpleName(Class<? extends GreenhousePage> clazz) {
        String simpleName = clazz.getSimpleName();
        return simpleName.substring(0, simpleName.indexOf("Page")).toLowerCase(Locale.ENGLISH);
    }

    private static PageParameters params(String projectKey, String simpleClassName) {
        return WicketUtils.indexed(projectKey, simpleClassName);

    }

    private static class ContextsModel extends LoadableDetachableModel<List<Context>> {
        private final ProjectRepository repo;
        private final String projectKey;

        private ContextsModel(ProjectRepository repo, String projectKey) {
            this.repo = repo;
            this.projectKey = projectKey;
        }

        @Override
        protected List<Context> load() {
            Project project = repo.getProject(projectKey);
            List<Context> keys = Lists.newArrayList(project.getContexts().values());
            Collections.sort(keys, new Comparator<Context>() {
                @Override
                public int compare(Context first, Context second) {
                    return first.getName().compareTo(second.getName());
                }
            });
            return keys;
        }
    }

}
