package greenhouse.project;

import greenhouse.util.Utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * A FileSource that copies an unversioned project on the local file system into
 * the project files directory.
 */
public class LocalFileSource implements FileSource {

    private final File source;
    private final File files;

    /**
     * Creates a new LocalFileSource.
     * 
     * @param source A project root directory that exists on the local file
     *            system.
     * @param files The greenhouse project files directory
     */
    public LocalFileSource(String source, File files) {
        this.source = new File(source);
        this.files = files;
    }

    @Override
    public File getDirectory() {
        return files;
    }

    @Override
    public void initialize() {
        Utils.delete(files);
        copySourceToFiles();
    }

    @Override
    public void update() {
        copySourceToFiles();
    }

    private void copySourceToFiles() {
        try {
            FileUtils.copyDirectory(source, files);
        } catch (final IOException e) {
            throw new RuntimeException("Error while copying " + source.getAbsolutePath() + " to " + files.getAbsolutePath(), e);
        }
    }

}
