package greenhouse.ui.wicket;

import greenhouse.project.Project;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
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

    public static PageParameters indexed(Object... args) {
        Map<String, String> params = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            params.put(Integer.toString(i), Strings.toString(args[i]));
        }
        return new PageParameters(params);
    }

    public static void addComponents(AjaxRequestTarget target, Component... components) {
        if (target != null) {
            for (Component component : components) {
                target.addComponent(component);
            }
        }
    }

    public static String getContextKey(WebRequest request, Project project) {
        Cookie cookie = getContextCookie(request);
        if (cookie == null) {
            return null;
        }
        return projectsToContexts(cookie).get(project.getKey());
    }

    public static void addContextKey(WebRequest request, WebResponse response, Project project, String contextKey) {
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
        for (Cookie cookie : request.getCookies()) {
            if (CONTEXTS_COOKIE.equals(cookie.getName())) {
                return cookie;
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
