package kappamaki.ui.wicket.page;

import java.util.ArrayList;

import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
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
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.visural.wicket.component.dialog.Dialog;

/**
 * Displays and executes Features, Scenarios, and Examples.
 */
@MountPath(path = "/features")
@MountIndexedParam
public class FeaturesPage extends KappamakiPage {

    @SpringBean
    private Index index;

    private final Dialog dialog;
    private final Label output;

    private final WebMarkupContainer feature;

    public FeaturesPage(PageParameters params) {
        ImmutableList<IndexedFeature> features = index.features();
        ArrayList<String> names = new ArrayList<String>();
        for (IndexedFeature feature : features) {
            names.add(feature.getName());
        }

        dialog = new Dialog("dialog");
        output = new Label("output", new Model<String>(""));// new FilteringModel());
        add(dialog);
        dialog.add(new Label("progress", "").setOutputMarkupId(true));
        dialog.add(output.setOutputMarkupId(true));
        dialog.add(new AjaxFallbackLink<Void>("close") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });

        feature = new WebMarkupContainer("feature");
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
        IndexedFeature indexedFeature = index.featureByName(name);
        String gherkin = Utils.readContents(indexedFeature.getUri());
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
                    context = new Fragment("context", "featureFragment", this);

                    int colonIndex = line.indexOf(": ");
                    Label pretext = new Label("pretext", line.substring(0, colonIndex + 2));
                    String featureName = line.substring(colonIndex + 2).trim();

                    ExecuteFeatureLink executeFeatureLink = new ExecuteFeatureLink("execute", dialog, output, featureName);
                    executeFeatureLink.add(new Label("name", featureName));

                    context.add(pretext, executeFeatureLink);
                } else if (line.contains("Scenario")) {
                    // Scenario or Scenario Outline
                    context = new Fragment("context", "scenario", this);

                    int colonIndex = line.indexOf(": ");
                    Label pretext = new Label("pretext", line.substring(0, colonIndex + 2));
                    String scenarioName = line.substring(colonIndex + 2).trim();

                    ExecuteScenarioLink executeScenarioLink = new ExecuteScenarioLink("execute", dialog, output, scenarioName);
                    executeScenarioLink.add(new Label("name", scenarioName));

                    context.add(pretext, executeScenarioLink);
                } else if (line.contains("|") && !getModelObject().get(item.getIndex() - 1).contains("Examples")) {
                    // Example
                    context = new Fragment("context", "example", this);

                    int pipeIndex = line.indexOf('|');
                    Label pretext = new Label("pretext", line.substring(0, pipeIndex));

                    IndexedFeature feature = index.featureByName(name);
                    IndexedScenario indexedScenario = index.scenarioByLine(feature, lineNumber);
                    ExecuteExampleLink executeExampleLink = new ExecuteExampleLink("execute", dialog, output, indexedScenario.getName(), lineNumber);
                    executeExampleLink.add(new Label("text", line.substring(pipeIndex)));

                    context.add(pretext, executeExampleLink);
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

        private final Dialog dialog;
        private final Label output;

        public ExecutingLink(String id, Dialog dialog, Label output) {
            super(id);
            this.dialog = dialog;
            this.output = output;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            final int taskId = execute();
            final long startTime = System.currentTimeMillis();
            final Label progress = (Label) output.getParent().get("progress");
            output.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {

                @Override
                protected void onTimer(AjaxRequestTarget target) {
                    String outputText = "";
                    long runtime = (System.currentTimeMillis() - startTime) / 1000;
                    String prefix = "Running...";
                    if (executor.isComplete(taskId)) {
                        outputText = executor.getOutput(taskId);
                        prefix = "Complete!";
                        stop();
                    } else {
                        outputText = executor.getPartialOutput(taskId);
                    }
                    progress.setDefaultModelObject(prefix + " (" + runtime + "s)");
                    output.setDefaultModelObject(outputText);
                    if (target != null) {
                        target.addComponent(output);
                        target.addComponent(progress);
                    }
                }
            });
            if (target != null) {
                dialog.open(target);
                target.addComponent(dialog);
            }
        }

        protected abstract int execute();

    }

    private static class ExecuteFeatureLink extends ExecutingLink {

        private final String name;

        public ExecuteFeatureLink(String id, Dialog dialog, Label output, String name) {
            super(id, dialog, output);
            this.name = name;
        }

        @Override
        protected int execute() {
            IndexedFeature feature = index.featureByName(name);
            return executor.execute(feature);
        }
    }

    private static class ExecuteScenarioLink extends ExecutingLink {

        private final String name;

        public ExecuteScenarioLink(String id, Dialog dialog, Label output, String name) {
            super(id, dialog, output);
            this.name = name;
        }

        @Override
        protected int execute() {
            IndexedScenario scenario = index.scenarioByName(name);
            return executor.execute(scenario);
        }
    }

    private static class ExecuteExampleLink extends ExecutingLink {

        private final String name;
        private final int line;

        public ExecuteExampleLink(String id, Dialog dialog, Label output, String name, int line) {
            super(id, dialog, output);
            this.name = name;
            this.line = line;
        }

        @Override
        protected int execute() {
            IndexedScenario scenario = index.scenarioByName(name);
            return executor.executeExample(scenario, line);
        }

    }

}
