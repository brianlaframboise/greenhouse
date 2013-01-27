package greenhouse.ui.wicket.page.update;

import greenhouse.project.Project;
import greenhouse.ui.wicket.page.GreenhousePage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * A page that updates and re-indexes its associated project upon viewing. This
 * page is meant to be exposed as a simple means to enable build systems to send
 * a push notification to Greenhouse to update a project.
 */
@MountPath("/projects/${project}/update")
public class UpdatePage extends GreenhousePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePage.class);

    public UpdatePage(PageParameters params) {
        super(params);
        Project project = project();
        project.update();
        indices.index(project);
        add(new Label("output", project.getLastUpdateOutput()));
        LOGGER.info("Updated " + project.getKey());
    }
}
