package greenhouse.project;

import java.io.Serializable;

/**
 * Represents a distinct execution context for a Project. By providing multiple
 * contexts a Project can target its execution against multiple test enviroments
 * or with different runtime configuration.
 */
public class Context implements Serializable {

    private final String key;
    private final String name;
    private final String command;

    /**
     * Creates a new Context.
     * 
     * @param key A project-unique key
     * @param name The verbose name of this context
     * @param command The Maven command to execute for this context
     */
    public Context(String key, String name, String command) {
        this.key = key;
        this.name = name;
        this.command = command;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

}
