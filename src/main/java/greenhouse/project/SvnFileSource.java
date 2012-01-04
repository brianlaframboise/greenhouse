package greenhouse.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.io.IOUtils;

/**
 * A FileSource that checks out and updates a project from a target Subversion
 * url.
 */
public class SvnFileSource implements FileSource {

    private final File projectRoot;
    private final File files;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a new SvnFileSource.
     * 
     * @param projectRoot The Greenhouse root project directory
     * @param files The files directory into which the project's files will be
     *            checked out
     * @param url The url at which the project can be checked out
     * @param username The subversion username used to check out the project
     * @param password The subversion password used to check out the project
     */
    public SvnFileSource(File projectRoot, File files, String url, String username, String password) {
        this.projectRoot = projectRoot;
        this.files = files;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public File getDirectory() {
        return files;
    }

    @Override
    public void initialize() {
        if (files.exists()) {
            update();
        } else {
            checkout();
        }
    }

    private void checkout() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(projectRoot);
        builder.redirectErrorStream(true);

        List<String> args = basicArgs();
        args.add("checkout");
        args.add(url);
        args.add(files.getAbsolutePath());
        builder.command(args);

        try {
            System.out.println("Checking out " + url + " into " + files);
            Process process = builder.start();
            System.out.println(IOUtils.toString(process.getInputStream()));

            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Unable to checkout project from " + url, e);
        }
        System.out.println("Checkout complete: " + url);
    }

    @Override
    public void update() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(files);
        builder.redirectErrorStream(true);

        List<String> args = basicArgs();
        args.add("update");
        builder.command(args);

        try {
            System.out.println("Updating: " + files);
            Process process = builder.start();
            System.out.println(IOUtils.toString(process.getInputStream()));

            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Unable to update " + files, e);
        }
        System.out.println("Update complete: " + files);
    }

    private List<String> basicArgs() {
        List<String> args = new ArrayList<String>();
        args.add("svn");
        args.add("--no-auth-cache");
        args.add("--non-interactive");
        args.add("--trust-server-cert");
        args.add("--username");
        args.add(username);
        args.add("--password");
        args.add(password);
        return args;
    }

}
