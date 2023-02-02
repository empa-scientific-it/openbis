package ch.systemsx.cisd.common.filesystem;

import java.io.File;

/**
 * Role that implements a strategy deciding on whether a file is allowed to be overwritten.
 * 
 * @author Bernd Rinn
 */
public interface IFileOverwriteStrategy
{
    /**
     * Returns <code>true</code> if the existing <var>outputFile</var> can be overwritten and <code>false</code> otherwise.
     */
    public boolean overwriteAllowed(File outputFile);
}