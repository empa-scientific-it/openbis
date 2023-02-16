/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share.ShufflePriority;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * A share finder implementation. The search algorithm considers all shares with enough free space as potential result "candidates" (The free space of
 * the data set "home" share is increased by the data set size). The result (which is simply the best candidate) is elected by the following rules:
 * 
 * <pre>
 * 1. An extension share is preferred above an incoming share.
 * 2. A share whose speed matches the speed requirements of the data set is preferred. If there is more 
 * than one share matching in the same way then choose the one with speed closest to absolute value of speed hint. 
 * 3. If all candidates have the same parameters for (1) and (2) choose the share with most free space.
 * </pre>
 * 
 * The priority of points (1) and (2) can be swapped if the current location of the data set is an incoming share and it has a shuffle priority of
 * {@link ShufflePriority#SPEED}.
 * <p>
 * Generally the {@link StandardShareFinder} tends to move data sets from incoming to extension shares. A data set can only be moved from extension to
 * incoming share by an unarchiving operation if at the time of unarchiving all extension shares (regardless of their speeds) are full.
 * 
 * @author Kaloyan Enimanev
 */
public class StandardShareFinder implements IShareFinder
{

    @Private
    public static final String MINIMUM_FREE_SPACE_KEY = "incoming-shares-minimum-free-space-in-MB";

    private final long incomingShareMinFreeSpace;

    /**
     * ctor required by the framework.
     */
    public StandardShareFinder(Properties properties)
    {
        incomingShareMinFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
    }

    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        List<CandidateShare> candidates = new ArrayList<CandidateShare>();
        Share homeShare = findDataSetShare(dataSet, shares);
        for (Share share : shares)
        {
            if (share.isWithdrawShare())
            {
                continue;
            }
            if (share.equals(homeShare) || canMoveShareTo(dataSet, share))
            {
                CandidateShare candidate = createCandidate(dataSet, homeShare, share);
                candidates.add(candidate);
            }
        }

        if (false == candidates.isEmpty())
        {
            CandidateShare bestCandidate = Collections.min(candidates);
            Share bestDestinationShare = bestCandidate.getShare();
            if (false == bestDestinationShare.equals(homeShare))
            {
                return bestDestinationShare;
            }
        }
        return null;
    }

    private Share findDataSetShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        for (Share share : shares)
        {
            if (isSameShare(dataSet, share))
            {
                return share;
            }
        }
        return null;
    }

    private boolean isSameShare(SimpleDataSetInformationDTO dataSet, Share share)
    {
        String dataSetShareId = dataSet.getDataSetShareId();
        return dataSetShareId != null && dataSetShareId.equals(share.getShareId());
    }

    private boolean canMoveShareTo(SimpleDataSetInformationDTO dataSet, Share share)
    {
        long freeSpace = share.calculateFreeSpace();
        if (share.isIncoming())
        {
            freeSpace -= incomingShareMinFreeSpace;
        }
        if (dataSet.getDataSetSize() == null)
        {
            throw new NullPointerException();
        }
        boolean hasEnoughSpace = freeSpace > dataSet.getDataSetSize();
        return hasEnoughSpace;
    }

    private CandidateShare createCandidate(SimpleDataSetInformationDTO dataSet,
            Share homeShareOrNull, Share share)
    {
        long freeSpace = share.calculateFreeSpace();
        if (isSameShare(dataSet, share))
        {
            freeSpace += dataSet.getDataSetSize();
        }

        int speedMatchRating = Integer.MAX_VALUE;
        for (SpeedChecker speedChecker : SpeedChecker.values())
        {
            if (speedChecker.check(dataSet, share))
            {
                speedMatchRating = speedChecker.ordinal();
                break;
            }
        }
        int speedDeviation = Math.abs(dataSet.getSpeedHint()) - share.getSpeed();
        speedDeviation = Math.abs(speedDeviation);
        return new CandidateShare(homeShareOrNull, share, speedMatchRating, speedDeviation,
                freeSpace);
    }

    /**
     * A "candidate" destination for a data set.
     */
    private static class CandidateShare implements Comparable<CandidateShare>
    {

        private final Share homeShareOrNull;

        private final Share share;

        private final int speedMatchRating;

        private final int speedDeviation;

        private final long freeSpace;

        public CandidateShare(Share homeShareOrNull, Share share, int speedMatchRating,
                int speedDeviation, long freeSpace)
        {
            this.homeShareOrNull = homeShareOrNull;
            this.share = share;
            this.speedMatchRating = speedMatchRating;
            this.speedDeviation = speedDeviation;
            this.freeSpace = freeSpace;
        }

        @Override
        public int compareTo(CandidateShare otherCandidate)
        {
            int incomingToExtensionComparison = compareIncomingToExtension(otherCandidate);
            int speedComparison = compareSpeeds(otherCandidate);

            int moreImportantCriteria;
            int lessImportantCriteria;

            if (homeShareOrNull != null && homeShareOrNull.isIncoming()
                    && homeShareOrNull.getShufflePriority() == ShufflePriority.SPEED)
            {
                moreImportantCriteria = speedComparison;
                lessImportantCriteria = incomingToExtensionComparison;
            } else
            {
                moreImportantCriteria = incomingToExtensionComparison;
                lessImportantCriteria = speedComparison;
            }

            if (moreImportantCriteria != 0)
            {
                return moreImportantCriteria;
            }
            if (lessImportantCriteria != 0)
            {
                return lessImportantCriteria;
            }

            return compareFreeSpace(otherCandidate);
        }

        private int compareFreeSpace(CandidateShare otherCandidate)
        {
            long freeSpaceDiff = freeSpace - otherCandidate.freeSpace;
            if (freeSpaceDiff != 0)
            {
                return freeSpaceDiff > 0 ? -1 : 1;
            }
            return 0;
        }

        private int compareIncomingToExtension(CandidateShare otherCandidate)
        {
            if (share.isIncoming() != otherCandidate.share.isIncoming())
            {
                return (share.isIncoming()) ? 1 : -1;
            }
            return 0;
        }

        private int compareSpeeds(CandidateShare otherCandidate)
        {
            int speedMatchRatingDiff = speedMatchRating - otherCandidate.speedMatchRating;
            if (speedMatchRatingDiff == 0)
            {
                int speedDeviationDiff = speedDeviation - otherCandidate.speedDeviation;
                if (speedDeviationDiff != 0)
                {
                    return speedDeviationDiff;
                }
            } else
            {
                return speedMatchRatingDiff;

            }
            return 0;
        }

        public Share getShare()
        {
            return share;
        }

    }
}
