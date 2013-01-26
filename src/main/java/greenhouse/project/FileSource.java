package greenhouse.project;

import java.io.File;

/**
 * A FileSource provides a means to get and update a project's sources from a
 * version control or file system.
 */
public interface FileSource {

    /**
     * The directory into which this FileSource will materialize the associated
     * project's files. Once initialized, this directory will contain all files
     * at and below the project root, such as the pom.xml and the features
     * folder with Cucumber scenarios.
     * 
     * @return the project files root directory
     */
    File getDirectory();

    /**
     * Initializes the files, such as from a version control system (ie does an
     * svn checkout).
     */
    void initialize();

    /**
     * Updates to the latest version of the project source files, if applicable.
     * 
     * @return text describing the result of the update, such as output from the
     *         update command
     */
    String update();
}
