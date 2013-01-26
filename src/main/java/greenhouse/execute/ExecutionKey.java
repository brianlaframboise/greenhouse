package greenhouse.execute;

import java.io.Serializable;

/**
 * A symbolic key representative of a single Cucumber execution in a given
 * Project.
 */
public class ExecutionKey implements Serializable {

    private final String projectKey;
    private final int executionNumber;

    /**
     * Creates a new ExecutionKey.
     * 
     * @param projectKey The key to the associated Project.
     * @param executionNumber The unique execution number for this execution.
     */
    public ExecutionKey(String projectKey, int executionNumber) {
        this.projectKey = projectKey;
        this.executionNumber = executionNumber;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public int getNumber() {
        return executionNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + executionNumber;
        result = prime * result + projectKey.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExecutionKey other = (ExecutionKey) obj;
        if (executionNumber != other.executionNumber) {
            return false;
        }

        return projectKey.equals(other.projectKey);
    }

    @Override
    public String toString() {
        return projectKey + "-" + executionNumber;
    }

}
