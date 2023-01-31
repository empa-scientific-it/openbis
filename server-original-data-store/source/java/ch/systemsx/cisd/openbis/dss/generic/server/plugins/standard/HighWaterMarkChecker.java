package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher.HighwaterMarkState;

/**
 * Checks if the space available is larger than specified value.
 * 
 * @author Izabela Adamczyk
 */
public class HighWaterMarkChecker implements IStatusChecker, Serializable
{
    private static final long serialVersionUID = 1L;

    private final long highWaterMark; // amount of free space per item in KB

    private final File highWaterMarkPath;

    public HighWaterMarkChecker(long archiveHighWaterMark, File archiveHighWaterMarkPath)
    {
        this.highWaterMark = archiveHighWaterMark;
        this.highWaterMarkPath = archiveHighWaterMarkPath;
    }

    @Override
    public Status check(int numberOfItems)
    {
        HighwaterMarkWatcher w = new HighwaterMarkWatcher(highWaterMark * numberOfItems);
        HighwaterMarkState state = w.getHighwaterMarkState(new HostAwareFile(highWaterMarkPath));
        if (HighwaterMarkWatcher.isBelow(state))
        {
            String canonicalPath = highWaterMarkPath.getPath();
            String mark = HighwaterMarkWatcher.displayKilobyteValue(state.getHighwaterMark());
            String space = HighwaterMarkWatcher.displayKilobyteValue(state.getFreeSpace());
            String message =
                    String.format("Free space on '%s': %s, highwater mark: %s.", canonicalPath,
                            space, mark);
            return Status.createError(message);
        } else
        {
            return Status.OK;
        }

    }
}