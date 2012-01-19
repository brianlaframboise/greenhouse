package greenhouse.index;

import greenhouse.project.Project;

/**
 * Performs and stores {@link Index}s for Projects.
 */
public interface IndexRepository {

    /**
     * Calculates an Index for a given Project.
     * 
     * @param project The Projec to index
     * @return an Index of the given Project
     */
    Index index(Project project);

    /**
     * Returns the Index for the given Project key, if available.
     * 
     * @param projectKey A Project's key
     * @return the Index for that project, or null if none exists
     */
    Index getIndex(String projectKey);
}
