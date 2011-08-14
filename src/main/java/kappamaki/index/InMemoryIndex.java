package kappamaki.index;

import java.io.File;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

public class InMemoryIndex implements Index {

    private final File featuresRoot;
    private final ImmutableList<IndexedFeature> features;
    private final ImmutableMap<IndexedScenario, IndexedFeature> featuresByScenario;
    private final ImmutableMultimap<String, IndexedScenario> scenariosByTag;

    public InMemoryIndex(File featuresRoot, ImmutableList<IndexedFeature> features, ImmutableMultimap<String, IndexedScenario> scenariosByTag) {
        this.featuresRoot = featuresRoot;
        this.features = features;
        this.scenariosByTag = scenariosByTag;

        Builder<IndexedScenario, IndexedFeature> builder = ImmutableMap.<IndexedScenario, IndexedFeature> builder();
        for (IndexedFeature feature : features) {
            for (IndexedScenario scenario : feature.getScenarios()) {
                builder.put(scenario, feature);
            }
        }
        featuresByScenario = builder.build();
    }

    // Features

    @Override
    public File getFeaturesRoot() {
        return featuresRoot;
    }

    @Override
    public ImmutableList<IndexedFeature> features() {
        return features;
    }

    @Override
    public IndexedFeature featureByName(String name) {
        for (IndexedFeature feature : features) {
            if (feature.getName().equals(name)) {
                return feature;
            }
        }
        return null;
    }

    @Override
    public IndexedFeature findByScenario(IndexedScenario scenario) {
        return featuresByScenario.get(scenario);
    }

    // Scenarios

    @Override
    public IndexedScenario scenarioByName(String name) {
        for (IndexedFeature feature : features) {
            for (IndexedScenario scenario : feature.getScenarios()) {
                if (scenario.getName().equals(name)) {
                    return scenario;
                }
            }
        }
        return null;
    }

    @Override
    public IndexedScenario scenarioByLine(IndexedFeature feature, int line) {
        int nearest = -1;
        IndexedScenario preceeding = null;
        for (IndexedScenario scenario : feature.getScenarios()) {
            int scenarioLine = scenario.getLine();
            if (scenarioLine < line && scenarioLine > nearest) {
                nearest = scenarioLine;
                preceeding = scenario;
            }
        }
        return preceeding;
    }

    @Override
    public ImmutableSet<IndexedScenario> findByTag(String tag) {
        return ImmutableSet.copyOf(scenariosByTag.get(tag));
    }

    // Tags

    @Override
    public Multiset<String> tags() {
        return scenariosByTag.keys();
    }

}
