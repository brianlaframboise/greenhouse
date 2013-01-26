package greenhouse.execute;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;

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
        List<ExamplesTableRow> newRows = new ArrayList<ExamplesTableRow>(2);
        if (line >= 0) {
            List<ExamplesTableRow> rows = examples.getRows();
            newRows.add(rows.get(0));
            for (ExamplesTableRow row : rows.subList(1, rows.size())) {
                if (row.getLine() == line) {
                    newRows.add(row);
                    break;
                }
            }
        }
        if (newRows.size() == 2) {
            examples.setRows(newRows);
        }
        super.examples(examples);
    }

}
