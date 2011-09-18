package kappamaki.extract;

import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ExampleFilter extends PrettyFormatter {

    private final String outlineName;
    private final int line;
    private boolean inOutline = false;

    public ExampleFilter(Writer out, String outlineName, int line) {
        super(out, true, false);
        this.outlineName = outlineName;
        this.line = line;
    }

    @Override
    public void scenario(Scenario scenario) {
    }

    @Override
    public void scenarioOutline(ScenarioOutline outline) {
        if (outlineName.equals(outline.getName())) {
            super.scenarioOutline(outline);
            inOutline = true;
        } else {
            inOutline = false;
        }
    };

    @Override
    public void examples(Examples examples) {
        if (inOutline) {
            List<Row> rows = examples.getRows();
            List<Row> newRows = new ArrayList<Row>(2);
            newRows.add(rows.get(0)); // headings
            for (Row row : rows.subList(1, rows.size())) {
                if (row.getLine() == line) {
                    newRows.add(row);
                    break;
                }
            }
            examples.setRows(newRows);
            super.examples(examples);
        }
    }

}
