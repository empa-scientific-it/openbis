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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.postregistration.SimpleShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class SimpleShareFinderTest extends AbstractIShareFinderTestCase
{
    private static final class MockSpeedChecker implements ISpeedChecker
    {
        private final boolean[] checkingResults;

        private final List<Share> recordedShares = new ArrayList<Share>();

        private SimpleDataSetInformationDTO recordedDataSet;

        private int index;

        MockSpeedChecker(boolean... checkingResults)
        {
            this.checkingResults = checkingResults;
        }

        @Override
        public boolean check(SimpleDataSetInformationDTO dataSet, Share share)
        {
            recordedDataSet = dataSet;
            recordedShares.add(share);
            return checkingResults[index++];
        }

        void verify(SimpleDataSetInformationDTO expectedDataSet, Share... expectedShares)
        {
            assertEquals(index, checkingResults.length);
            assertSame(expectedDataSet, recordedDataSet);
            for (int i = 0; i < expectedShares.length; i++)
            {
                assertSame(expectedShares[i], recordedShares.get(i));
            }
        }
    }

    private SimpleShareFinder shareFinder;

    @BeforeMethod
    public void setUp()
    {
        shareFinder = new SimpleShareFinder(new Properties());
    }

    @Test
    public void testMaxFreeExtensionShare()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetSize(kiloBytes(42));
        dataSet.setDataSetShareId("1");
        Share s1 = incomingShare("1", kiloBytes(300), 0);
        Share s2 = incomingShare("2", kiloBytes(300), 0);
        Share s3 = extensionShare("3", kiloBytes(30), 0);
        Share s4 = extensionShare("4", kiloBytes(50), 0);

        MockSpeedChecker speedChecker = new MockSpeedChecker(true, true, true, true);
        Share share = shareFinder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4), speedChecker);

        assertSame(s4, share);
        speedChecker.verify(dataSet, s1, s2, s3, s4);
    }

    @Test
    public void testMaxFreeIncomingShare()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetSize(kiloBytes(42));
        dataSet.setDataSetShareId("1");
        Share s1 = incomingShare("1", kiloBytes(300), 0);
        Share s2 = incomingShare("2", kiloBytes(400), 0);
        Share s3 = extensionShare("3", kiloBytes(30), 0);
        Share s4 = extensionShare("4", kiloBytes(50), 0);

        MockSpeedChecker speedChecker = new MockSpeedChecker(true, true, true, false);
        Share share = shareFinder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4), speedChecker);

        assertSame(s2, share);
        speedChecker.verify(dataSet, s1, s2, s3, s4);
    }

    @Test
    public void testIncomingDataSetShareHasMoreSpaceThanOtherIncomingShare()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetSize(kiloBytes(42));
        dataSet.setDataSetShareId("1");
        Share s1 = incomingShare("1", kiloBytes(300), 0);
        Share s2 = incomingShare("2", kiloBytes(200), 0);
        Share s3 = extensionShare("3", kiloBytes(30), 0);

        Share share = shareFinder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3));

        assertSame(null, share);
    }

    @Test
    public void testExtensionDataSetShareHasMoreSpaceThanOtherExtensionShare()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetSize(kiloBytes(42));
        dataSet.setDataSetShareId("1");
        Share s1 = extensionShare("1", kiloBytes(300), 0);
        Share s2 = extensionShare("2", kiloBytes(200), 0);
        Share s3 = incomingShare("3", kiloBytes(50), 0);

        Share share = shareFinder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3));

        assertSame(null, share);
    }

    @Test
    public void testExtensionDataSetShareHasSlightlyMoreSpaceThanDataSetSize()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetSize(kiloBytes(100));
        dataSet.setDataSetShareId("1");
        Share s1 = incomingShare("1", kiloBytes(10), 0);
        Share s2 = extensionShare("2", kiloBytes(109), 0);

        Share share = shareFinder.tryToFindShare(dataSet, Arrays.asList(s1, s2));

        assertSame(null, share);
    }
}
