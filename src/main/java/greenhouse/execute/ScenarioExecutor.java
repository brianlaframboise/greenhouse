package greenhouse.execute;

public interface ScenarioExecutor {

    /**
     * Creates and schedules an Execution to be performed as soon as possible.
     * 
     * @param request The request from which an Execution can be created, once
     *        scheduled
     * @return the ExecutionKey that can be used to retrieve and monitor the
     *         Execution
     */
    ExecutionKey execute(ExecutionRequest request);

    /**
     * Returns the Execution associated with the given key. Note that the state
     * of this Execution may be modified after it has been returned.
     * 
     * @param executionKey The key matching an Execution
     * @return the associated execution or null if no Execution exists for the
     *         given key
     */
    Execution getExecution(ExecutionKey executionKey);

    /**
     * Returns all Executions, past and present, that have been scheduled by
     * this executor. The state of any Execution contained within may continue
     * to change after this method returns.
     * 
     * @return all executions known to this executor
     */
    Iterable<Execution> getAllExecutions();
}
