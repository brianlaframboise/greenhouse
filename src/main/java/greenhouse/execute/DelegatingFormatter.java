package greenhouse.execute;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

public class DelegatingFormatter implements Formatter {

    private final Formatter delegate;

    public DelegatingFormatter(Formatter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void uri(String uri) {
        delegate.uri(uri);
    }

    @Override
    public void feature(Feature feature) {
        delegate.feature(feature);
    }

    @Override
    public void background(Background background) {
        delegate.background(background);
    }

    @Override
    public void scenario(Scenario scenario) {
        delegate.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        delegate.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        delegate.examples(examples);
    }

    @Override
    public void step(Step step) {
        delegate.step(step);
    }

    @Override
    public void eof() {
        delegate.eof();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        delegate.syntaxError(state, event, legalEvents, uri, line);
    }

    @Override
    public void done() {
        delegate.done();
    }

    @Override
    public void close() {
        delegate.close();
    }

}
