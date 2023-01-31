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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share.ShufflePriority;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Kaloyan Enimanev
 */
public class StandardShareFinderTest extends AbstractIShareFinderTestCase
{

    private IShareFinder shareFinder;

    private List<Share> shares;

    @BeforeMethod
    public void setUp()
    {
        Properties properties = new Properties();
        properties.put(StandardShareFinder.MINIMUM_FREE_SPACE_KEY, "200");
        shareFinder = new StandardShareFinder(properties);
    }

    @Test
    public void testMoveFromIncomingToExtension()
    {
        shares =
                Arrays.asList(
                        incomingShare("1", megaBytes(300), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        incomingShare("2", megaBytes(3000), 50, ShufflePriority.SPEED),
                        extensionShare("3", megaBytes(50), 50),
                        extensionShare("4", megaBytes(2000), 90),
                        extensionShare("5", megaBytes(1000), 80),
                        extensionShare("6", megaBytes(500), 40),
                        extensionShare("7", megaBytes(20), 40),
                        extensionShare("8", megaBytes(800), 30),
                        extensionShare("9", megaBytes(600), 40));
        assertShareFoundForDataSet("3", dataSet("1", megaBytes(40), -50));
        assertShareFoundForDataSet("9", dataSet("1", megaBytes(100), -50));
        assertShareFoundForDataSet("5", dataSet("1", megaBytes(100), 50));
        assertShareFoundForDataSet("5", dataSet("1", megaBytes(900), -50));
        assertShareFoundForDataSet("4", dataSet("1", megaBytes(1100), -50));
    }

    @Test
    public void testAvoidShufflingToIncomingShareWithLessSpace()
    {
        shares =
                Arrays.asList(incomingShare("1", megaBytes(2000), 50, ShufflePriority.SPEED),
                        incomingShare("2", megaBytes(3000), 50, ShufflePriority.SPEED),
                        extensionShare("3", megaBytes(5000), 40));
        assertShareFoundForDataSet(null, dataSet("2", megaBytes(900), -50));
    }

    @Test
    public void testMoveToExtensionShareWithMoreSpace()
    {
        shares =
                Arrays.asList(incomingShare("1", megaBytes(2000), 50, ShufflePriority.SPEED),
                        extensionShare("2", megaBytes(3000), 40),
                        extensionShare("3", megaBytes(5000), 40),
                        extensionShare("4", megaBytes(4000), 40));
        assertShareFoundForDataSet("3", dataSet("2", megaBytes(900), 60));
    }

    @Test
    public void testMoveToExtensionShareWithBetterSpeedMatch()
    {
        shares =
                Arrays.asList(incomingShare("1", megaBytes(2000), 50, ShufflePriority.SPEED),
                        extensionShare("2", megaBytes(3000), 40),
                        extensionShare("3", megaBytes(3000), 45),
                        extensionShare("4", megaBytes(1000), 50));
        assertShareFoundForDataSet("4", dataSet("2", megaBytes(900), 60));
    }

    @Test
    public void testShuffleToIncomingShareWithMoreSpace()
    {
        shares =
                Arrays.asList(incomingShare("1", megaBytes(2000), 50, ShufflePriority.SPEED),
                        incomingShare("2", megaBytes(100), 50, ShufflePriority.SPEED));
        assertShareFoundForDataSet("1", dataSet("2", megaBytes(900), -50));
    }

    @Test
    public void testShuffleToIncomingShareWithMoreSpace2()
    {
        shares =
                Arrays.asList(
                        incomingShare("1", megaBytes(2000), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        incomingShare("2", megaBytes(100), 50, ShufflePriority.MOVE_TO_EXTENSION));
        assertShareFoundForDataSet("1", dataSet("2", megaBytes(900), -50));
    }

    @Test
    public void testUnarchivingCases()
    {
        shares =
                Arrays.asList(
                        incomingShare("1", megaBytes(300), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        incomingShare("2", megaBytes(3000), 50, ShufflePriority.SPEED),
                        extensionShare("3", megaBytes(50), 50),
                        extensionShare("4", megaBytes(2000), 90),
                        extensionShare("5", megaBytes(1000), 80),
                        extensionShare("6", megaBytes(500), 40),
                        extensionShare("7", megaBytes(20), 40),
                        extensionShare("8", megaBytes(800), 30),
                        extensionShare("9", megaBytes(600), 40));

        assertShareFoundForDataSet("3", archivedDataSet(megaBytes(40), -50));
        assertShareFoundForDataSet("9", archivedDataSet(megaBytes(100), -50));
        assertShareFoundForDataSet("5", archivedDataSet(megaBytes(100), 50));
        assertShareFoundForDataSet("5", archivedDataSet(megaBytes(900), -50));
        assertShareFoundForDataSet(null, archivedDataSet(megaBytes(2900), -50));
    }

    @Test
    public void testPickExtensionShareWithBestSpeedAndSpaceMatch()
    {
        shares =
                Arrays.asList(
                        incomingShare("1", megaBytes(100), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        incomingShare("2", megaBytes(400), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        extensionShare("3", megaBytes(200), 70),
                        extensionShare("4", megaBytes(1500), 80),
                        extensionShare("5", megaBytes(500), 70),
                        extensionShare("6", megaBytes(2000), 90));

        assertShareFoundForDataSet("5", dataSet("1", megaBytes(100), -50));
    }

    @Test
    public void testWithdrawShare()
    {
        Share share4 = extensionShare("4", megaBytes(500), 50);
        shares =
                Arrays.asList(
                        incomingShare("1", megaBytes(100), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        incomingShare("2", megaBytes(400), 50, ShufflePriority.MOVE_TO_EXTENSION),
                        extensionShare("3", megaBytes(200), 70),
                        share4);
        assertShareFoundForDataSet("4", dataSet("1", megaBytes(100), -50));

        share4.setWithdrawShare(true);
        assertShareFoundForDataSet("3", dataSet("1", megaBytes(100), -50));
    }

    private void assertShareFoundForDataSet(String expectedShareId, SimpleDataSetInformationDTO dataSet)
    {
        Share shareFound = shareFinder.tryToFindShare(dataSet, shares);
        String foundShareId = (shareFound != null) ? shareFound.getShareId() : null;
        assertEquals("Wrong share found", expectedShareId, foundShareId);
    }

    private SimpleDataSetInformationDTO archivedDataSet(long size, int speedHint)
    {
        return dataSet(null, size, speedHint);
    }

    private SimpleDataSetInformationDTO dataSet(String shareId, long size, int speedHint)
    {
        SimpleDataSetInformationDTO result = new SimpleDataSetInformationDTO();
        result.setDataSetShareId(shareId);
        result.setDataSetSize(size);
        result.setSpeedHint(speedHint);
        return result;
    }

}
