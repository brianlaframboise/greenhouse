package greenhouse.project;

import greenhouse.index.InMemoryIndex;
import greenhouse.index.Index;
import greenhouse.index.Indexer;
import greenhouse.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;
import java.util.Properties;

public class Project {

    private final String key;
    private final String name;
    private final File root;
    private final File files;
    private final Index index;
    private final String command;
    private int executions;

    public Project(String key, String name, File root, File files, Index index, String command, int executions) {
        this.key = key;
        this.name = name;
        this.root = root;
        this.files = files;
        this.index = index;
        this.command = command;
        this.executions = executions;
    }

    public static Project load(File root) {
        File props = Utils.joinPaths(root.getAbsolutePath(), "project.properties");
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(props));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + props.getAbsolutePath(), e);
        }

        String name = properties.getProperty("name");
        String files = properties.getProperty("files");
        int executions = Integer.valueOf(properties.getProperty("executions", "1"));
        if (files == null) {
            files = Utils.joinPaths(root.getAbsolutePath(), "files").getAbsolutePath();
        }
        InMemoryIndex index = new Indexer(files).index();

        String key = root.getName().toLowerCase(Locale.ENGLISH);
        String command = properties.getProperty("command");
        return new Project(key, name, root, new File(files), index, command, executions);
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("name", name);
        props.setProperty("files", files.getAbsolutePath());
        props.setProperty("command", command);
        props.setProperty("executions", Integer.toString(executions));

        try {
            FileWriter writer = new FileWriter(Utils.joinPaths(root.getAbsolutePath(), "project.properties"));
            props.store(writer, null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to write Project properties file", e);
        }
    }

    public synchronized Execution nextExecution() {
        int taskId = executions++;
        File tempDir = Utils.joinPaths(root.getAbsolutePath(), "results", Integer.toString(taskId));
        if (!tempDir.mkdirs()) {
            throw new RuntimeException("Could not create temp directory: " + tempDir.getAbsolutePath());
        }
        save();
        return new Execution(taskId, tempDir);
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
        return files;
    }

    public String getCommand() {
        return command;
    }

    public Index index() {
        return index;
    }

}
