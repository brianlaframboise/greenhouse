package greenhouse.ui.wicket.page;

import greenhouse.execute.Execution;
import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ExecutionState;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.ui.wicket.WicketUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.web.util.HtmlUtils;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.collect.Lists;

@MountIndexedParam
public class HistoryPage extends GreenhousePage {

    @SpringBean
    private ScenarioExecutor executor;

    public HistoryPage(PageParameters params) {
        super(params);
        final String projectKey = getProjectKey();

        int executionNumber = params.getAsInteger("2", 0);
        String format = params.getString("3", "");

        if (executionNumber > 0) {
            ExecutionKey executionKey = new ExecutionKey(projectKey, executionNumber);
            Execution execution = executor.getExecution(executionKey);
            String content;
            if ("report".equals(format)) {
                content = execution.getReportHtml();
            } else {
                content = "<pre>" + HtmlUtils.htmlEscape(execution.getOutput()) + "</pre>";
            }

            throw new RestartResponseException(new HtmlPage(content));
        }

        IModel<List<Execution>> model = new LoadableDetachableModel<List<Execution>>() {
            @Override
            protected List<Execution> load() {
                List<Execution> executions = Lists.newArrayList();
                for (Execution execution : executor.getAllExecutions()) {
                    if (execution.getKey().getProjectKey().equals(projectKey)) {
                        executions.add(execution);
                    }
                }

                Collections.sort(executions, new Comparator<Execution>() {
                    @Override
                    public int compare(Execution first, Execution second) {
                        return Integer.valueOf(second.getKey().getNumber()).compareTo(first.getKey().getNumber());
                    }
                });

                return executions;
            }
        };

        add(new ListView<Execution>("history", model) {
            @Override
            protected void populateItem(ListItem<Execution> item) {
                Execution execution = item.getModelObject();
                int number = execution.getKey().getNumber();
                ExecutionState state = execution.getState();
                item.add(new Label("number", Integer.toString(number)));

                item.add(new Label("state", state.name()));
                item.add(new Label("start", Model.of(new Date(execution.getStart()))));
                long end = execution.getEnd();
                item.add(new Label("end", end == 0 ? Model.of("-") : Model.of(new Date(end))));
                item.add(new Label("type", Model.of(execution.getType().name())));
                item.add(new Label("details", execution.getDetails()));
                item.add(new BookmarkablePageLink<Void>("output", ProjectsPage.class, WicketUtils.indexed(projectKey, "history", number, "output")));
                item.add(new BookmarkablePageLink<Void>("report", ProjectsPage.class, WicketUtils.indexed(projectKey, "history", number, "report"))
                        .setEnabled(state == ExecutionState.COMPLETE));
            }
        });
    }

}
