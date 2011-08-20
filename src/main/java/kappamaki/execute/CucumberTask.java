package kappamaki.execute;

import java.io.File;
import java.util.concurrent.Future;

public class CucumberTask {
    private final File output;
    private final Future<String> result;

    public CucumberTask(File output, Future<String> result) {
        this.output = output;
        this.result = result;
    }

    public File getOutput() {
        return output;
    }

    public Future<String> getResult() {
        return result;
    }

}