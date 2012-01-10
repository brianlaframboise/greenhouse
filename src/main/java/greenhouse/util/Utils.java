package greenhouse.util;

import gherkin.util.FixJava;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Miscellaneous utilities for dealing with files, maven, and properties.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String SEPARATOR = System.getProperty("file.separator");

    public static final String MVN = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";

    public static File file(String... parts) {
        return new File(Joiner.on(SEPARATOR).join(parts));
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

    public static ProcessBuilder mavenProcess(File directory, List<String> args) {
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

    public static Properties load(File directory, String filename) {
        File props = Utils.file(directory.getAbsolutePath(), filename);
        Properties properties = new Properties();
        if (props.exists()) {
            try {
                FileReader reader = new FileReader(props);
                properties.load(reader);
                reader.close();
            } catch (Exception e) {
                throw new RuntimeException("Unable to load properties file: " + props.getAbsolutePath(), e);
            }
        }
        return properties;
    }

    public static void save(File file, Properties props) {
        try {
            FileWriter writer = new FileWriter(file);
            props.store(writer, null);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save properties to " + file, e);
        }
    }

    /**
     * Deletes the file or recursively deletes the given directory
     * 
     * @param file the file or directory to delete
     */
    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        LOGGER.debug("Deleting: " + file);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Could not delete " + file.getPath());
        }
    }
}
