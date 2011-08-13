package kappamaki.index;

import java.io.File;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class InMemoryIndex implements Index {

    private final File featuresRoot;
    private final ImmutableSet<IndexedScenario> all;
    private final Multimap<String, IndexedScenario> tagged;
    private final Multimap<String, IndexedScenario> byUri;

    public InMemoryIndex() {
        this(null, null, null, null);
    }

    public InMemoryIndex(File featuresRoot, ImmutableSet<IndexedScenario> all,
            Multimap<String, IndexedScenario> tagged,
            Multimap<String, IndexedScenario> byUri) {
        this.featuresRoot = featuresRoot;
        this.all = all;
        this.tagged = tagged;
        this.byUri = byUri;
    }

    public File getFeaturesRoot() {
        return featuresRoot;
    }

    public Multiset<String> tags() {
        return tagged.keys();
    }

    @Override
    public ImmutableSet<IndexedScenario> findByTag(String tag) {
        return ImmutableSet.copyOf(tagged.get(tag));
    }

    @Override
    public Collection<IndexedScenario> findByUri(String uri) {
        return byUri.get(uri);
    }

    @Override
    public ImmutableSet<IndexedScenario> all() {
        return all;
    }

    @Override
    public IndexedScenario findByName(String name) {
        for (IndexedScenario scenario : all) {
            if (scenario.getName().equals(name)) {
                return scenario;
            }
        }
        return null;

    }

}
