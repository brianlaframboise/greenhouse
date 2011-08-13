package kappamaki.execute;

import static kappamaki.util.Utils.joinPaths;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

import kappamaki.index.Index;
import kappamaki.index.IndexedScenario;
import kappamaki.util.Utils;

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
public class ProcessExecutor implements ScenarioExecutor {

    private final Index index;
    private final String projectRoot;

    private String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat"
            : "mvn";
    private String phase = "integration-test";

    /**
     * Creates a new ScenarioExecutor. By default output is inherited from the
     * current Process, which probably means output is directed to standard out.
     * 
     * @param index An Index containing the scenario to run
     * @param projectRoot The root directory of a cuke4duke project
     */
    public ProcessExecutor(Index index, String projectRoot) {
        this.index = index;
        this.projectRoot = projectRoot;
    }

    @Override
    public String execute(IndexedScenario scenario) {
        String output = null;
        File tempDir = null;
        try {
            long time = System.currentTimeMillis();
            tempDir = makeTempDir(time);
            copyScenarios(tempDir, scenario);
            output = executeScenarios(tempDir, time);
        } finally {
            if (tempDir != null) {
                delete(tempDir);
            }
        }
        return output;
    }

    private void copyScenarios(File tempDir, IndexedScenario scenario) {
        try {
            // Setup tagged destination file
            String root = index.getFeaturesRoot().getAbsolutePath();
            String subPath = scenario.getUri().substring(root.length() + 1);
            File tempScenario = new File(joinPaths(tempDir.getPath(), subPath));
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

    private String executeScenarios(File tempDir, long time) {
        String features = "-Dcucumber.features=\"" + tempDir.getAbsolutePath()
                + "\"";
        String tags = "-Dcucumber.tagsArg=\"--tags=@kappamaki\"";
        System.out.println("Executing: "
                + Joiner.on(' ').join(projectRoot, mvn, features, tags));
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File(projectRoot));
            builder.command(mvn, phase, features, tags);

            File output = new File(joinPaths(Utils.TEMP_DIR, "kappamaki-"
                    + time + ".output"));
            builder.redirectOutput(output);

            Process process = builder.start();
            process.waitFor();

            FileReader outputReader = new FileReader(output);
            String result = FixJava.readReader(outputReader);
            outputReader.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File makeTempDir(long time) {
        File tempScenarioDir = Utils.tempFile("kappamaki-" + time);
        if (!tempScenarioDir.mkdir()) {
            throw new RuntimeException("Could not create temp directory: "
                    + tempScenarioDir.getAbsolutePath());
        }
        return tempScenarioDir;
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
