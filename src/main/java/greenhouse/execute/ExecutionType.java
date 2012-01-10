package greenhouse.execute;

/**
 * Describes the scope of a Cucumber execution.
 */
public enum ExecutionType {

    /** An entire Cucumber Feature. */
    FEATURE,

    /** A single Scenario in a Cucumber Feature. */
    SCENARIO,

    /** A single Example in a single Scenario Outline in a Cucumber Feature. */
    EXAMPLE,

    /** A single tag. */
    TAG,

    /** Custom Gherkin */
    GHERKIN;
}
