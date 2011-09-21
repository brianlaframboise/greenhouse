package kappamaki.project;

import java.io.File;

import kappamaki.execute.ProcessExecutor;
import kappamaki.execute.ScenarioExecutor;
import kappamaki.index.Index;

public class Project {

    private final String key;
    private final String name;
    private final File root;
    private final Index index;
    private final ScenarioExecutor executor;

    public Project(String key, String name, File root, Index index) {
        this.key = key;
        this.name = name;
        this.root = root;
        this.index = index;
        executor = new ProcessExecutor(index, root);
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

    public Index index() {
        return index;
    }

    public ScenarioExecutor executor() {
        return executor;
    }

}
