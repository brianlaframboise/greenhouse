package greenhouse.ui.wicket;

import greenhouse.ui.wicket.page.FeaturesPage;
import greenhouse.ui.wicket.page.ProjectsPage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.convert.IConverter;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.jquery.JQueryResourceReference;

public class GreenhouseApplication extends WebApplication {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(Date.class, new IConverter() {
            @Override
            public String convertToString(Object value, Locale locale) {
                return new SimpleDateFormat(DATE_FORMAT).format((Date) value);
            }

            @Override
            public Object convertToObject(String value, Locale locale) {
                throw new UnsupportedOperationException();
            }
        });
        return locator;
    }

}
