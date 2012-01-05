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
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.lf5.util.StreamUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

        System.out.println("Indexing steps...");
        Properties props = loadIndex();
        ImmutableSet<StepMethod> steps = indexSteps(props);
        ImmutableMap<String, ImmutableList<String>> examples = indexExamples(props);
        return new InMemoryIndex(featuresRoot, ImmutableList.copyOf(features), ImmutableMultimap.copyOf(scenariosByTag), steps, examples);
    }

    private Properties loadIndex() {
        final ProcessBuilder builder = Utils.mavenProcess(projectRoot, ImmutableList.of("greenhouse:greenhouse-maven-plugin:0.1-SNAPSHOT:index"));
        builder.redirectErrorStream(true);
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
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load index properties file", e);
        }
    }

    private ImmutableSet<StepMethod> indexSteps(Properties props) {
        int i = 1;
        Set<StepMethod> stepMethods = new HashSet<StepMethod>();
        while (true) {
            String key = i + ".regex";
            if (!props.containsKey(key)) {
                break;
            }
            String regex = props.getProperty(key);
            String params = props.getProperty(i + ".params");
            ImmutableList<String> types = "".equals(params) ? ImmutableList.<String> of() : ImmutableList.copyOf(Splitter.on(',').split(params));
            stepMethods.add(new StepMethod(regex, types));
            i++;
        }
        System.out.println("Loaded " + (i - 1) + " indexed steps.");
        return ImmutableSet.copyOf(stepMethods);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ImmutableMap<String, ImmutableList<String>> indexExamples(Properties props) {
        Map<String, ImmutableList<String>> examples = Maps.newHashMap();
        for (String key : (Set<String>) (Set) props.keySet()) {
            if (key.startsWith("enum.")) {
                String clazz = key.substring("enum.".length());
                String value = props.getProperty(key);
                List<String> values = Lists.newArrayList(Splitter.on(',').split(value));
                Collections.sort(values);
                examples.put(clazz, ImmutableList.copyOf(values));
            }
        }
        System.out.println("Loaded " + examples.keySet().size() + " enum values.");
        return ImmutableMap.copyOf(examples);
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
