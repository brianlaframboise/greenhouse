package greenhouse.execute;

public class ExecutionRequest {

    private final ExecutionType type;
    private final String projectKey;
    private final String contextKey;
    private final String feature;
    private final String scenario;
    private final int line;
    private final String gherkin;

    private ExecutionRequest(ExecutionType type, String projectKey, String contextKey, String feature, String scenario, int line, String gherkin) {
        this.type = type;
        this.projectKey = projectKey;
        this.contextKey = contextKey;
        this.feature = feature;
        this.scenario = scenario;
        this.line = line;
        this.gherkin = gherkin;
    }

    public static ExecutionRequest feature(String projectKey, String contextKey, String featureName) {
        return new ExecutionRequest(ExecutionType.FEATURE, projectKey, contextKey, featureName, null, -1, null);
    }

    public static ExecutionRequest scenario(String projectKey, String contextKey, String scenarioName) {
        return new ExecutionRequest(ExecutionType.SCENARIO, projectKey, contextKey, null, scenarioName, -1, null);
    }

    public static ExecutionRequest example(String projectKey, String contextKey, String scenarioOutlineName, int line) {
        return new ExecutionRequest(ExecutionType.EXAMPLE, projectKey, contextKey, null, scenarioOutlineName, line, null);
    }

    public static ExecutionRequest gherkin(String projectKey, String contextKey, String gherkin) {
        return new ExecutionRequest(ExecutionType.GHERKIN, projectKey, contextKey, null, null, -1, gherkin);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public ExecutionType getType() {
        return type;
    }

    public String getContextKey() {
        return contextKey;
    }

    public String getFeature() {
        return feature;
    }

    public String getScenario() {
        return scenario;
    }

    public int getLine() {
        return line;
    }

    public String getGherkin() {
        return gherkin;
    }

}
