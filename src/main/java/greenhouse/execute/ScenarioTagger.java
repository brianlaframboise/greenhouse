package greenhouse.execute;

import gherkin.formatter.model.TagStatement;
import greenhouse.index.IndexedScenario;

import java.io.Writer;

/**
 * A PrettyFormatter that adds the @greenhouse tag to given scenarios.
 */
public class ScenarioTagger extends GreenhouseTagger {

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
