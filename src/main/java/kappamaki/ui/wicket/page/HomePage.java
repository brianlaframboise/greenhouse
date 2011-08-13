package kappamaki.ui.wicket.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;
import kappamaki.index.IndexedScenario;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

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

        add(new ListView<String>("scenarios", names) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final String name = item.getModelObject();
                AjaxFallbackLink<Void> link = new IndicatingAjaxFallbackLink<Void>(
                        "link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        IndexedScenario scenario = index.findByName(name);
                        String result = executor.execute(scenario);
                        String remove = "kappamaki-example ---";
                        int index = result.lastIndexOf(remove);
                        result = result.substring(index + remove.length() + 1);
                        result = result.replaceAll("\\[INFO\\] ", "");

                        output.setDefaultModelObject(result);
                        if (target != null) {
                            target.addComponent(output);
                        }
                    }
                };
                item.add(link);
                link.add(new Label("name", name));
            }
        });
    }
}
