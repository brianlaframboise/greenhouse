package kappamaki.index;

import java.io.File;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

public interface Index {

    // Features

    File getFeaturesRoot();

    /**
     * All features, ordered by feature name ascending
     * 
     * @return list of indexed features
     */
    ImmutableList<IndexedFeature> features();

    IndexedFeature featureByName(String name);

    IndexedFeature findByScenario(IndexedScenario scenario);

    // Scenarios

    IndexedScenario scenarioByName(String name);

    IndexedScenario scenarioByLine(IndexedFeature feature, int line);

    ImmutableSet<IndexedScenario> findByTag(String tag);

    // Tags

    Multiset<String> tags();

    // Steps

    ImmutableSet<StepMethod> steps();

}
