/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Represents a share of a segmented store. Holds the root directory of the share as well as the
 * data sets. It is able to calculate the free disk space.
 * 
 * @author Franz-Josef Elmer
 */
public final class Share
{
    private static final String SHARE_PROPERTIES_FILENAME = "share.properties";

    public static final Comparator<SimpleDataSetInformationDTO> DATA_SET_SIZE_COMPARATOR =
            new Comparator<SimpleDataSetInformationDTO>()
                {
                    public int compare(SimpleDataSetInformationDTO o1,
                            SimpleDataSetInformationDTO o2)
                    {
                        long size1 = o1.getDataSetSize();
                        long size2 = o2.getDataSetSize();
                        return size1 < size2 ? 1 : (size1 > size2 ? -1 : 0);
                    }
                };

    /**
     * different factors that have priority when moving a data set to the share.
     */
    public static enum ShufflePriority
    {
        /**
         * always respect the speed hint of a data set when moving.
         */
        SPEED,
        /**
         * allows a shuffling operation to ignore the speed hints when moving from incoming to
         * extension share.
         */
        MOVE_TO_EXTENSION;
    }

    private final File share;

    private final IFreeSpaceProvider freeSpaceProvider;

    private final String shareId;

    private final int speed;

    private final List<SimpleDataSetInformationDTO> dataSets =
            new ArrayList<SimpleDataSetInformationDTO>();

    private boolean incoming;

    private long size;

    private ShufflePriority shufflePriority = ShufflePriority.SPEED;

    private boolean withdrawShare;

    public Share(File share, int speed, IFreeSpaceProvider freeSpaceProvider)
    {
        this.share = share;
        this.speed = speed;
        this.freeSpaceProvider = freeSpaceProvider;
        shareId = share.getName();
    }

    /**
     * Returns the share Id of this share.
     */
    public String getShareId()
    {
        return shareId;
    }

    /**
     * Returns the speed of this share.
     */
    public int getSpeed()
    {
        return speed;
    }

    public void setIncoming(boolean incoming)
    {
        this.incoming = incoming;
    }

    /**
     * Returns <code>true</code> if this is an incoming share.
     */
    public boolean isIncoming()
    {
        return incoming;
    }

    /**
     * Returns the root directory of this share.
     */
    public File getShare()
    {
        return share;
    }

    /**
     * If the share has a properties file (named share.properties and located at the root of the
     * share), return it. Return null otherwise.
     * 
     * @return The properties for the share or null
     */
    public Properties tryShareProperties()
    {
        File propsFile = new File(share, SHARE_PROPERTIES_FILENAME);
        if (false == propsFile.exists())
        {
            return null;
        }
        Properties props = new Properties();
        return props;
    }

    /**
     * Calculates the actual free space (in bytes) of the hard disk on which this share resides.
     */
    public long calculateFreeSpace()
    {
        try
        {
            return 1024 * freeSpaceProvider.freeSpaceKb(new HostAwareFile(share));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void addDataSet(SimpleDataSetInformationDTO dataSet)
    {
        dataSets.add(dataSet);
        size += dataSet.getDataSetSize();
    }

    /**
     * Returns all data sets of this shared ordered by size starting with the largest data set.
     */
    public List<SimpleDataSetInformationDTO> getDataSetsOrderedBySize()
    {
        Collections.sort(dataSets, DATA_SET_SIZE_COMPARATOR);
        return dataSets;
    }

    /**
     * Returns the total size (in bytes) of all data sets.
     */
    public long getTotalSizeOfDataSets()
    {
        return size;
    }

    public ShufflePriority getShufflePriority()
    {
        return shufflePriority;
    }

    public void setShufflePriority(ShufflePriority shufflePriority)
    {
        this.shufflePriority = shufflePriority;
    }

    public boolean isWithdrawShare()
    {
        return withdrawShare;
    }

    public void setWithdrawShare(boolean withdrawShare)
    {
        this.withdrawShare = withdrawShare;
    }
}