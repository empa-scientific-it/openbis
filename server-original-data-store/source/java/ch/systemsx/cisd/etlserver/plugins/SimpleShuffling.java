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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.common.logging.LogLevel.INFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.postregistration.EagerShufflingTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.TaskExecutor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Simple shuffling which moves data sets from full shares to the share with initial most free space until it is full.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleShuffling implements ISegmentedStoreShuffling
{
    @Private
    static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    private static final class ShareAndFreeSpace
    {
        private final Share share;

        private long freeSpace;

        ShareAndFreeSpace(Share share)
        {
            this.share = share;
            freeSpace = share.calculateFreeSpace();
        }

        long getFreeSpace()
        {
            return freeSpace;
        }

        Share getShare()
        {
            return share;
        }
    }

    private final long minimumFreeSpace;

    private IPostRegistrationTask shufflingTask;

    private TaskExecutor taskExecutor;

    public SimpleShuffling(Properties properties)
    {
        this(properties, new EagerShufflingTask(properties, ServiceProvider.getOpenBISService()));
    }

    SimpleShuffling(Properties properties, IPostRegistrationTask shufflingTask)
    {
        this.shufflingTask = shufflingTask;
        minimumFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
        taskExecutor = new TaskExecutor(properties, LogFactory.getLogger(LogCategory.OPERATION,
                SimpleShuffling.class));
    }

    @Override
    public void init(ISimpleLogger logger)
    {
        taskExecutor.cleanup();
        logger.log(LogLevel.INFO, "Simple shuffling strategy initialized");
    }

    @Override
    public void shuffleDataSets(List<Share> sourceShares, List<Share> targetShares,
            IEncapsulatedOpenBISService service, IDataSetMover dataSetMover, ISimpleLogger logger)
    {
        List<ShareAndFreeSpace> fullShares = getFullShares(sourceShares);
        for (ShareAndFreeSpace fullShare : fullShares)
        {
            Share share = fullShare.getShare();
            List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
            long initalFreeSpaceAboveMinimum = fullShare.getFreeSpace() - minimumFreeSpace;

            int numberOfDataSetsToMove;
            if (share.isWithdrawShare())
            {
                numberOfDataSetsToMove = dataSets.size();
                logger.log(INFO, "All " + numberOfDataSetsToMove
                        + " data sets should be moved for share " + share.getShareId());
            }
            else
            {
                logger.log(INFO,
                        "BEGIN Computing number of data sets to be moved for share " + share.getShareId());
                numberOfDataSetsToMove = getNumberOfDataSetsToMove(dataSets,
                        initalFreeSpaceAboveMinimum, logger);
                logger.log(INFO,
                        "END Computing number of data sets to move for share " + share.getShareId());
                if (numberOfDataSetsToMove < 0)
                {
                    throw new IllegalStateException("Share " + share.getShareId()
                            + " has not enough free space even if it is empty.");
                }
            }
            for (int i = 0; i < numberOfDataSetsToMove; i++)
            {
                SimpleDataSetInformationDTO dataSet = dataSets.get(i);
                try
                {
                    taskExecutor.execute(shufflingTask, "shuffling", dataSet.getDataSetCode(), false);
                } catch (Throwable ex)
                {
                    // ignore because it has already been logged. Try the next data set.
                }
            }
        }
    }

    private int getNumberOfDataSetsToMove(List<SimpleDataSetInformationDTO> dataSets,
            long initalFreeSpaceAboveMinimum, ISimpleLogger logger)
    {
        long freeSpaceAboveMinimum = initalFreeSpaceAboveMinimum;

        long spaceBelowMinimum = freeSpaceAboveMinimum * -1;
        float spaceBelowMinimumkB = spaceBelowMinimum / 1024.f;
        float spaceBelowMinimumMB = spaceBelowMinimumkB / 1024.f;
        String freeSpaceString =
                String.format("\tSpace needed to free: %d bytes (%.2f kB, %.2f MB)",
                        spaceBelowMinimum, spaceBelowMinimumkB, spaceBelowMinimumMB);
        logger.log(INFO, freeSpaceString);
        logger.log(INFO, "\tInspecting " + dataSets.size() + " data sets.");
        for (int i = 0; i < dataSets.size(); i++)
        {
            if (freeSpaceAboveMinimum > 0)
            {
                logger.log(INFO, "\t" + i + " data sets to move, available space : "
                        + freeSpaceAboveMinimum);
                return i;
            }
            freeSpaceAboveMinimum += dataSets.get(i).getDataSetSize();
        }
        return freeSpaceAboveMinimum > 0 ? dataSets.size() : -1;
    }

    private List<ShareAndFreeSpace> getFullShares(List<Share> sourceShares)
    {
        List<ShareAndFreeSpace> fullShares = new ArrayList<ShareAndFreeSpace>();
        for (ShareAndFreeSpace shareState : getSortedShares(sourceShares))
        {
            if (shareState.getShare().isWithdrawShare() || shareState.getFreeSpace() < minimumFreeSpace)
            {
                fullShares.add(shareState);
            }
        }
        return fullShares;
    }

    private List<ShareAndFreeSpace> getSortedShares(List<Share> shares)
    {
        List<ShareAndFreeSpace> shareStates = new ArrayList<ShareAndFreeSpace>();
        for (Share share : shares)
        {
            shareStates.add(new ShareAndFreeSpace(share));
        }
        Collections.sort(shareStates, new Comparator<ShareAndFreeSpace>()
            {
                @Override
                public int compare(ShareAndFreeSpace o1, ShareAndFreeSpace o2)
                {
                    long s1 = o1.getFreeSpace();
                    long s2 = o2.getFreeSpace();
                    return s1 < s2 ? -1 : (s1 > s2 ? 1 : 0);
                }
            });
        return shareStates;
    }

}
