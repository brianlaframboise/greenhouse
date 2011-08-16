package kappamaki.ui.wicket.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kappamaki.index.Index;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

/**
 * Displays tags and their associated Features and Scenarios.
 */
@MountPath(path = "/tags")
public class TagsPage extends KappamakiPage {

    @SpringBean
    private Index index;

    public TagsPage() {
        final Multiset<String> tags = index.tags();

        List<CountedTag> countedTags = new ArrayList<CountedTag>();
        for (String tag : tags.elementSet()) {
            countedTags.add(new CountedTag(tag, tags.count(tag)));
        }

        Collections.sort(countedTags, new Comparator<CountedTag>() {
            @Override
            public int compare(CountedTag first, CountedTag second) {
                int result = second.count.compareTo(first.count);
                if (result == 0) {
                    result = first.name.compareTo(second.name);
                }
                return result;
            }
        });

        final WebMarkupContainer matches = new WebMarkupContainer("matches");
        add(matches.setOutputMarkupId(true));
        final ListView<IndexedScenario> scenarios = new ListView<IndexedScenario>("scenarios", new ArrayList<IndexedScenario>()) {
            @Override
            protected void populateItem(ListItem<IndexedScenario> item) {
                IndexedScenario scenario = item.getModelObject();
                IndexedFeature feature = index.findByScenario(scenario);
                BookmarkablePageLink<Void> view = new BookmarkablePageLink<Void>("view", FeaturesPage.class, new PageParameters("0=" + feature.getName()));
                view.add(new Label("featureScenario", feature.getName() + " > " + scenario.getName()));
                item.add(view);
            }
        };
        matches.add(scenarios);

        add(new ListView<CountedTag>("tags", countedTags) {
            @Override
            protected void populateItem(ListItem<CountedTag> item) {
                CountedTag tag = item.getModelObject();
                final String tagName = tag.name;
                AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ImmutableSet<IndexedScenario> taggedScenarios = index.findByTag(tagName);
                        scenarios.setModelObject(taggedScenarios.asList());
                        if (target != null) {
                            target.addComponent(matches);
                        }

                    }
                };
                item.add(link.add(new Label("tag", tag.name)));
                item.add(new Label("count", Model.of(tag.count)));
            }
        });
    }

    private static class CountedTag {
        public final String name;
        public final Integer count;

        public CountedTag(String name, int count) {
            this.name = name;
            this.count = count;
        }

    }
}