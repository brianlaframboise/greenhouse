package kappamaki.extract;

import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.parser.Parser;

import java.io.StringWriter;

import kappamaki.index.Index;
import kappamaki.index.IndexedFeature;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

public class Extractor {

    private final Index index;

    public Extractor(Index index) {
        this.index = index;
    }

    public String extract(IndexedFeature feature) {
        return extract(feature, new FormatterMaker() {
            @Override
            public Formatter make(StringWriter buffer) {
                return new PrettyFormatter(buffer, true, false);
            }
        });
    }

    public String extract(final IndexedScenario scenario) {
        IndexedFeature feature = index.findByScenario(scenario);
        return extract(feature, new FormatterMaker() {
            @Override
            public Formatter make(StringWriter buffer) {
                return new ScenarioFilter(scenario.getName(), buffer);
            }
        });
    }

    public String extract(final IndexedScenario scenario, final int line) {
        IndexedFeature feature = index.findByScenario(scenario);
        return extract(feature, new FormatterMaker() {
            @Override
            public Formatter make(StringWriter buffer) {
                return new ExampleFilter(buffer, scenario.getName(), line);
            }
        });
    }

    String extract(IndexedFeature feature, FormatterMaker maker) {
        StringWriter buffer = new StringWriter();

        String uri = feature.getUri();
        String gherkin = Utils.readContents(uri);

        Formatter formatter = maker.make(buffer);
        Parser parser = new Parser(formatter);
        parser.parse(gherkin, uri, 0);

        return buffer.toString();
    }

    interface FormatterMaker {
        Formatter make(StringWriter buffer);
    }

}
