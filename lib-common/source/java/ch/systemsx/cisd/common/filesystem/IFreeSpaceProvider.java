package ch.systemsx.cisd.common.filesystem;

import java.io.IOException;

/**
 * Each implementation is able to return the free space on a drive or volume.
 * 
 * @author Christian Ribeaud
 */
public interface IFreeSpaceProvider
{

    /**
     * Returns the free space on a drive or volume in kilobytes by invoking the command line.
     */
    public long freeSpaceKb(final HostAwareFile path) throws IOException;
}