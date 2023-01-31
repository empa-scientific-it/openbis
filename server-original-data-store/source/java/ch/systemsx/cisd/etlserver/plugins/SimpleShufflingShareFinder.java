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

import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.AbstractShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISpeedChecker;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Find the share with most free space above a specified minimum space.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleShufflingShareFinder extends AbstractShareFinder
{
    @Private
    public static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    private final long minimumFreeSpace;

    public SimpleShufflingShareFinder(Properties properties)
    {
        minimumFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);

    }

    @Override
    protected Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares,
            ISpeedChecker speedChecker)
    {
        Share shareWithMostFree = null;
        Share homeShare = null;
        long maxFreeSpace = 0;
        for (Share share : shares)
        {
            if (speedChecker.check(dataSet, share) == false)
            {
                continue;
            }
            if (share.getShareId().equals(dataSet.getDataSetShareId()))
            {
                homeShare = share;
            }
            long freeSpace = share.calculateFreeSpace();
            if (freeSpace > maxFreeSpace)
            {
                maxFreeSpace = freeSpace;
                shareWithMostFree = share;
            }
        }

        if (maxFreeSpace - dataSet.getDataSetSize() > minimumFreeSpace)
        {
            if (homeShare != shareWithMostFree)
            {
                return shareWithMostFree;
            }
        }
        return null;
    }

}
