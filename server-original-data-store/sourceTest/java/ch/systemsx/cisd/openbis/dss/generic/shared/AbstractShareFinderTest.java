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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractShareFinderTest extends AssertJUnit
{
    private static final String DATA_SET_CODE = "ds-1";

    private static final List<Share> SHARES = Collections.emptyList();

    private static final class MockShareFinder extends AbstractShareFinder
    {
        private final Share[] returnValues;

        private int index;

        private SimpleDataSetInformationDTO recordedDataSet;

        private List<Share> recordedShares;

        MockShareFinder(Share... shares)
        {
            this.returnValues = shares;
        }

        @Override
        protected Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares,
                ISpeedChecker speedChecker)
        {
            recordedDataSet = dataSet;
            recordedShares = shares;
            return returnValues[index++];
        }

        void verify(SimpleDataSetInformationDTO dataSet, List<Share> shares)
        {
            assertSame(dataSet, recordedDataSet);
            assertSame(shares, recordedShares);
            assertEquals(index, returnValues.length);
        }
    }

    @Test
    public void testFindMatchingShareForPositiveHint()
    {
        Share share = share(20);
        MockShareFinder finder = new MockShareFinder(share);

        SimpleDataSetInformationDTO dataSet = dataSet(20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindMatchingShareForNegativeHint()
    {
        Share share = share(20);
        MockShareFinder finder = new MockShareFinder(share);

        SimpleDataSetInformationDTO dataSet = dataSet(-20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindInvalidMatchingShare()
    {
        Share share = share(20);
        MockShareFinder finder = new MockShareFinder(share);

        SimpleDataSetInformationDTO dataSet = dataSet(21);
        try
        {
            finder.tryToFindShare(dataSet, SHARES);
            fail("AssertionError expected");
        } catch (AssertionError ex)
        {
            assertEquals("Found share 2 has speed 20 but data set ds-1 has speed hint 21. "
                    + "This violates speed checker MATCHING_CHECKER.", ex.getMessage());
        }
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindRespectingShareForPositiveHint()
    {
        Share share = share(21);
        MockShareFinder finder = new MockShareFinder(null, share);

        SimpleDataSetInformationDTO dataSet = dataSet(20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindRespectingShareForNegativeHint()
    {
        Share share = share(19);
        MockShareFinder finder = new MockShareFinder(null, share);

        SimpleDataSetInformationDTO dataSet = dataSet(-20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindInvalidRespectingShareForPositiveHint()
    {
        Share share = share(20);
        MockShareFinder finder = new MockShareFinder(null, share);

        final SimpleDataSetInformationDTO dataSet = dataSet(20);
        try
        {
            finder.tryToFindShare(dataSet, SHARES);
            fail("AssertionError expected");
        } catch (AssertionError ex)
        {
            assertEquals("Found share 2 has speed 20 but data set ds-1 has speed hint 20. "
                    + "This violates speed checker RESPECTING_SPEED_HINT_CHECKER.", ex.getMessage());
        }
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindAnyShareForPositiveHint()
    {
        Share share = share(10);
        MockShareFinder finder = new MockShareFinder(null, null, share);

        SimpleDataSetInformationDTO dataSet = dataSet(20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindAnyShareForNegativeHint()
    {
        Share share = share(10);
        MockShareFinder finder = new MockShareFinder(null, null, share);

        SimpleDataSetInformationDTO dataSet = dataSet(-20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(share, foundShare);
        finder.verify(dataSet, SHARES);
    }

    @Test
    public void testFindNoShare()
    {
        MockShareFinder finder = new MockShareFinder(null, null, null);

        SimpleDataSetInformationDTO dataSet = dataSet(-20);
        Share foundShare = finder.tryToFindShare(dataSet, SHARES);

        assertSame(null, foundShare);
        finder.verify(dataSet, SHARES);
    }

    private SimpleDataSetInformationDTO dataSet(int speedHint)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(DATA_SET_CODE);
        dataSet.setSpeedHint(speedHint);
        return dataSet;
    }

    private Share share(int speed)
    {
        return new Share(new File(Integer.toString(speed / 10)), speed, null);
    }
}
