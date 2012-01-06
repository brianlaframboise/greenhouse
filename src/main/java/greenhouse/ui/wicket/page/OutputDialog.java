package greenhouse.ui.wicket.page;

import greenhouse.execute.ScenarioExecutor;
import greenhouse.execute.TaskId;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

import com.visural.wicket.component.dialog.Dialog;

/**
 * Wraps a VisuralWicket Dialog that automatically refreshes itself to show the
 * process of a scenario exeuction.
 */
public class OutputDialog extends Panel {

    @SpringBean
    private ScenarioExecutor executor;

    private final Dialog dialog;
    private final Label output;
    private final Label progress;

    public OutputDialog(String id) {
        super(id);
        dialog = new Dialog("dialog");
        // new FilteringModel();
        output = new Label("output", new Model<String>(""));
        add(dialog);
        dialog.add((progress = new Label("progress", "")).setOutputMarkupId(true));
        dialog.add(output.setOutputMarkupId(true));
        dialog.add(new AjaxFallbackLink<Void>("close") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void begin(final TaskId taskId, AjaxRequestTarget target) {
        progress.setDefaultModelObject("Preparing...");
        output.setDefaultModelObject("");
        final long startTime = System.currentTimeMillis();
        output.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                String outputText = "";
                long runtime = (System.currentTimeMillis() - startTime) / 1000;
                String prefix = "Running...";
                if (executor.isComplete(taskId)) {
                    outputText = executor.getOutput(taskId);
                    prefix = "Complete!";
                    stop();
                } else {
                    outputText = executor.getPartialOutput(taskId);
                }
                progress.setDefaultModelObject(prefix + " (" + runtime + "s)");
                output.setDefaultModelObject(outputText);
                if (target != null) {
                    target.addComponent(output);
                    target.addComponent(progress);
                }
            }
        });
        target.addComponent(dialog);
        target.addComponent(output);
        target.addComponent(progress);
        dialog.open(target);
    }

    @SuppressWarnings("unused")
    private static class FilteringModel extends Model<String> {
        private String text = "";

        @Override
        public void setObject(String object) {
            String filtered = object;
            String remove = "greenhouse-example ---";
            int index = object.lastIndexOf(remove);
            filtered = filtered.substring(index + remove.length() + 1);
            filtered = filtered.replaceAll("\\[INFO\\] ", "");
            text = filtered;
        }

        @Override
        public String getObject() {
            return text;
        }
    }
}