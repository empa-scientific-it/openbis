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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import static org.apache.commons.io.FileUtils.ONE_KB;
import static org.apache.commons.io.FileUtils.ONE_MB;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Simple share finder tries to find the external share with maximum free space larger than the size of the data set to be moved. If no such external
 * share can be found it tries to find the incoming share with maximum free space larger than the size of the data set to be moved and which is not
 * the share of the data set.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleShareFinder extends AbstractShareFinder
{
    public SimpleShareFinder(Properties properties)
    {
    }

    @Override
    protected Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares,
            ISpeedChecker speedChecker)
    {
        long dataSetSize = dataSet.getDataSetSize();
        // 10% but not more than 1 MB are added to the data set size to take into account that
        // creating directories consume disk space.
        dataSetSize += Math.max(ONE_KB, Math.min(ONE_MB, dataSet.getDataSetSize() / 10));
        String dataSetShareId = dataSet.getDataSetShareId();
        long dataSetShareFreeSpace = 0;
        boolean dataSetShareIncoming = false;
        Share incomingShareWithMostFree = null;
        long incomingMaxFreeSpace = dataSetSize;
        Share extensionShareWithMostFree = null;
        long extensionsMaxFreeSpace = dataSetSize;
        for (Share share : shares)
        {
            if (speedChecker.check(dataSet, share) == false)
            {
                continue;
            }
            long freeSpace = share.calculateFreeSpace();
            String shareId = share.getShareId();
            if (dataSetShareId != null && dataSetShareId.equals(shareId))
            {
                dataSetShareFreeSpace = freeSpace;
                dataSetShareIncoming = share.isIncoming();
                continue;
            }
            if (share.isIncoming())
            {
                if (freeSpace > incomingMaxFreeSpace)
                {
                    incomingMaxFreeSpace = freeSpace;
                    incomingShareWithMostFree = share;
                }
            } else
            {
                if (freeSpace > extensionsMaxFreeSpace)
                {
                    extensionsMaxFreeSpace = freeSpace;
                    extensionShareWithMostFree = share;
                }
            }
        }
        if (extensionShareWithMostFree != null)
        {
            if (dataSetShareIncoming
                    || extensionsMaxFreeSpace - dataSetSize > dataSetShareFreeSpace)
            {
                return extensionShareWithMostFree;
            }
        } else if (incomingShareWithMostFree != null)
        {
            if (dataSetShareIncoming == false
                    || incomingMaxFreeSpace - dataSetSize > dataSetShareFreeSpace)
            {
                return incomingShareWithMostFree;
            }
        }
        return null;
    }

}
