package kappamaki.execute;

import gherkin.formatter.model.TagStatement;

import java.io.Writer;

import kappamaki.index.IndexedScenario;

/**
 * A PrettyFormatter that adds the @kappamaki tag to given scenarios.
 */
public class ScenarioTagger extends KappamakiTagger {

    private final IndexedScenario scenario;

    public ScenarioTagger(IndexedScenario scenario, Writer out) {
        super(out);
        this.scenario = scenario;
    }

    @Override
    protected void tag(TagStatement statement) {
        if (statement.getName().equals(scenario.getName())) {
            super.tag(statement);
        }
    }

}
