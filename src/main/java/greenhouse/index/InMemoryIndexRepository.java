package greenhouse.index;

import greenhouse.config.GreenhouseSettings;
import greenhouse.project.Project;
import greenhouse.util.Utils;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

public class InMemoryIndexRepository implements IndexRepository {

    private final Map<String, Index> indices = Maps.newHashMap();

    private final GreenhouseSettings settings;

    public InMemoryIndexRepository(GreenhouseSettings settings) {
        this.settings = settings;
    }

    @Override
    public Index index(Project project) {
        File projectFiles = Utils.file(project.getRoot().getAbsolutePath(), "files");
        String projectKey = project.getKey();
        InMemoryIndex index = new Indexer(projectKey, projectFiles.getAbsolutePath(), settings).index();
        indices.put(projectKey, index);
        return index;
    }

    @Override
    public Index getIndex(String projectKey) {
        return indices.get(projectKey);
    }

}
