package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;

/**
 * Creates soft links commands.
 * 
 * @author Izabela Adamczyk
 */
public class SoftLinkMaker
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SoftLinkMaker.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, SoftLinkMaker.class);

    private final File lnExec;

    private SoftLinkMaker()
    {
        lnExec = OSUtilities.findExecutable("ln");
        if (lnExec == null)
        {
            throw new IllegalStateException("Linking command not found");
        }
    }

    private final List<String> createCommand(final File sourceFile, final File targetDir)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(lnExec.getAbsolutePath());
        tokens.add("-sf");
        tokens.add(sourceFile.getAbsolutePath());
        tokens.add(targetDir.getAbsolutePath());
        return tokens;
    }

    /**
     * @param targetDirOrFile if directory, a file with the same name as the source file which links to a source will be created. Specify a file to
     *            change the name of the symbolic link file as well.
     */
    public static boolean createSymbolicLink(File sourceFile, File targetDirOrFile)
    {
        return ProcessExecutionHelper.runAndLog(new SoftLinkMaker().createCommand(sourceFile,
                targetDirOrFile), operationLog, machineLog);
    }
}