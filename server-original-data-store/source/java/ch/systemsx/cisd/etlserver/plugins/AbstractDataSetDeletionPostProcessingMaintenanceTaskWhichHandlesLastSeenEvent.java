package ch.systemsx.cisd.etlserver.plugins;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

import java.io.File;

public abstract class AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent
        extends AbstractDataSetDeletionPostProcessingMaintenanceTask
{

    protected File lastSeenEventIdFile;

    @Override
    protected Long getLastSeenEventId()
    {
        if (lastSeenEventIdFile.exists())
        {
            String lastSeenEventId = FileUtilities.loadToString(lastSeenEventIdFile).trim();
            try
            {
                return Long.valueOf(lastSeenEventId);
            } catch (NumberFormatException e)
            {
                operationLog
                        .error("Couldn't get the last seen data set from file: "
                                + lastSeenEventIdFile.getAbsolutePath()
                                + " because the contents of that file cannot be parsed to a long value. "
                                + " As there is no last seen data set available all data sets deletions will be taken into consideration.");
                return null;
            }
        } else
        {
            return null;
        }
    }

    @Override
    protected void updateLastSeenEventId(Long eventId)
    {
        File parent = lastSeenEventIdFile.getParentFile();
        parent.mkdirs();
        File newLastSeenEventIdFile = new File(parent, lastSeenEventIdFile.getName() + ".new");
        FileUtilities.writeToFile(newLastSeenEventIdFile, String.valueOf(eventId));
        newLastSeenEventIdFile.renameTo(lastSeenEventIdFile);
    }
}
