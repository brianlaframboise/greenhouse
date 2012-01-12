package greenhouse.ui.wicket.page;

import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.project.ProjectRepository;
import greenhouse.ui.wicket.WicketUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Provides common layout for all Project-specific pages.
 */
abstract class BaseProjectPage extends GreenhousePage {

    private static final IChoiceRenderer<Context> CONTEXT_RENDERER = new IChoiceRenderer<Context>() {
        @Override
        public Object getDisplayValue(Context context) {
            return context.getName();
        }

        @Override
        public String getIdValue(Context context, int index) {
            return context.getKey();
        }
    };

    public BaseProjectPage(PageParameters params) {
        super(params);
        WebMarkupContainer base = new WebMarkupContainer("base");
        Project project = project();
        base.add(new Label("name", project.getName()));

        base.add(newPageLink(project, FeaturesPage.class));
        base.add(newPageLink(project, TagsPage.class));
        base.add(newPageLink(project, CreatePage.class));
        base.add(newPageLink(project, HistoryPage.class));

        final String projectKey = getProjectKey();
        base.add(new DropDownChoice<Context>("commands", Model.of(getCurrentContext(project)), new CommandsModel(repo, getProjectKey()), CONTEXT_RENDERER) {
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @Override
            protected void onSelectionChanged(Context newContext) {
                WicketUtils.addContextKey(getWebRequest(), (WebResponse) getResponse(), repo.getProject(projectKey), newContext.getKey());
            }

            @Override
            public boolean isNullValid() {
                return false;
            }
        });
        add(base);
    }

    private Context getCurrentContext(Project project) {
        String contextKey = WicketUtils.getContextKey((WebRequest) getRequest(), project);
        ImmutableMap<String, Context> contexts = project.getContexts();
        Context context = contexts.get(contextKey);
        if (contextKey == null) {
            context = contexts.values().iterator().next();
            WicketUtils.addContextKey((WebRequest) getRequest(), (WebResponse) getResponse(), project, context.getKey());
        }
        return context;
    }

    public static BookmarkablePageLink<Void> newPageLink(Project project, Class<? extends GreenhousePage> clazz) {
        String simpleName = clazz.getSimpleName();
        String name = simpleName.substring(0, simpleName.indexOf("Page")).toLowerCase(Locale.ENGLISH);
        return new BookmarkablePageLink<Void>(name, ProjectsPage.class, WicketUtils.indexed(project.getKey(), name));
    }

    private static class CommandsModel extends LoadableDetachableModel<List<Context>> {
        private final ProjectRepository repo;
        private final String projectKey;

        private CommandsModel(ProjectRepository repo, String projectKey) {
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
