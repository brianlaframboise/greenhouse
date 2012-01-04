package greenhouse.ui.wicket.page;

import greenhouse.index.StepMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@MountIndexedParam
public class StepsPage extends GreenhousePage {

    public StepsPage(PageParameters params) {
        super(params);

        final TextArea<String> scenario = new TextArea<String>("scenario", Model.of(""));
        add(scenario.setOutputMarkupId(true));

        Form<Void> form = new Form<Void>("form");
        add(form);

        final Model<String> stepModel = Model.of("");

        AutoCompleteSettings settings = new AutoCompleteSettings();
        settings.setPreselect(true);
        settings.setThrottleDelay(100);
        settings.setShowListOnFocusGain(true);
        settings.setAdjustInputWidth(true);
        final AutoCompleteTextField<String> stepField = new AutoCompleteTextField<String>("step", stepModel, String.class, settings) {

            @Override
            protected Iterator<String> getChoices(String input) {
                if ("".equals(input)) {
                    return ImmutableList.<String> of().iterator();
                }
                List<String> tokens = ImmutableList.copyOf(Splitter.on(' ').split(input));

                ImmutableSet<StepMethod> steps = index().steps();
                ArrayList<String> choices = new ArrayList<String>();
                for (StepMethod step : steps) {
                    boolean all = true;
                    for (String token : tokens) {
                        if (!step.getRegex().contains(token)) {
                            all = false;
                            break;
                        }
                    }
                    if (all) {
                        choices.add(step.getRegex());
                    }
                }
                return choices.iterator();
            }
        };

        form.add(stepField.setMarkupId("step").setOutputMarkupId(true));
        form.add(new AjaxButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String text = scenario.getDefaultModelObjectAsString();
                scenario.setDefaultModelObject(text + "\n" + stepModel.getObject());
                stepModel.setObject("");
                target.addComponent(stepField);
                target.addComponent(scenario);
                target.appendJavascript("document.getElementById('step').focus()");
            }

        });

    }
}
