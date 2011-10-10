package greenhouse.ui.wicket;

import greenhouse.ui.wicket.page.FeaturesPage;
import greenhouse.ui.wicket.page.ProjectsPage;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.jquery.JQueryResourceReference;

public class GreenhouseApplication extends WebApplication {

    @Override
    protected void init() {
        addComponentInstantiationListener(new SpringComponentInjector(this));
        new AnnotatedMountScanner().scanPackage(FeaturesPage.class.getPackage().getName()).mount(this);
        getMarkupSettings().setStripWicketTags(true);
        addRenderHeadListener(JavascriptPackageResource.getHeaderContribution(new JQueryResourceReference()));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return ProjectsPage.class;
    }

}
