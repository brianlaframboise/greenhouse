package greenhouse.ui.wicket.page;

import greenhouse.project.Context;
import greenhouse.project.Project;
import greenhouse.ui.wicket.WicketUtils;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import com.google.common.collect.ImmutableList;

public class SettingsPage extends BaseProjectPage {

    public SettingsPage(final PageParameters params) {
        super(params);

        ImmutableList<Context> contexts = ImmutableList.copyOf(project().getContexts().values());
        final String activeContextKey = WicketUtils.getContextKey(getWebRequestCycle().getWebRequest(), project());
        add(new ListView<Context>("contexts", contexts) {
            @Override
            protected void populateItem(ListItem<Context> item) {
                Context context = item.getModelObject();
                final String key = context.getKey();
                item.add(new Label("key", key));
                item.add(new Label("name", context.getName()));
                item.add(new Label("command", context.getCommand()));
                item.add(new Link<Void>("remove") {
                    @Override
                    public void onClick() {
                        Project project = project();
                        project.removeContext(key);
                        setResponsePage(ProjectsPage.class, params);
                        setRedirect(true);
                    }
                }.setVisible(!key.equals(activeContextKey)));
            }
        });

        Form<Void> form = new Form<Void>("form");

        final IModel<String> addKeyModel = Model.of("");
        form.add(newTextField("addKey", addKeyModel).add(new PatternValidator(Project.CONTEXT_KEY_PATTERN)));

        final IModel<String> addNameModel = Model.of("");
        form.add(newTextField("addName", addNameModel));

        final IModel<String> addCommandModel = Model.of("");
        form.add(newTextField("addCommand", addCommandModel));

        form.add(new Button("submit") {
            @Override
            public void onSubmit() {
                String key = addKeyModel.getObject();
                Context context = new Context(key, addNameModel.getObject(), addCommandModel.getObject());
                project().addContext(key, context);
                setResponsePage(ProjectsPage.class, params);
                setRedirect(true);
            }
        });
        form.add(new FeedbackPanel("feedback"));
        add(form);

        final Label updateOutput = new Label("updateOutput", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return project().getLastUpdateOutput();
            }
        });
        add(updateOutput.setOutputMarkupId(true));

        add(new IndicatingAjaxLink<Void>("update") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Project project = project();
                project.update();
                indices.index(project);
                WicketUtils.addComponents(target, updateOutput);
            }
        });
    }

    private TextField<String> newTextField(String id, IModel<String> model) {
        TextField<String> field = new TextField<String>(id, model);
        field.setMarkupId(field.getId());
        field.setRequired(true);
        return field;
    }

}
