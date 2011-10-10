package greenhouse.execute;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.io.Writer;

public class GreenhouseTagger extends PrettyFormatter {

    public GreenhouseTagger(Writer out) {
        super(out, true, false);
    }

    @Override
    public void scenario(Scenario scenario) {
        tag(scenario);
        super.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline outline) {
        tag(outline);
        super.scenarioOutline(outline);
    }

    protected void tag(TagStatement statement) {
        statement.getTags().add(new Tag("@greenhouse", -1));
    }
}
