package greenhouse.ui.wicket.page.history;

import greenhouse.execute.Execution;
import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ExecutionState;
import greenhouse.execute.ScenarioExecutor;
import greenhouse.ui.wicket.WicketUtils;
import greenhouse.ui.wicket.page.BaseProjectPage;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.wicketstuff.annotation.mount.MountPath;

import com.google.common.collect.Lists;

@MountPath("/projects/${project}/history/#{execution}/#{format}")
public class HistoryPage extends BaseProjectPage {

    @SpringBean
    private ScenarioExecutor executor;

    public static PageParameters paramsFor(String projectKey, int execution) {
        return new PageParameters().add("project", projectKey).add("execution", execution);
    }

    public static PageParameters paramsFor(String projectKey, int execution, String format) {
        return new PageParameters().add("project", projectKey).add("execution", execution).add("format", format);
    }

    public HistoryPage(PageParameters params) {
        super(params);
        final String projectKey = getProjectKey();

        int executionNumber = params.get("execution").toInt(0);
        String format = params.get("format").toString("");

        final Fragment body;
        if (executionNumber > 0) {
            if ("".equals(format)) {
                body = details(projectKey, executionNumber);
            } else {
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
        } else {
            body = table(projectKey);
        }
        add(body);
    }

    private Fragment details(String projectKey, int executionNumber) {
        Fragment body = new Fragment("body", "details", this);
        final ExecutionKey executionKey = new ExecutionKey(projectKey, executionNumber);
        Execution execution = executor.getExecution(executionKey);

        final Label output = new Label("output", new FilteringModel());
        output.setOutputMarkupId(true);
        output.setDefaultModelObject(execution.getOutput());

        if (execution.getState() != ExecutionState.COMPLETE) {
            output.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
                @Override
                protected void onTimer(AjaxRequestTarget target) {
                    Execution execution = executor.getExecution(executionKey);
                    output.setDefaultModelObject(execution.getOutput());
                    if (execution.getState() == ExecutionState.COMPLETE) {
                        stop(target);
                    }
                    WicketUtils.addComponents(target, output);
                }
            });
        }

        body.add(output);

        return body;
    }

    private Fragment table(final String projectKey) {
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

        Fragment body = new Fragment("body", "table", this);
        body.add(new ListView<Execution>("history", model) {
            @Override
            protected void populateItem(ListItem<Execution> item) {
                Execution execution = item.getModelObject();
                int number = execution.getKey().getNumber();
                ExecutionState state = execution.getState();

                BookmarkablePageLink<Void> detailsLink = new BookmarkablePageLink<Void>("detailsLink", HistoryPage.class, paramsFor(projectKey, number));
                detailsLink.add(new Label("number", Integer.toString(number)));
                item.add(detailsLink);

                item.add(new Label("state", Model.of(state)));
                item.add(new Label("start", Model.of(new Date(execution.getStart()))));

                long end = execution.getEnd();
                item.add(new Label("end", end == 0 ? Model.of("-") : Model.of(new Date(end))));
                item.add(new Label("environment", execution.getContext().getName()));
                item.add(new Label("type", Model.of(execution.getType())));
                item.add(new Label("details", execution.getDescription()));
                item.add(new BookmarkablePageLink<Void>("output", HistoryPage.class, paramsFor(projectKey, number, "output")));
                item.add(new BookmarkablePageLink<Void>("report", HistoryPage.class, paramsFor(projectKey, number, "report"))
                        .setEnabled(state == ExecutionState.COMPLETE));
            }
        });
        return body;
    }

    private static class FilteringModel extends Model<String> {
        private String text = "";

        public FilteringModel() {
            reset();
        }

        @Override
        public void setObject(final String object) {
            String filtered = object;
            int index = Math.max(filtered.lastIndexOf("--- cuke4duke-maven-plugin"), filtered.lastIndexOf("RunCukesTest"));
            if (index == -1) {
                text += ".";
            } else {
                filtered = filtered.substring(filtered.indexOf('\n', index));
                filtered = filtered.replaceAll("\\[INFO\\] ", "");
                if ("".equals(StringUtils.trimWhitespace(filtered))) {
                    text += ".";
                } else {
                    text = filtered;
                }
            }
        }

        @Override
        public String getObject() {
            return text;
        }

        public void reset() {
            text = "Loading Maven and Cucumber.";
        }
    }

}
