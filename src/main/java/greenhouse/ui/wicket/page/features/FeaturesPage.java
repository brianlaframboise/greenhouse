package greenhouse.ui.wicket.page.features;

import greenhouse.execute.ExecutionKey;
import greenhouse.execute.ExecutionRequest;
import greenhouse.index.Index;
import greenhouse.index.IndexedFeature;
import greenhouse.index.IndexedScenario;
import greenhouse.ui.wicket.link.ExecuteGherkinLink;
import greenhouse.ui.wicket.link.ExecutingLink;
import greenhouse.ui.wicket.page.BaseProjectPage;
import greenhouse.ui.wicket.page.tags.TagsPage.ExecuteTagLink;
import greenhouse.util.Utils;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.util.StringUtils;
import org.wicketstuff.annotation.mount.MountPath;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Displays and executes Features, Scenarios, and Examples.
 */
@MountPath("/projects/${project}/features/#{feature}")
public class FeaturesPage extends BaseProjectPage {

    private final String projectKey;

    private final WebMarkupContainer feature;

    private final Form<Void> editForm;
    private final TextArea<String> gherkinArea;

    public FeaturesPage(PageParameters params) {
        super(params);
        projectKey = getProjectKey();

        add(new ExecuteTagLink("executeAll", projectKey, "~@ignore"));

        ImmutableList<IndexedFeature> features = index().features();
        List<String> names = Lists.newArrayList();
        for (IndexedFeature feature : features) {
            names.add(feature.getName());
        }
        Collections.sort(names);

        editForm = new Form<Void>("editForm");
        add(editForm.setVisible(false).setOutputMarkupPlaceholderTag(true));
        gherkinArea = new TextArea<String>("gherkin", Model.of(""));
        gherkinArea.setEscapeModelStrings(false);
        gherkinArea.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });

        feature = new WebMarkupContainer("feature");
        feature.add(new WebMarkupContainer("lines"));
        add(feature.setVisible(false).setOutputMarkupPlaceholderTag(true));
        feature.add(new WebMarkupContainer("edit"));

        editForm.add(gherkinArea);
        editForm.add(new ExecuteGherkinLink("run", projectKey, gherkinArea.getModel()));
        editForm.add(new AjaxFallbackLink<Void>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(editForm.setVisible(false), feature.setVisible(true));
            }
        });

        String featureNameArg = params.get("feature").toString("");
        if (names.contains(featureNameArg)) {
            showFeature(featureNameArg, null);
        }

        add(new ListView<String>("features", names) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final String name = item.getModelObject();
                AjaxFallbackLink<Void> link = new IndicatingAjaxFallbackLink<Void>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        showFeature(name, target);
                    }
                };
                item.add(link);
                link.add(new Label("name", name));
            }
        });
    }

    private void showFeature(final String name, AjaxRequestTarget target) {
        IndexedFeature indexedFeature = index().featureByName(name);
        final String gherkin = Utils.readContents(indexedFeature.getUri());

        feature.replace(new AjaxFallbackLink<Void>("edit") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(feature.setVisible(false), editForm.setVisible(true));
                gherkinArea.setDefaultModelObject(gherkin);
            }
        });

        Iterable<String> gherkinLines = Splitter.on('\n').split(gherkin);

        feature.replace(new ListView<String>(("lines"), ImmutableList.copyOf(gherkinLines)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String line = item.getModelObject();
                final int lineNumber = item.getIndex() + 1;
                Label text = new Label("text", item.getModelObject());
                text.setRenderBodyOnly(true);

                Fragment context;
                if (line.startsWith("Feature")) {
                    context = new Fragment("context", "featureFragment", FeaturesPage.this);

                    int colonIndex = line.indexOf(": ");
                    Label pretext = new Label("pretext", line.substring(0, colonIndex + 2));
                    String featureName = line.substring(colonIndex + 2).trim();

                    ExecuteFeatureLink executeFeatureLink = new ExecuteFeatureLink("execute", projectKey, featureName);
                    executeFeatureLink.add(new Label("name", featureName));

                    context.add(pretext, executeFeatureLink);
                } else if (line.contains("Scenario")) {
                    // Scenario or Scenario Outline
                    context = new Fragment("context", "scenario", FeaturesPage.this);

                    int colonIndex = line.indexOf(": ");
                    Label pretext = new Label("pretext", line.substring(0, colonIndex + 2));
                    String scenarioName = line.substring(colonIndex + 2).trim();

                    ExecuteScenarioLink executeScenarioLink = new ExecuteScenarioLink("execute", projectKey, scenarioName);
                    executeScenarioLink.add(new Label("name", scenarioName));

                    context.add(pretext, executeScenarioLink);
                } else if (isExample(line, item.getIndex(), getModelObject())) {
                    // Example
                    context = new Fragment("context", "example", FeaturesPage.this);

                    int pipeIndex = line.indexOf('|');
                    Label pretext = new Label("pretext", line.substring(0, pipeIndex));

                    Index index = index();
                    IndexedFeature feature = index.featureByName(name);
                    IndexedScenario indexedScenario = index.scenarioByLine(feature, lineNumber);
                    ExecuteExampleLink executeExampleLink = new ExecuteExampleLink("execute", projectKey, indexedScenario.getName(), lineNumber);
                    executeExampleLink.add(new Label("text", line.substring(pipeIndex)));

                    context.add(pretext, executeExampleLink);
                } else {
                    context = new Fragment("context", "plain", FeaturesPage.this);
                    context.add(text);
                }
                item.add(new WebMarkupContainer("pre").add(context));
            }

            /**
             * Determines whether or not the given line is a row in an Examples
             * block.
             * 
             * @param line The line of gherkin
             * @param lineIndex The index of the current line in allLines
             * @param allLines All of the lines of gherkin in which line exists
             * @return true iff line is an example row
             */
            private boolean isExample(String line, int lineIndex, List<String> allLines) {
                line = StringUtils.trimLeadingWhitespace(line);
                if (!line.startsWith("|")) {
                    return false;
                }
                if (line.startsWith("|")) {
                    if (allLines.get(lineIndex - 1).contains("Examples")) {
                        return false;
                    }
                    for (int i = lineIndex - 2; i >= 0; i--) {
                        String preceeding = StringUtils.trimLeadingWhitespace(allLines.get(i));
                        if (!preceeding.startsWith("|")) {
                            return preceeding.startsWith("Examples");
                        }
                    }
                }
                return false;
            }

        }.setRenderBodyOnly(true));
        feature.setVisible(true);
        editForm.setVisible(false);
        if (target != null) {
            target.add(feature, editForm);
        }
    }

    private static class ExecuteFeatureLink extends ExecutingLink {

        private final String name;

        public ExecuteFeatureLink(String id, String projectKey, String name) {
            super(id, projectKey);
            this.name = name;
        }

        @Override
        protected ExecutionKey execute() {
            return executor.execute(ExecutionRequest.feature(projectKey, context(), name));
        }
    }

    private static class ExecuteScenarioLink extends ExecutingLink {

        private final String name;

        public ExecuteScenarioLink(String id, String projectKey, String name) {
            super(id, projectKey);
            this.name = name;
        }

        @Override
        protected ExecutionKey execute() {
            return executor.execute(ExecutionRequest.scenario(projectKey, context(), name));
        }
    }

    private static class ExecuteExampleLink extends ExecutingLink {

        private final String name;
        private final int line;

        public ExecuteExampleLink(String id, String projectKey, String name, int line) {
            super(id, projectKey);
            this.name = name;
            this.line = line;
        }

        @Override
        protected ExecutionKey execute() {
            return executor.execute(ExecutionRequest.example(projectKey, context(), name, line));
        }
    }

}
