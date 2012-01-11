package greenhouse.ui.wicket.page;

import greenhouse.project.Project;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * Administrator page.
 */
@MountPath(path = "/admin")
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
