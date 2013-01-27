package greenhouse.ui.wicket.page.history;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Displays an entire page of raw html.
 */
public class HtmlPage extends WebPage {

    public HtmlPage(String html) {
        add(new Label("html", html).setEscapeModelStrings(false));
    }

}
