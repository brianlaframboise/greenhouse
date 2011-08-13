package kappamaki.execute;

import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

import kappamaki.index.Index;
import kappamaki.index.IndexedScenario;

import com.google.common.base.Joiner;

/**
 * Executes an IndexedScenario by:
 * 
 * <ol>
 * <li>Creating a kappamaki-TIMESTAMP directory in the temp directory</li>
 * <li>Copying the scenario's feature file to that directory while tagging the
 * specific scenario with @kappamaki</li>
 * <li>Executing a Maven Process against a cuke4duke project to run only
 * scenarios tagged @kappamaki</li>
 * <li>Deleting the temp directory</li>
 * </ol>
 * 
 * The output of the process execution may be redirected to a provided file.
 * 
 * The cuke4duke project must accept a parameter "cucumber.tagsArg" that will
 * pass the cucumber tags argument (ex: "--tags=@tagName") through to the
 * underlying Cucumber execution.
 */
public class ScenarioExecutor {

    private static final String SEPARATOR = System
            .getProperty("file.separator");
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private final Index index;
    private final IndexedScenario scenario;
    private final String projectRoot;

    private String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat"
            : "mvn";
    private String phase = "integration-test";

    private File tempDir;
    private File output;

    private final long time = System.currentTimeMillis();

    /**
     * Creates a new ScenarioExecutor. By default output is inherited from the
     * current Process, which probably means output is directed to standard out.
     * 
     * @param index An Index containing the scenario to run
     * @param scenario The scenario to run
     * @param projectRoot The root directory of a cuke4duke project
     */
    public ScenarioExecutor(Index index, IndexedScenario scenario,
            String projectRoot) {
        this.index = index;
        this.scenario = scenario;
        this.projectRoot = projectRoot;
    }

    /**
     * Runs the scenario(s) provided.
     */
    public void execute() {
        try {
            makeTempDir();
            copyScenarios();
            executeScenarios();
        } finally {
            delete(tempDir);
        }
    }

    private void copyScenarios() {
        try {
            // Setup tagged destination file
            String root = index.getFeaturesRoot().getAbsolutePath();
            String subPath = scenario.getUri().substring(root.length() + 1);
            File tempScenario = new File(tempDir.getPath() + SEPARATOR
                    + subPath);
            Writer tempFile = new FileWriter(tempScenario);

            // Load source file
            FileReader reader = new FileReader(scenario.getUri());
            String gherkin = FixJava.readReader(reader);
            reader.close();

            // Parse and tage source into destination
            Tagger tagger = new Tagger(scenario, tempFile);
            Parser parser = new Parser(tagger);
            System.out.println("Tagging " + scenario.getUri() + " into "
                    + tempScenario.getPath());
            parser.parse(gherkin, tempScenario.getAbsolutePath(), 0);

            tempFile.flush();
            tempFile.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not copy scenarios");
        }
    }

    private void executeScenarios() {
        String features = "-Dcucumber.features=\"" + tempDir.getAbsolutePath()
                + "\"";
        String tags = "-Dcucumber.tagsArg=\"--tags=@kappamaki\"";
        System.out.println("Executing: "
                + Joiner.on(' ').join(projectRoot, mvn, features, tags));
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File(projectRoot));
            builder.command(mvn, phase, features, tags);
            if (output == null) {
                builder.inheritIO();
            } else {
                builder.redirectOutput(output);
            }
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void makeTempDir() {
        File tempScenarioDir = new File(TMP_DIR + SEPARATOR + "kappamaki-"
                + time + SEPARATOR);
        if (!tempScenarioDir.mkdir()) {
            throw new RuntimeException("Could not create temp directory: "
                    + tempScenarioDir.getAbsolutePath());
        }
        tempDir = tempScenarioDir;
    }

    private void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new RuntimeException("Could not delete " + f.getPath());
        }
    }

    /**
     * Creates a File in temp directory into which process output can be
     * redirected.
     * 
     * @return a new File
     */
    public File makeOutputFile() {
        return new File(TMP_DIR + SEPARATOR + "kappamaki-" + time + ".output");
    }

    /**
     * Sets the file into which output is redirected. Null means output will be
     * inherited.
     * 
     * @param file the process output file location
     */
    public void setOutput(File file) {
        output = file;
    }

    /**
     * Sets the Maven executable name to use. By default this is "mvn.bat" on
     * Windows and "mvn" on all other platforms, which therefore assumes Maven
     * is in the path. If that is not the case, the absolute path to the Maven
     * executable can be provided here.
     * 
     * @param mvn The maven executable path/name
     */
    public void setMvn(String mvn) {
        this.mvn = mvn;
    }

    /**
     * Sets the Maven phase to execute. This is "integration-test" by default.
     * 
     * @param phase the new Maven phase to execute
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

}
