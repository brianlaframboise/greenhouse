package greenhouse.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter that accepts only directories.
 */
public final class DirectoryFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory();
    }
}