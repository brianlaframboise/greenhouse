package greenhouse.project;

import greenhouse.index.Index;
import greenhouse.index.Indexer;
import greenhouse.util.Utils;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

public class Project {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z]+$");

    private final String key;
    private final String name;
    private final File root;
    private final File files;
    private final Index index;
    private final FileSource fileSource;
    private final String command;

    public Project(String key, String name, File root, String command, FileSource fileSource) {
        if (!KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Project key " + key + " must be upper case characters only");
        }
        this.key = key;
        this.name = name;
        this.root = root;
        files = Utils.file(root.getAbsolutePath(), "files");
        index = new Indexer(key, files.getAbsolutePath()).index();
        this.command = command;
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

        String name = project.getProperty("name");
        String command = project.getProperty("command");

        FileSource fileSource = loadFileSource(root, project);

        return new Project(root.getName(), name, root, command, fileSource);
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

    public void update() {
        fileSource.update();
    }

    public void clearHistory() {
        File results = Utils.file(root.getAbsolutePath(), "executions");
        if (results.exists()) {
            Utils.delete(results);
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

    public String getCommand() {
        return command;
    }

    public Index index() {
        return index;
    }

}
