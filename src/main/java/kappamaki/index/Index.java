package kappamaki.index;

import java.io.File;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class Index {

    private final File featuresRoot;
    private final ImmutableSet<IndexedScenario> all;
    private final Multimap<String, IndexedScenario> tagged;
    private final Multimap<String, IndexedScenario> byUri;

    public Index(File featuresRoot, ImmutableSet<IndexedScenario> all,
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

    public ImmutableSet<IndexedScenario> findByTag(String tag) {
        return ImmutableSet.copyOf(tagged.get(tag));
    }

    public Collection<IndexedScenario> findByUri(String uri) {
        return byUri.get(uri);
    }

}
