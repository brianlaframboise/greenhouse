package greenhouse.ui.wicket.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.wicketstuff.annotation.strategy.MountIndexedParam;

/**
 * Displays an entire page of raw html.
 */
@MountIndexedParam
public class HtmlPage extends WebPage {

    public HtmlPage(String html) {
        add(new Label("html", html).setEscapeModelStrings(false));
    }

}
