package greenhouse.execute;

public class TaskId {

    private final String projectKey;
    private final int executionNumber;

    public TaskId(String projectKey, int execution) {
        this.projectKey = projectKey;
        executionNumber = execution;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public int getExecution() {
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
        TaskId other = (TaskId) obj;
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
