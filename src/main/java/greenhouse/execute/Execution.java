package greenhouse.execute;

import static greenhouse.util.Utils.file;
import greenhouse.project.Context;
import greenhouse.util.Utils;

import java.io.File;
import java.util.concurrent.Future;

/**
 * An execution of Cucumber against a given Project.
 */
public class Execution {

    private final ExecutionRequest request;

    /** The symbolic key for this execution. */
    private final ExecutionKey key;

    /**
     * The directory into which all input for this execution is placed and all
     * output from this execution is generated.
     */
    private final File executionDirectory;

    /**
     * The file into which the console output for this execution will be
     * written.
     */
    private final File outputFile;

    /** The Future tied to this execution. */
    private Future<Void> result;

    /** The actual command used to perform this task. */
    private String command;
    /** The time at which the task started. */
    private long start = 0;
    /** The time at which the task completed, either successfully or in error. */
    private long end = 0;
    /** The current state of this task. */
    private ExecutionState state = ExecutionState.PENDING;

    public Execution(ExecutionRequest request, File projectRootDir, int executionNumber) {
        this.request = request;
        key = new ExecutionKey(request.getProjectKey(), executionNumber);
        executionDirectory = Utils.file(projectRootDir.getAbsolutePath(), "executions", Integer.toString(executionNumber));
        if (!executionDirectory.exists() && !executionDirectory.mkdirs()) {
            throw new RuntimeException("Could not create execution directory for " + key);
        }
        outputFile = file(executionDirectory.getAbsolutePath(), "output.txt");
    }

    /**
     * The file containing the HTML report for this execution. The only time
     * this file may exist is when this Execution is in state COMPLETE. However,
     * note that even in that case this file may not have been generated if
     * there was a Cucumber error.
     * 
     * @return the location of the html Cucumber execution report
     */
    public File getReportFile() {
        return file(executionDirectory.getAbsolutePath(), "report.html");
    }

    /**
     * Returns the full console output generated after this execution has
     * finished. If this execution is still running, this method will block
     * until this execution is complete.
     * 
     * @return the complete console output from this execution
     */
    public String getCompletedOutput() {
        try {
            result.get();
        } catch (Exception e) {
            throw new RuntimeException("Unable to wait for future result", e);
        }
        return readOutput();
    }

    /**
     * Returns any console output that has been generated by this execution. If
     * this execution has generated no output, a blank string is returned.
     * Otherwise this method returns whatever output has been generated at the
     * moment this method is called. It is safe to call this method while the
     * execution is running.
     * 
     * @return whatever console output this execution has generated so far
     */
    public String getOutput() {
        if (outputFile.exists()) {
            return readOutput();
        } else {
            return "";
        }
    }

    private String readOutput() {
        return Utils.readContents(outputFile.getAbsolutePath());
    }

    public String getReportHtml() {
        return Utils.readContents(getReportFile().getAbsolutePath());
    }

    public String getDescription() {
        return request.getDescription();
    }

    public String getCommand() {
        return command;
    }

    public long getEnd() {
        return end;
    }

    public ExecutionKey getKey() {
        return key;
    }

    public Context getContext() {
        return request.getContext();
    }

    public String getFeatureName() {
        return request.getFeature();
    }

    public String getScenarioName() {
        return request.getScenario();
    }

    public ExecutionType getType() {
        return request.getType();
    }

    public int getExampleLine() {
        return request.getLine();
    }

    public String getTag() {
        return request.getTag();
    }

    public String getGherkin() {
        return request.getGherkin();
    }

    public File getOutputFile() {
        return outputFile;
    }

    public Future<Void> getResult() {
        return result;
    }

    public File getExecutionDirectory() {
        return executionDirectory;
    }

    public long getStart() {
        return start;
    }

    public ExecutionState getState() {
        return state;
    }

    void setCommand(String command) {
        this.command = command;
    }

    void setEnd(long end) {
        this.end = end;
    }

    void setResult(Future<Void> result) {
        this.result = result;
    }

    void setStart(long start) {
        this.start = start;
    }

    void setState(ExecutionState state) {
        this.state = state;
    }

}
