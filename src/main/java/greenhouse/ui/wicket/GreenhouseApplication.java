package greenhouse.ui.wicket;

import greenhouse.execute.ExecutionState;
import greenhouse.execute.ExecutionType;
import greenhouse.ui.wicket.page.FeaturesPage;
import greenhouse.ui.wicket.page.ProjectsPage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.IConverter;
import org.springframework.util.StringUtils;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

public class GreenhouseApplication extends WebApplication {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    protected void init() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        new AnnotatedMountScanner().scanPackage(FeaturesPage.class.getPackage().getName()).mount(this);
        getMarkupSettings().setStripWicketTags(true);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return ProjectsPage.class;
    }

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(Date.class, new StringConverter() {
            @Override
            public String convertToString(Date value, Locale locale) {
                return new SimpleDateFormat(DATE_FORMAT).format((Date) value);
            }
        });
        locator.set(ExecutionState.class, new ToStringCapitalizer());
        locator.set(ExecutionType.class, new ToStringCapitalizer());
        return locator;
    }

    private static class ToStringCapitalizer extends StringConverter {
        @Override
        public String convertToString(Date value, Locale locale) {
            return StringUtils.capitalize(value.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    private static abstract class StringConverter implements IConverter<Date> {
        @Override
        public Date convertToObject(String value, Locale locale) {
            throw new UnsupportedOperationException();
        }
    }

}
