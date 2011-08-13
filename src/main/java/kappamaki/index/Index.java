package kappamaki.index;

import java.io.File;
import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

public interface Index {

    ImmutableSet<IndexedScenario> all();

    ImmutableSet<IndexedScenario> findByTag(String tag);

    Collection<IndexedScenario> findByUri(String uri);

    IndexedScenario findByName(String name);

    Multiset<String> tags();

    File getFeaturesRoot();
}
