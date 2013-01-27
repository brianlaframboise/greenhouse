package greenhouse.ui.wicket.page.admin;

import greenhouse.project.Project;
import greenhouse.ui.wicket.page.GreenhousePage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Administrator page.
 */
@MountPath("/admin")
public class AdminPage extends GreenhousePage {

    public AdminPage(PageParameters params) {
        super(params);
        add(new AjaxFallbackLink<Void>("clearAllHistory") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                for (Project project : repo.getAllProjects()) {
                    project.clearHistory();
                }
            }
        });
    }
}
