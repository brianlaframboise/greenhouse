package kappamaki.project;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import kappamaki.index.InMemoryIndex;
import kappamaki.index.Indexer;
import kappamaki.util.Utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

public class PropsProjectRepository implements ProjectRepository {

    private final File repo;
    private final Map<String, Project> projects = new HashMap<String, Project>();

    public PropsProjectRepository(File repo) {
        this.repo = repo;
        Properties properties = new Properties();
        try {
            FileReader reader = new FileReader(repo);
            properties.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load properties file: " + repo.getAbsolutePath(), e);
        }

        String keyProp = properties.getProperty("keys");
        Iterable<String> keys = Splitter.on(',').split(keyProp);
        for (String key : keys) {
            String name = properties.getProperty(key + ".name");
            String path = properties.getProperty(key + ".path");
            Indexer indexer = new Indexer(Utils.joinPaths(path, "features"));
            InMemoryIndex index = indexer.index();
            Project project = new Project(key, name, new File(path), index);
            projects.put(key, project);
        }
    }

    @Override
    public ImmutableMap<String, Project> getProjects() {
        return ImmutableMap.copyOf(projects);
    }

    @Override
    public void add(Project project) {
        projects.put(project.getKey(), project);
        save();
    }

    private void save() {
        Set<String> keys = new HashSet<String>();
        Properties props = new Properties();
        for (Project project : projects.values()) {
            String key = project.getKey();
            keys.add(key);
            props.setProperty(key + ".name", project.getName());
            props.setProperty(key + ".path", project.getRoot().getAbsolutePath());
        }
        props.setProperty("keys", Joiner.on(',').join(keys).toString());

        try {
            FileWriter writer = new FileWriter(repo);
            props.store(writer, null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to write Project repository file", e);
        }
    }

}
