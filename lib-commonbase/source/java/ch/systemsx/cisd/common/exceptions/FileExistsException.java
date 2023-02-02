package ch.systemsx.cisd.common.exceptions;

import java.io.File;
import java.io.IOException;

/**
 * An exception indicating that an output file already exists.
 * 
 * @author Bernd Rinn
 */
public class FileExistsException extends IOException
{
    private static final long serialVersionUID = -3387516993124229948L;

    public FileExistsException(File file)
    {
        super("Output file '" + file.getAbsolutePath() + "' already exists.");
    }
}