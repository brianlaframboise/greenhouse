package execute;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;

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
        if (scenario.getName().equals(this.scenario.getName())) {
            scenario.getTags().add(new Tag("@kappamaki", -1));
        }
        super.scenario(scenario);
    }
}
