package greenhouse.ui.wicket;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.util.string.Strings;

import com.google.common.collect.Maps;

/**
 * General purpose utilities for dealing with Wicket pages and components.
 */
public final class WicketUtils {

    private WicketUtils() {
    }

    public static PageParameters indexed(Object... args) {
        Map<String, String> params = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            params.put(Integer.toString(i), Strings.toString(args[i]));
        }
        return new PageParameters(params);
    }
}
