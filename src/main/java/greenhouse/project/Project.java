package greenhouse.project;

import greenhouse.index.Index;
import greenhouse.index.Indexer;
import greenhouse.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;
import java.util.Properties;

public class Project {

    private final String key;
    private final String name;
    private final File root;
    private final File files;
    private final Index index;
    private final FileSource fileSource;
    private final String command;
    private int executions;

    public Project(String key, String name, File root, String command, FileSource fileSource, int executions) {
        this.key = key;
        this.name = name;
        this.root = root;
        files = Utils.file(root.getAbsolutePath(), "files");
        index = new Indexer(files.getAbsolutePath()).index();
        this.command = command;
        this.fileSource = fileSource;
        this.executions = executions;
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

        Properties state = Utils.load(root, "state.properties");
        int executions = Integer.valueOf(state.getProperty("executions", "1"));

        String key = root.getName().toLowerCase(Locale.ENGLISH);
        return new Project(key, name, root, command, fileSource, executions);
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

    public void save() {
        Properties props = new Properties();
        props.setProperty("executions", Integer.toString(executions));

        try {
            FileWriter writer = new FileWriter(Utils.file(root.getAbsolutePath(), "state.properties"));
            props.store(writer, null);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to write " + name + " state properties file", e);
        }
    }

    public synchronized Execution nextExecution() {
        int taskId = executions++;
        File results = Utils.file(root.getAbsolutePath(), "results", Integer.toString(taskId));
        if (!results.mkdirs()) {
            throw new RuntimeException("Could not create execution results directory: " + results.getAbsolutePath());
        }
        save();
        return new Execution(key, taskId, results);
    }

    public void update() {
        fileSource.update();
    }

    public void clearHistory() {
        File results = Utils.file(root.getAbsolutePath(), "results");
        if (results.exists()) {
            Utils.delete(results);
        }
        File state = Utils.file(root.getAbsolutePath(), "state.properties");
        if (state.exists()) {
            Utils.delete(state);
        }
        executions = 1;
    }

    public static class Execution {

        private final String projectKey;
        private final int executionNumber;
        private final File directory;

        public Execution(String projectKey, int executionNumber, File directory) {
            this.projectKey = projectKey;
            this.executionNumber = executionNumber;
            this.directory = directory;
        }

        public String getProjectKey() {
            return projectKey;
        }

        public int getExecutionNumber() {
            return executionNumber;
        }

        public File getDirectory() {
            return directory;
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
