package kappamaki.execute;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.io.Writer;

import kappamaki.index.IndexedScenario;

/**
 * A PrettyFormatter that adds the @kappamaki tag to given scenarios.
 */
public class Tagger extends PrettyFormatter {

    private final IndexedScenario scenario;

    public Tagger(IndexedScenario scenario, Writer out) {
        super(out, true, false);
        this.scenario = scenario;
    }

    @Override
    public void scenario(Scenario scenario) {
        tag(scenario, this.scenario.getName());
        super.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline outline) {
        tag(outline, scenario.getName());
        super.scenarioOutline(outline);
    }

    private void tag(TagStatement statement, String name) {
        if (statement.getName().equals(name)) {
            statement.getTags().add(new Tag("@kappamaki", -1));
        }
    }

}
