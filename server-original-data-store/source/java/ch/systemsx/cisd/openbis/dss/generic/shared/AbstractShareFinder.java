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

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Abstract implementation which fulfills the contract of {@link IShareFinder} concerning speed hint.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractShareFinder implements IShareFinder
{
    /**
     * Tries to find a share fulfilling speed hint contract.
     */
    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        SpeedChecker[] values = SpeedChecker.values();
        for (SpeedChecker speedChecker : values)
        {
            Share share = tryToFindShare(dataSet, shares, speedChecker);
            if (share != null)
            {
                assertSpeedChecker(dataSet, share, speedChecker);
                return share;
            }
        }
        return null;
    }

    private void assertSpeedChecker(SimpleDataSetInformationDTO dataSet, Share share,
            SpeedChecker speedChecker)
    {
        if (speedChecker.check(dataSet, share) == false)
        {
            throw new AssertionError("Found share " + share.getShareId() + " has speed "
                    + share.getSpeed() + " but data set " + dataSet.getDataSetCode()
                    + " has speed hint " + dataSet.getSpeedHint()
                    + ". This violates speed checker " + speedChecker + ".");
        }
    }

    /**
     * Tries to find a share from the specified shares to whom the specified data set can be moved. The returned share has to fulfill specified speed
     * checker. That is, if return value <code>share != null</code> then <code>speedChecker(dataSet, share) == true</code>.
     */
    protected abstract Share tryToFindShare(SimpleDataSetInformationDTO dataSet,
            List<Share> shares, ISpeedChecker speedChecker);

}
