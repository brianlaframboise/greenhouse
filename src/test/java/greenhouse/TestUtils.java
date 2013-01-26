package greenhouse;

public final class TestUtils {

    /** The Greenhouse projects root folder for unit tests. */
    public static final String DEMO_PROJECTS = TestUtils.class.getResource(".").getPath().toString() + "../../../demo-projects";

    /** The Greenhouse cuke4duke demo project root folder. */
    public static final String CUKE4DUKE_PROJECT = DEMO_PROJECTS + "/CUKE4DUKE";

    /** The files directory for the Greenhousecuke4duke demo project. */
    public static final String CUKE4DUKE_FILES = CUKE4DUKE_PROJECT + "/files";

    /** The Greenhouse Cucumber-JVM demo project root folder. */
    public static final String CUCUMBER_JVM_PROJECT = DEMO_PROJECTS + "/CJVM";

    /** The files directory for the Greenhouse Cucumber-JVM demo project. */
    public static final String CUCUMBER_JVM_FILES = CUCUMBER_JVM_PROJECT + "/files";

    private TestUtils() {
    }
}
