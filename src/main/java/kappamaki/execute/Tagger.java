package kappamaki.execute;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import kappamaki.index.IndexedScenario;

/**
 * A PrettyFormatter that adds the @kappamaki tag to given scenarios.
 */
public class Tagger extends PrettyFormatter {

    private final IndexedScenario scenario;
    private int line = -1;

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

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public void examples(Examples examples) {
        if (line >= 0) {
            List<Row> rows = examples.getRows();
            List<Row> newRows = new ArrayList<Row>(2);
            newRows.add(rows.get(0));
            for (Row row : rows.subList(1, rows.size())) {
                if (row.getLine() == line) {
                    newRows.add(row);
                    break;
                }
            }
            examples.setRows(newRows);

        }
        super.examples(examples);
    }

    private void tag(TagStatement statement, String name) {
        if (statement.getName().equals(name)) {
            statement.getTags().add(new Tag("@kappamaki", -1));
        }
    }

}
