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

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * A share finder which first searches for the extension share with most free space which matches speed. If nothing is found it does the same also for
 * all extension shares but speed needs not to match but speed hint needs to be respected. If this isn't working {@link SimpleShareFinder} is used
 * ignoring speed hint.
 * 
 * @author Franz-Josef Elmer
 */
public class SpeedOptimizedShareFinder implements IShareFinder
{
    private final SimpleShareFinder simpleFinder;

    public SpeedOptimizedShareFinder(Properties properties)
    {
        simpleFinder = new SimpleShareFinder(properties);
    }

    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        Share share = tryToFindExtensionShare(dataSet, shares, SpeedChecker.MATCHING_CHECKER);
        if (share != null)
        {
            return share;
        }
        share =
                tryToFindExtensionShare(dataSet, shares,
                        SpeedChecker.RESPECTING_SPEED_HINT_CHECKER);
        if (share != null)
        {
            return share;
        }
        return simpleFinder.tryToFindShare(dataSet, shares,
                SpeedChecker.IGNORING_SPEED_HINT_CHECKER);
    }

    private Share tryToFindExtensionShare(SimpleDataSetInformationDTO dataSet, List<Share> shares,
            ISpeedChecker speedChecker)
    {
        Share result = null;
        long maxFreeSpace = dataSet.getDataSetSize();
        for (Share share : shares)
        {
            if (share.isIncoming() || speedChecker.check(dataSet, share) == false)
            {
                continue;
            }
            long freeSpace = share.calculateFreeSpace();
            if (freeSpace > maxFreeSpace)
            {
                maxFreeSpace = freeSpace;
                result = share;
            }
        }
        return result;
    }

}
