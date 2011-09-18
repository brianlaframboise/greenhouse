package kappamaki.extract;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;

import java.io.Writer;

public class ScenarioFilter extends PrettyFormatter {

    private final String scenarioName;
    private String current = "";

    public ScenarioFilter(String scenarioName, Writer out) {
        super(out, true, false);
        this.scenarioName = scenarioName;
    }

    @Override
    public void scenario(Scenario scenario) {
        current = scenario.getName();
        if (scenarioName.equals(current)) {
            super.scenario(scenario);
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        current = scenarioOutline.getName();
        if (scenarioName.equals(current)) {
            super.scenarioOutline(scenarioOutline);
        }
    };

    @Override
    public void examples(Examples examples) {
        if (scenarioName.equals(current)) {
            super.examples(examples);
        }
    }

}
