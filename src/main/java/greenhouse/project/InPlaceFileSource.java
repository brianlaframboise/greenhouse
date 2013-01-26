package greenhouse.project;

import java.io.File;

/**
 * A FileSource that assumes all necessary files already exist in the project
 * files directory and therefore does no initialzation or updating. Useful for
 * unit testing.
 */
public class InPlaceFileSource implements FileSource {

    private final File files;

    public InPlaceFileSource(File filesDirectory) {
        files = filesDirectory;
    }

    @Override
    public File getDirectory() {
        return files;
    }

    @Override
    public void initialize() {
    }

    @Override
    public String update() {
        return "No files to update.";
    }

}
