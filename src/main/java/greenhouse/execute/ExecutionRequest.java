package greenhouse.execute;

import greenhouse.project.Context;

public class ExecutionRequest {

    private final ExecutionType type;
    private final String projectKey;
    private final Context context;
    private final String feature;
    private final String scenario;
    private final int line;
    private final String tag;
    private final String gherkin;

    private ExecutionRequest(ExecutionType type, String projectKey, Context context, String feature, String scenario, int line, String tag, String gherkin) {
        this.type = type;
        this.projectKey = projectKey;
        this.context = context;
        this.feature = feature;
        this.scenario = scenario;
        this.line = line;
        this.tag = tag;
        this.gherkin = gherkin;
    }

    public static ExecutionRequest feature(String projectKey, Context context, String featureName) {
        return new ExecutionRequest(ExecutionType.FEATURE, projectKey, context, featureName, null, -1, null, null);
    }

    public static ExecutionRequest scenario(String projectKey, Context context, String scenarioName) {
        return new ExecutionRequest(ExecutionType.SCENARIO, projectKey, context, null, scenarioName, -1, null, null);
    }

    public static ExecutionRequest example(String projectKey, Context context, String scenarioOutlineName, int line) {
        return new ExecutionRequest(ExecutionType.EXAMPLE, projectKey, context, null, scenarioOutlineName, line, null, null);
    }

    public static ExecutionRequest tag(String projectKey, Context context, String tag) {
        return new ExecutionRequest(ExecutionType.TAG, projectKey, context, null, null, -1, tag, null);
    }

    public static ExecutionRequest gherkin(String projectKey, Context context, String gherkin) {
        return new ExecutionRequest(ExecutionType.GHERKIN, projectKey, context, null, null, -1, null, gherkin);
    }

    public String getDescription() {
        if (type == ExecutionType.FEATURE) {
            return feature;
        } else if (type == ExecutionType.SCENARIO) {
            return scenario;
        } else if (type == ExecutionType.EXAMPLE) {
            return scenario + " : " + line;
        } else if (type == ExecutionType.TAG) {
            return tag;
        }
        return "";
    }

    public String getProjectKey() {
        return projectKey;
    }

    public ExecutionType getType() {
        return type;
    }

    public Context getContext() {
        return context;
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

    public String getTag() {
        return tag;
    }

    public String getGherkin() {
        return gherkin;
    }

}
