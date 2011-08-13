package kappamaki.util;

import java.io.File;
import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class Utils {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public static final String SEPARATOR = System.getProperty("file.separator");

    public static String joinPaths(String... parts) {
        return Joiner.on(SEPARATOR).join(parts);
    }

    public static File tempFile(String... parts) {
        ImmutableList<String> pieces = ImmutableList.<String> builder()
                .add(TEMP_DIR).addAll(Arrays.asList(parts)).build();
        return new File(Joiner.on(SEPARATOR).join(pieces));
    }

}
