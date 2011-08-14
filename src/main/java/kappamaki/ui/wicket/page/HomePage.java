package kappamaki.ui.wicket.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

public class HomePage extends WebPage {

    @SpringBean
    private Index index;

    @SpringBean
    private ScenarioExecutor executor;

    public HomePage() {
        final Multiset<String> tags = index.tags();

        List<String> sortedTags = new ArrayList<String>(tags.elementSet());
        Collections.sort(sortedTags);

        add(new ListView<String>("tags", sortedTags) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String tag = item.getModelObject();
                item.add(new Label("tag", tag));
                item.add(new Label("count", Model.of(tags.count(tag))));
            }
        });

        ImmutableSet<IndexedScenario> all = index.all();
        ArrayList<String> names = new ArrayList<String>();
        for (IndexedScenario scenario : all) {
            names.add(scenario.getName());
        }
        Collections.sort(names);

        final Label output = new Label("output", "");
        add(output.setOutputMarkupId(true));

        final WebMarkupContainer scenario = new WebMarkupContainer("scenario");
        scenario.add(new Label("name", ""));
        scenario.add(new WebMarkupContainer("execute"));
        scenario.add(new WebMarkupContainer("lines"));
        add(scenario.setVisible(false).setOutputMarkupPlaceholderTag(true));

        add(new ListView<String>("scenarios", names) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final String name = item.getModelObject();
                AjaxFallbackLink<Void> link = new IndicatingAjaxFallbackLink<Void>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        scenario.replace(new Label("name", name));
                        scenario.replace(new IndicatingAjaxFallbackLink<Void>("execute") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                IndexedScenario scenario = index.findByName(name);
                                String result = filter(executor.execute(scenario));
                                output.setDefaultModelObject(result);
                                if (target != null) {
                                    target.addComponent(output);
                                }
                            }
                        });
                        IndexedScenario indexedScenario = index.findByName(name);
                        String gherkin = Utils.readGherkin(indexedScenario.getUri());
                        Iterable<String> gherkinLines = Splitter.on('\n').split(gherkin);
                        scenario.replace(new ListView<String>(("lines"), ImmutableList.copyOf(gherkinLines)) {
                            @Override
                            protected void populateItem(ListItem<String> item) {
                                final int lineNumber = item.getIndex() + 1;
                                Label text = new Label("text", item.getModelObject());
                                text.setRenderBodyOnly(true);

                                Fragment context;
                                boolean isExample = item.getModelObject().contains("|") && !getModelObject().get(item.getIndex() - 1).contains("Examples");
                                if (isExample) {
                                    context = new Fragment("context", "example", this);
                                    context.add(new IndicatingAjaxFallbackLink<Void>("execute") {
                                        @Override
                                        public void onClick(AjaxRequestTarget target) {
                                            IndexedScenario scenario = index.findByName(name);
                                            String result = filter(executor.executeExample(scenario, lineNumber));
                                            output.setDefaultModelObject(result);
                                            if (target != null) {
                                                target.addComponent(output);
                                            }
                                        }
                                    }.add(text));
                                } else {
                                    context = new Fragment("context", "plain", this);
                                    context.add(text);
                                }
                                WebMarkupContainer line = new WebMarkupContainer("line");
                                line.add(context);
                                item.add(line);
                            }
                        });
                        if (target != null) {
                            target.addComponent(scenario.setVisible(true));
                        }
                    }
                };
                item.add(link);
                link.add(new Label("name", name));
            }
        });
    }

    private String filter(String output) {
        String remove = "kappamaki-example ---";
        String filtered = output;
        int index = output.lastIndexOf(remove);
        filtered = filtered.substring(index + remove.length() + 1);
        filtered = filtered.replaceAll("\\[INFO\\] ", "");
        return filtered;
    }
}
