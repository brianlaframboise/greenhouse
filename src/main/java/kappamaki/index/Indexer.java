package kappamaki.index;

import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;
import gherkin.parser.Parser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kappamaki.util.Utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class Indexer {
    private static final Comparator<IndexedFeature> FEATURE_NAME_COMPARATOR = new Comparator<IndexedFeature>() {
        @Override
        public int compare(IndexedFeature first, IndexedFeature second) {
            return first.getName().compareTo(second.getName());
        }
    };

    private static final FileFilter FEATURES = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".feature");
        }
    };

    private final File featuresRoot;
    private final Set<IndexedFeature> features = new HashSet<IndexedFeature>();
    private final Multimap<String, IndexedScenario> scenariosByTag = ArrayListMultimap.create();

    private String uri;
    private String featureName;
    private final List<IndexedScenario> scenarios = new ArrayList<IndexedScenario>();

    public Indexer(File featuresRoot) {
        this.featuresRoot = featuresRoot;
    }

    public InMemoryIndex index() {
        walk(featuresRoot);
        List<IndexedFeature> sortedFeatures = new ArrayList<IndexedFeature>();
        sortedFeatures.addAll(features);
        Collections.sort(sortedFeatures, FEATURE_NAME_COMPARATOR);
        return new InMemoryIndex(featuresRoot, ImmutableList.copyOf(features), ImmutableMultimap.copyOf(scenariosByTag));
    }

    private void walk(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles(FEATURES)) {
                walk(child);
            }
        } else {
            index(file);
        }
    }

    private void index(File file) {
        try {
            uri = file.getPath();
            IndexingFormatter formatter = new IndexingFormatter(this);
            Parser parser = new Parser(formatter);
            String gherkin = Utils.readGherkin(file.getPath());
            parser.parse(gherkin, file.getPath(), 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void feature(Feature feature) {
        featureName = feature.getName();
    }

    public void scenario(Scenario scenario) {
        addTagStatement(scenario, "scenario");
    }

    public void scenarioOutline(ScenarioOutline outline) {
        addTagStatement(outline, "scenario_outline");
    }

    public void eof() {
        features.add(new IndexedFeature(uri, featureName, ImmutableList.copyOf(scenarios)));
        uri = null;
        featureName = null;
        scenarios.clear();
    }

    private void addTagStatement(TagStatement statement, String type) {
        int line = statement.getLine();
        String name = statement.getName();
        String description = statement.getDescription();
        ImmutableSet<Tag> tags = ImmutableSet.copyOf(statement.getTags());

        IndexedScenario indexed = new IndexedScenario(line, type, name, description, tags);
        scenarios.add(indexed);
        for (Tag tag : indexed.getTags()) {
            scenariosByTag.put(tag.getName(), indexed);
        }
    }
}
