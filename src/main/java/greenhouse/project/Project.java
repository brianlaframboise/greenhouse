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

    private static FileSource loadFileSource(File root, Properties project) {
        String protocol = project.getProperty("src.protocol", "file");
        String url = project.getProperty("src.url");
        File filesDirectory = Utils.file(root.getAbsolutePath(), "files");
        FileSource fileSource;

        if (protocol.equals("file")) {
            if (url == null) {
                url = filesDirectory.getAbsolutePath();
            }
            fileSource = new LocalFileSource(url);
        } else if (protocol.equals("svn")) {
            String username = project.getProperty("src.username");
            String password = project.getProperty("src.password");
            fileSource = new SvnFileSource(root, filesDirectory, url, username, password);
            fileSource.getDirectory();
            fileSource.initialize();
        } else {
            throw new RuntimeException("Unrecoginized protocol: " + protocol);
        }
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
        return new Execution(taskId, results);
    }

    public void update() {
        fileSource.update();
    }

    public void clearHistory() {
        File results = Utils.file(root.getAbsolutePath(), "results");
        if (results.exists()) {
            delete(results);
        }
        File state = Utils.file(root.getAbsolutePath(), "state.properties");
        if (state.exists()) {
            delete(state);
        }
        executions = 1;
    }

    private void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        System.out.println("Deleting: " + f);
        if (!f.delete()) {
            throw new RuntimeException("Could not delete " + f.getPath());
        }
    }

    public static class Execution {

        private final int taskId;
        private final File directory;

        public Execution(int taskId, File directory) {
            this.taskId = taskId;
            this.directory = directory;
        }

        public int getTaskId() {
            return taskId;
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
