package greenhouse.execute;

/** The current state of an Execution. */
public enum ExecutionState {
    /** The Execution has been submitted but is not yet running. */
    PENDING,

    /** The Execution is currently executing. */
    RUNNING,

    /** The Execution has completed, either successfully or in error. */
    COMPLETE
}
