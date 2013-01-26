package greenhouse.index;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

public class IndexingFormatter implements Formatter {

    private final Indexer indexer;

    public IndexingFormatter(Indexer indexer) {
        this.indexer = indexer;
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(Feature feature) {
        indexer.feature(feature);
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
        indexer.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        indexer.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void eof() {
        indexer.eof();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void close() {
    }

    @Override
    public void done() {
    }

}
