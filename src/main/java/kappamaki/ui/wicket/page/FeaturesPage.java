package kappamaki.ui.wicket.page;

import java.util.ArrayList;

import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Displays and executes Features, Scenarios, and Examples.
 */
@MountPath(path = "/features")
@MountIndexedParam
public class FeaturesPage extends KappamakiPage {

    @SpringBean
    private Index index;

    private final Label output;

    private final WebMarkupContainer feature;

    public FeaturesPage(PageParameters params) {
        ImmutableList<IndexedFeature> features = index.features();
        ArrayList<String> names = new ArrayList<String>();
        for (IndexedFeature feature : features) {
            names.add(feature.getName());
        }

        output = new Label("output", new FilteringModel());
        add(output.setOutputMarkupId(true));

        feature = new WebMarkupContainer("feature");
        feature.add(new WebMarkupContainer("execute").add(new Label("name", "")));
        feature.add(new WebMarkupContainer("lines"));
        add(feature.setVisible(false).setOutputMarkupPlaceholderTag(true));

        String featureNameArg = params.getString("0", "");
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
        ExecuteFeatureLink executeFeatureLink = new ExecuteFeatureLink("execute", output, name);
        executeFeatureLink.add(new Label("name", name));
        feature.replace(executeFeatureLink);

        IndexedFeature indexedFeature = index.featureByName(name);
        String gherkin = Utils.readGherkin(indexedFeature.getUri());
        Iterable<String> gherkinLines = Splitter.on('\n').split(gherkin);

        feature.replace(new ListView<String>(("lines"), ImmutableList.copyOf(gherkinLines)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String line = item.getModelObject();
                final int lineNumber = item.getIndex() + 1;
                Label text = new Label("text", item.getModelObject());
                text.setRenderBodyOnly(true);

                Fragment context;
                if (line.contains("|") && !getModelObject().get(item.getIndex() - 1).contains("Examples")) {
                    // Example
                    context = new Fragment("context", "example", this);

                    int pipeIndex = line.indexOf('|');
                    Label pretext = new Label("pretext", line.substring(0, pipeIndex));

                    IndexedFeature feature = index.featureByName(name);
                    IndexedScenario indexedScenario = index.scenarioByLine(feature, lineNumber);
                    ExecuteExampleLink executeExampleLink = new ExecuteExampleLink("execute", output, indexedScenario.getName(), lineNumber);
                    executeExampleLink.add(new Label("text", line.substring(pipeIndex)));

                    context.add(pretext, executeExampleLink);
                } else if (line.contains("Scenario")) {
                    // Scenario or Scenario Outline
                    context = new Fragment("context", "scenario", this);

                    int colonIndex = line.indexOf(": ");
                    Label pretext = new Label("pretext", line.substring(0, colonIndex + 2));
                    String scenarioName = line.substring(colonIndex + 2).trim();

                    ExecuteScenarioLink executeScenarioLink = new ExecuteScenarioLink("execute", output, scenarioName);
                    executeScenarioLink.add(new Label("name", scenarioName));

                    context.add(pretext, executeScenarioLink);
                } else {
                    context = new Fragment("context", "plain", this);
                    context.add(text);
                }
                item.add(new WebMarkupContainer("pre").add(context));
            }
        }.setRenderBodyOnly(true));
        feature.setVisible(true);
        if (target != null) {
            target.addComponent(feature);
        }
    }

    private static class FilteringModel extends Model<String> {
        private String text = "";

        @Override
        public void setObject(String object) {
            String filtered = object;
            String remove = "kappamaki-example ---";
            int index = object.lastIndexOf(remove);
            filtered = filtered.substring(index + remove.length() + 1);
            filtered = filtered.replaceAll("\\[INFO\\] ", "");
            text = filtered;
        }

        @Override
        public String getObject() {
            return text;
        }
    }

    private abstract static class ExecutingLink extends IndicatingAjaxFallbackLink<Void> {
        @SpringBean
        protected Index index;

        @SpringBean
        protected ScenarioExecutor executor;

        private final Label output;

        public ExecutingLink(String id, Label output) {
            super(id);
            this.output = output;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            String result = execute();
            output.setDefaultModelObject(result);
            if (target != null) {
                target.addComponent(output);
            }
        }

        protected abstract String execute();

    }

    private static class ExecuteFeatureLink extends ExecutingLink {

        private final String name;

        public ExecuteFeatureLink(String id, Label output, String name) {
            super(id, output);
            this.name = name;
        }

        @Override
        protected String execute() {
            IndexedFeature feature = index.featureByName(name);
            return executor.execute(feature);
        }
    }

    private static class ExecuteScenarioLink extends ExecutingLink {

        private final String name;

        public ExecuteScenarioLink(String id, Label output, String name) {
            super(id, output);
            this.name = name;
        }

        @Override
        protected String execute() {
            IndexedScenario scenario = index.scenarioByName(name);
            return executor.execute(scenario);
        }
    }

    private static class ExecuteExampleLink extends ExecutingLink {

        private final String name;
        private final int line;

        public ExecuteExampleLink(String id, Label output, String name, int line) {
            super(id, output);
            this.name = name;
            this.line = line;
        }

        @Override
        protected String execute() {
            IndexedScenario scenario = index.scenarioByName(name);
            return executor.executeExample(scenario, line);
        }

    }

}
