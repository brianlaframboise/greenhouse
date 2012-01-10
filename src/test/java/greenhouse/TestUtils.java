package greenhouse;

public final class TestUtils {

    /** The Greenhouse projects root folder for unit tests. */
    public static final String DEMO_PROJECTS = TestUtils.class.getResource(".").getPath().toString() + "../../../demo-projects";

    /** The Greenhouse Hello World demo project root folder. */
    public static final String HELLO_WORLD_PROJECT = DEMO_PROJECTS + "/HELLO";

    /** The files directory for the Greenhouse Hello World demo project. */
    public static final String HELLO_WORLD_FILES = HELLO_WORLD_PROJECT + "/files";

    private TestUtils() {
    }
}
