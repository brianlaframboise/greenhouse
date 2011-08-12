package kappamaki.index;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class Index {

    private final ImmutableSet<IndexedScenario> all;
    private final Multimap<String, IndexedScenario> tagged;

    public Index(ImmutableSet<IndexedScenario> all,
            Multimap<String, IndexedScenario> tagged) {
        this.all = all;
        this.tagged = tagged;
    }

    public ImmutableSet<IndexedScenario> findByTag(String tag) {
        return ImmutableSet.copyOf(tagged.get(tag));
    }

}
