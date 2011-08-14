package kappamaki.execute;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.List;

public class ExampleFilterer extends DelegatingFormatter {

    private final int line;

    public ExampleFilterer(Formatter delegate, int line) {
        super(delegate);
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

}
