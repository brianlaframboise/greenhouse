package greenhouse.index;

import java.io.Serializable;

import com.google.common.collect.ImmutableSet;

public class IndexedScenario implements Serializable {

    private final int line;
    /** One of "scenario" or "scenario_outline" */
    private final String type;
    private final String name;
    private final String description;
    private final ImmutableSet<Tag> tags;

    public IndexedScenario(int line, String type, String name, String description, ImmutableSet<greenhouse.index.Tag> tags) {
        this.line = line;
        this.type = type;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

    public int getLine() {
        return line;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ImmutableSet<Tag> getTags() {
        return tags;
    }

}
