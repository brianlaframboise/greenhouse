package kappamaki.ui.wicket;

import kappamaki.ui.wicket.page.HomePage;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class KappamakiApplication extends WebApplication {

    @Override
    protected void init() {
        addComponentInstantiationListener(new SpringComponentInjector(this));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

}
