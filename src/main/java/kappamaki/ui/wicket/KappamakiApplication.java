package kappamaki.ui.wicket;

import kappamaki.ui.wicket.page.FeaturesPage;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

public class KappamakiApplication extends WebApplication {

    @Override
    protected void init() {
        addComponentInstantiationListener(new SpringComponentInjector(this));
        new AnnotatedMountScanner().scanPackage(FeaturesPage.class.getPackage().getName()).mount(this);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return FeaturesPage.class;
    }

}
