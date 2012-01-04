package greenhouse.project;

import greenhouse.index.InMemoryIndex;
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
        Properties project = Utils.load(root, "project.properties");

        String name = project.getProperty("name");
        String files = project.getProperty("files");
        String command = project.getProperty("command");
        if (files == null) {
            files = Utils.file(root.getAbsolutePath(), "files").getAbsolutePath();
        }
        InMemoryIndex index = new Indexer(files).index();

        Properties state = Utils.load(root, "state.properties");
        int executions = Integer.valueOf(state.getProperty("executions", "1"));

        String key = root.getName().toLowerCase(Locale.ENGLISH);
        return new Project(key, name, root, new File(files), index, command, executions);
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
        return files;
    }

    public String getCommand() {
        return command;
    }

    public Index index() {
        return index;
    }

}
