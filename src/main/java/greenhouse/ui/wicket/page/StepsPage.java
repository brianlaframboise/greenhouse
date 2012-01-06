package greenhouse.ui.wicket.page;

import greenhouse.index.StepMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutocompleteTextField;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@MountIndexedParam
public class StepsPage extends GreenhousePage {

    /**
     * Splits a Gherkin step into a list of the predicate part and the remainder
     * part.
     * 
     * Input: "given I do x" Result: 0 -> "Given ", 1 -> "I do x"
     * 
     * Input: "I do x" Result: 0 -> "", 1 -> "I do x"
     * 
     * @param step A gherkin step
     * @return A two-element List of (predicate, remainder)
     */
    private static ImmutableList<String> splitStep(String step) {
        for (String arg : ImmutableList.of("given ", "when ", "then ", "and ", "but ")) {
            if (step.toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                int index = step.indexOf(' ') + 1;
                return ImmutableList.of(Strings.capitalize(step.substring(0, index).toLowerCase(Locale.ENGLISH)), step.substring(index));
            }
        }
        return ImmutableList.of("", step);
    }

    public static class AppendingGherkinModel extends Model<String> {
        private List<String> steps = Lists.newArrayList();

        public AppendingGherkinModel(String value) {
            super.setObject(value);
        }

        @Override
        public String getObject() {
            return Joiner.on('\n').join(steps);
        }

        @Override
        public void setObject(String step) {
            ImmutableList<String> split = splitStep(step);
            String prep = split.get(0);
            String remainder = split.get(1);
            if ("".equals(prep)) {
                steps.add((steps.isEmpty() ? "Given " : "And ") + remainder);
            } else {
                steps.add(Strings.capitalize(prep) + remainder);
            }
        }

        public void undo() {
            steps.remove(steps.size() - 1);
        }

    }

    public StepsPage(PageParameters params) {
        super(params);

        OutputDialog dialog = new OutputDialog("dialog");
        add(dialog);

        final AppendingGherkinModel gherkinModel = new AppendingGherkinModel("");
        final TextArea<String> scenario = new TextArea<String>("scenario", gherkinModel);
        add(scenario.setOutputMarkupId(true));
        add(new ExecuteGherkinLink("execute", project().getKey(), dialog, new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                String gherkin = gherkinModel.getObject();
                String formatted = Joiner.on("\n\t\t").join(Splitter.on('\n').split(gherkin));
                return "Feature: Custom feature\n\n\tScenario: Custom scenario\n" + formatted;
            }
        }));
        add(new AjaxFallbackLink<Void>("undo") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                gherkinModel.undo();
                if (target != null) {
                    target.addComponent(scenario);
                }
            }
        });

        Form<Void> form = new Form<Void>("form");
        add(form);

        final WebMarkupContainer table = new WebMarkupContainer("table");
        form.add(table.setVisible(false));
        table.add(new WebMarkupContainer("inputs"));

        final Model<String> stepModel = Model.of("");

        AutoCompleteSettings settings = new AutoCompleteSettings();
        settings.setPreselect(true);
        settings.setThrottleDelay(100);
        settings.setShowListOnFocusGain(true);
        settings.setAdjustInputWidth(true);
        add(CSSPackageResource.getHeaderContribution(DefaultCssAutocompleteTextField.class, "DefaultCssAutocompleteTextField.css"));
        final AutoCompleteTextField<String> stepField = new AutoCompleteTextField<String>("step", stepModel, String.class, settings) {

            @Override
            protected Iterator<String> getChoices(String input) {
                if ("".equals(input)) {
                    return ImmutableList.<String> of().iterator();
                }
                ImmutableList<String> split = splitStep(input);
                String prep = split.get(0);
                String remainder = split.get(1);

                List<String> tokens = ImmutableList.copyOf(Splitter.on(' ').split(remainder));

                ImmutableSet<StepMethod> steps = index().steps();
                ArrayList<String> choices = new ArrayList<String>();
                for (StepMethod step : steps) {
                    boolean all = true;
                    String regex = step.getRegex();
                    for (String token : tokens) {
                        if (!regex.contains(token)) {
                            all = false;
                            break;
                        }
                    }
                    if (all) {
                        // Trim ^regex$ to regex
                        String trimmed = regex.substring(1, regex.length() - 1);
                        ImmutableList<String> types = step.getTypes();
                        int i = 0;
                        while (trimmed.contains("(")) {
                            int indexOf = trimmed.indexOf('(');
                            String newTrimmed = trimmed.substring(0, indexOf);
                            String type = types.get(i);
                            newTrimmed += "[[" + type.substring(type.lastIndexOf('.') + 1) + "]]";
                            trimmed = newTrimmed + trimmed.substring(trimmed.indexOf(')') + 1);
                            i++;
                        }
                        choices.add(prep + trimmed);
                    }
                }
                Collections.sort(choices);
                return choices.iterator();
            }
        };
        form.add(stepField.setMarkupId("step").setOutputMarkupId(true));

        final AjaxButton substitute = new AjaxButton("substitute", form) {
            @SuppressWarnings("rawtypes")
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final List<String> values = Lists.newArrayList();
                table.visitChildren(FormComponent.class, new Component.IVisitor<FormComponent>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Object component(FormComponent component) {
                        values.add(((FormComponent<String>) component).getModelObject());
                        return Component.IVisitor.CONTINUE_TRAVERSAL;
                    };
                });
                String step = stepModel.getObject();
                for (String value : values) {
                    step = step.substring(0, step.indexOf("[[")) + value + step.substring(step.indexOf("]]") + 2);
                }
                updateScenario(scenario, stepModel, stepField, target, step);
                setVisible(false);
                table.setVisible(false);
                form.get("add").setVisible(true);
                target.addComponent(form);
            }
        };
        form.add(substitute.setVisible(false).setOutputMarkupPlaceholderTag(true));

        form.add(new AjaxButton("add", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String text = stepModel.getObject();
                if (text.contains("[[")) {
                    List<String> types = Lists.newArrayList();
                    while (text.contains("[[")) {
                        int endIndex = text.indexOf("]]");
                        types.add(text.substring(text.indexOf("[[") + 2, endIndex));
                        text = text.substring(endIndex + 2);
                    }
                    table.replace(new ListView<String>("inputs", types) {
                        @Override
                        protected void populateItem(ListItem<String> item) {
                            String type = item.getModelObject();
                            item.add(new Label("type", type));
                            ImmutableMap<String, ImmutableList<String>> examples = index().examples();
                            List<String> values = null;
                            for (String clazz : examples.keySet()) {
                                if (type.equals(clazz.substring(clazz.lastIndexOf('.') + 1))) {
                                    values = examples.get(clazz);
                                    break;
                                }
                            }
                            if (values == null) {
                                item.add(new Fragment("input", "text", this).add(new TextField<String>("input", Model.of(""))));
                            } else {
                                item.add(new Fragment("input", "select", this).add(new DropDownChoice<String>("input", Model.of(""), values)));
                            }
                            item.add(new Label("example", "TODO"));
                        }
                    });
                    table.setVisible(true);
                    substitute.setVisible(true);
                    setVisible(false);
                    target.addComponent(form);
                } else {
                    updateScenario(scenario, stepModel, stepField, target, text);
                }
            }
        });
    }

    private void updateScenario(final TextArea<String> scenario, final Model<String> stepModel, final AutoCompleteTextField<String> stepField,
            AjaxRequestTarget target, String text) {
        scenario.setDefaultModelObject(text);
        stepModel.setObject("");
        target.addComponent(stepField);
        target.addComponent(scenario);
    }

}
