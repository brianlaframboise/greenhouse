package greenhouse.index;

import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;
import gherkin.parser.Parser;
import greenhouse.util.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.lf5.util.StreamUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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

    private final File projectRoot;
    private final File featuresRoot;
    private final Set<IndexedFeature> features = new HashSet<IndexedFeature>();
    private final Multimap<String, IndexedScenario> scenariosByTag = ArrayListMultimap.create();

    private String uri;
    private String featureName;
    private final List<IndexedScenario> scenarios = new ArrayList<IndexedScenario>();

    public Indexer(String projectRoot) {
        this.projectRoot = new File(projectRoot);
        featuresRoot = Utils.file(projectRoot, "features");
    }

    public InMemoryIndex index() {
        System.out.println("Indexing features...");
        walk(featuresRoot);
        System.out.println("Features indexed.");

        List<IndexedFeature> sortedFeatures = new ArrayList<IndexedFeature>();
        sortedFeatures.addAll(features);
        Collections.sort(sortedFeatures, FEATURE_NAME_COMPARATOR);

        ImmutableSet<StepMethod> steps = indexSteps();
        return new InMemoryIndex(featuresRoot, ImmutableList.copyOf(features), ImmutableMultimap.copyOf(scenariosByTag), steps);
    }

    private ImmutableSet<StepMethod> indexSteps() {
        System.out.println("Indexing steps...");
        final ProcessBuilder builder = Utils.mavenProcess(projectRoot, ImmutableList.of("greenhouse:greenhouse-maven-plugin:0.1-SNAPSHOT:index"));
        builder.redirectErrorStream(true);
        Set<StepMethod> stepMethods = new HashSet<StepMethod>();
        try {
            System.out.println("Executing: "
                    + Joiner.on(' ').join(ImmutableList.builder().add(builder.directory().getAbsolutePath()).addAll(builder.command()).build()));
            final Process process = builder.start();
            new Thread() {
                @Override
                public void run() {
                    try {
                        // Read process output to prevent blocking
                        BufferedInputStream stream = new BufferedInputStream(process.getInputStream());
                        byte[] bytes = StreamUtils.getBytes(stream);
                        System.out.println(new String(bytes));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            process.waitFor();
            File stepIndex = Utils.file(projectRoot.getAbsolutePath(), "target", "greenhouse-step-index.properties");
            System.out.println("Steps indexed. Loading: " + stepIndex.getAbsolutePath());
            Properties props = new Properties();
            FileReader reader = new FileReader(stepIndex);
            props.load(reader);
            int i = 1;
            while (true) {
                String key = i + ".regex";
                if (!props.containsKey(key)) {
                    break;
                }
                String regex = props.getProperty(key);
                String params = props.getProperty(i + ".params");
                stepMethods.add(new StepMethod(regex, ImmutableList.copyOf(Splitter.on(',').split(params))));
                i++;
            }
            System.out.println("Loaded " + (i - 1) + " indexed steps.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ImmutableSet.copyOf(stepMethods);
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
            System.out.println("Indexing: " + file.getAbsolutePath());
            uri = file.getPath();
            IndexingFormatter formatter = new IndexingFormatter(this);
            Parser parser = new Parser(formatter);
            String gherkin = Utils.readContents(file.getPath());
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
