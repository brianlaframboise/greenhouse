package kappamaki.index;

import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;
import gherkin.parser.Parser;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import kappamaki.util.Utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class Indexer {
    private static final FileFilter FEATURES = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".feature");
        }
    };

    private final File featuresRoot;
    private final Set<IndexedScenario> scenarios = new HashSet<IndexedScenario>();
    private final Multimap<String, IndexedScenario> tagged = ArrayListMultimap.create();
    private final Multimap<String, IndexedScenario> byUri = ArrayListMultimap.create();

    private String uri;

    public Indexer(File featuresRoot) {
        this.featuresRoot = featuresRoot;
    }

    public InMemoryIndex index() {
        walk(featuresRoot);
        return new InMemoryIndex(featuresRoot, ImmutableSet.copyOf(scenarios), tagged, byUri);
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

    public void scenario(Scenario scenario) {
        addTagStatement(scenario, "scenario");
    }

    public void scenarioOutline(ScenarioOutline outline) {
        addTagStatement(outline, "scenario_outline");
    }

    private void addTagStatement(TagStatement statement, String type) {
        int line = statement.getLine();
        String name = statement.getName();
        String description = statement.getDescription();
        ImmutableSet<Tag> tags = ImmutableSet.copyOf(statement.getTags());

        IndexedScenario indexed = new IndexedScenario(uri, line, type, name, description, tags);
        add(indexed);
    }

    private void add(IndexedScenario indexed) {
        scenarios.add(indexed);
        for (Tag tag : indexed.getTags()) {
            tagged.put(tag.getName(), indexed);
        }
        byUri.put(indexed.getUri(), indexed);
    }
}
