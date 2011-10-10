package greenhouse.util;

import gherkin.util.FixJava;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class Utils {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public static final String SEPARATOR = System.getProperty("file.separator");

    public static final String MVN = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";

    public static File joinPaths(String... parts) {
        return new File(Joiner.on(SEPARATOR).join(parts));
    }

    public static File tempFile(String... parts) {
        ImmutableList<String> pieces = ImmutableList.<String> builder().add(TEMP_DIR).addAll(Arrays.asList(parts)).build();
        return new File(Joiner.on(SEPARATOR).join(pieces));
    }

    public static String readContents(String uri) {
        try {
            File file = new File(uri);
            FileReader reader = new FileReader(file);
            String contents = FixJava.readReader(reader);
            reader.close();
            return contents;
        } catch (Exception e) {
            throw new RuntimeException("Could not load file: " + uri, e);
        }
    }

    public static ProcessBuilder mavenProcess(File directory, String... args) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(directory);
        ArrayList<String> argList = new ArrayList<String>();
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            argList.add(MVN);
        } else {
            argList.add("bash");
            argList.add("-c");
        }
        for (String arg : args) {
            argList.add(arg);
        }
        builder.command(argList);
        return builder;
    }

}
