package greenhouse.project;

import greenhouse.index.Index;
import greenhouse.index.Indexer;
import greenhouse.util.Utils;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Project {

    public static final Pattern PROJECT_KEY_PATTERN = Pattern.compile("^[A-Z]+$");

    public static final Pattern CONTEXT_KEY_PATTERN = Pattern.compile("^[a-z]+$");

    private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

    private final String key;
    private final String name;
    private final File root;
    private final File files;
    private final Index index;
    private final FileSource fileSource;

    private Map<String, Context> contexts;
    private String lastUpdateOutput = "";

    public Project(String key, String name, File root, ImmutableMap<String, Context> contexts, FileSource fileSource) {
        if (!PROJECT_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Project key " + key + " must be upper case characters only");
        }
        this.key = key;
        this.name = name;
        this.root = root;
        files = Utils.file(root.getAbsolutePath(), "files");
        index = new Indexer(key, files.getAbsolutePath()).index();
        this.contexts = Maps.newHashMap(contexts);
        this.fileSource = fileSource;
    }

    /**
     * Loads and fully initializes a Project from its root directory. The root
     * directory must contain a "project.properties" file from which the Project
     * can be bootstrapped.
     * 
     * @param root The project root directory
     * @return a new Project
     */
    public static Project load(File root) {
        Properties project = Utils.load(root, "project.properties");
        FileSource fileSource = loadFileSource(root, project);
        String name = project.getProperty("name");

        Map<String, Context> contexts = loadContexts(root);
        return new Project(root.getName(), name, root, ImmutableMap.copyOf(contexts), fileSource);
    }

    /**
     * Creates a FileSource based on a project's location and properties.
     * 
     * @param project The greenhouse directory for the project being loaded
     * @param props The project's properties
     * @return a new FileSource
     */
    private static FileSource loadFileSource(File project, Properties props) {
        String protocol = props.getProperty("src.protocol", "file");
        String url = props.getProperty("src.url");
        File filesDirectory = Utils.file(project.getAbsolutePath(), "files");
        FileSource fileSource;

        if (protocol.equals("file")) {
            if (url == null) {
                fileSource = new InPlaceFileSource(filesDirectory);
            } else {
                fileSource = new LocalFileSource(url, filesDirectory);
            }
        } else if (protocol.equals("svn")) {
            String username = props.getProperty("src.username");
            String password = props.getProperty("src.password");
            fileSource = new SvnFileSource(project, filesDirectory, url, username, password);
        } else {
            throw new RuntimeException("Unrecoginized protocol: " + protocol);
        }
        fileSource.initialize();
        return fileSource;
    }

    public String update() {
        lastUpdateOutput = fileSource.update();
        return getLastUpdateOutput();
    }

    public String getLastUpdateOutput() {
        return lastUpdateOutput;
    }

    public void clearHistory() {
        try {
            File results = Utils.file(root.getAbsolutePath(), "executions");
            if (results.exists()) {
                Utils.delete(results);
            }
            LOGGER.info("History cleared for project " + key);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to clear history for project " + key, e);
        }
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public File getRoot() {
        return root;
    }

    public File getFiles() {
        return fileSource.getDirectory();
    }

    public ImmutableMap<String, Context> getContexts() {
        return ImmutableMap.copyOf(contexts);
    }

    public Index index() {
        return index;
    }

    public void addContext(String addKey, Context context) {
        synchronized (contexts) {
            contexts.put(addKey, context);
            saveContexts();
        }
    }

    public void removeContext(String removeKey) {
        synchronized (contexts) {
            contexts.remove(removeKey);
            saveContexts();
        }
    }

    private static Map<String, Context> loadContexts(File root) {
        ImmutableMap<String, String> commands = Maps.fromProperties(Utils.load(root, "contexts.properties"));
        int i = 1;
        String key;
        Map<String, Context> contexts = Maps.newHashMap();
        while ((key = commands.get(i + ".key")) != null) {
            String contextName = commands.get(i + ".name");
            String command = commands.get(i + ".command");
            contexts.put(key, new Context(key, contextName, command));
            i++;
        }
        return contexts;
    }

    private void saveContexts() {
        Properties props = new Properties();
        int i = 1;
        for (Context context : contexts.values()) {
            props.put(i + ".key", context.getKey());
            props.put(i + ".name", context.getName());
            props.put(i + ".command", context.getCommand());
            i++;
        }
        Utils.save(Utils.file(root.getAbsolutePath(), "contexts.properties"), props);
    }

}
