package greenhouse.project;

import java.io.File;

/**
 * A FileSource that uses an unversioned project on the local file system. As
 * such, the initialize and update operations are no-ops.
 */
public class LocalFileSource implements FileSource {

    private final File directory;

    /**
     * Creates a new LocalFileSource.
     * 
     * @param directory A project root directory that exists on the local file
     *            system.
     */
    public LocalFileSource(String directory) {
        this.directory = new File(directory);
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
    }

}
