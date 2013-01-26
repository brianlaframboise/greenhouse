package greenhouse.ui.wicket;

import greenhouse.project.Project;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * General purpose utilities for dealing with Wicket pages and components.
 */
public final class WicketUtils {

    public static final String CONTEXTS_COOKIE = "greenhouse.contexts";

    private WicketUtils() {
    }

    /**
     * Creates a PageParameters with 0-based numeric indices for the toString()
     * value of the given args.
     * 
     * @param args Zero or more indexed parameters
     * @return a new PageParameters
     */
    public static PageParameters indexed(Object... args) {
        PageParameters params = new PageParameters();
        for (int i = 0; i < args.length; i++) {
            params.add(Integer.toString(i), Strings.toString(args[i]));
        }
        return params;
    }

    /**
     * Adds components to the AjaxRequestTarget, or does nothing if target is
     * null.
     * 
     * @param target An AjaxRequestTarget, or null
     * @param components The components to add to the AjaxRequestTarget, if
     *        available
     */
    public static void addComponents(AjaxRequestTarget target, Component... components) {
        if (target != null) {
            for (Component component : components) {
                target.add(component);
            }
        }
    }

    /**
     * Retrieves the selected context key for the given project, or randomly
     * selects a valid context key for the given Project if the request does not
     * contain a context key for the given project in the contexts cookie.
     * 
     * @param request A WebRequest
     * @param project A Project
     * @return a valid Project context key
     */
    public static String getContextKey(WebRequest request, Project project) {
        Cookie cookie = getContextCookie(request);
        if (cookie == null) {
            return null;
        }
        return projectsToContexts(cookie).get(project.getKey());
    }

    /**
     * Adds, or updates, the context key in Contexts cookie for the given
     * Project.
     * 
     * @param request The current WebRequest
     * @param response The current WebResponse
     * @param project The contextKey's associated Project
     * @param contextKey The context key to set in the cookie
     */
    public static void setContextKey(WebRequest request, WebResponse response, Project project, String contextKey) {
        Cookie cookie = getContextCookie(request);
        Map<String, String> projectsToContexts = projectsToContexts(cookie);
        projectsToContexts.put(project.getKey(), contextKey);

        Set<String> pairs = Sets.newHashSet();
        for (Entry<String, String> entry : projectsToContexts.entrySet()) {
            pairs.add(entry.getKey() + "=" + entry.getValue());
        }

        cookie = new Cookie(CONTEXTS_COOKIE, Joiner.on('|').join(pairs));
        cookie.setPath("/");
        cookie.setMaxAge(31556926); // one year in seconds
        response.addCookie(cookie);
    }

    private static Cookie getContextCookie(WebRequest request) {
        List<Cookie> cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CONTEXTS_COOKIE.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private static Map<String, String> projectsToContexts(Cookie cookie) {
        Map<String, String> projectsToContexts = Maps.newHashMap();
        if (cookie != null) {
            for (String pair : Splitter.on('|').split(cookie.getValue())) {
                List<String> keyValue = Lists.newArrayList(Splitter.on('=').split(pair));
                projectsToContexts.put(keyValue.get(0), keyValue.get(1));
            }
        }
        return projectsToContexts;
    }

}
