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

import static ch.systemsx.cisd.etlserver.plugins.SimpleShufflingShareFinder.MINIMUM_FREE_SPACE_KEY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISpeedChecker;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = SimpleShufflingShareFinder.class)
public class SimpleShufflingShareFinderTest extends AssertJUnit
{
    private static final String DATA_SET_CODE = "ds-1";

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
            assertEquals(expectedShares.length, recordedShares.size());
        }
    }

    private Mockery context;

    private IFreeSpaceProvider freeSpaceProvider;

    private SimpleDataSetInformationDTO dataSet;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(DATA_SET_CODE);
        dataSet.setDataSetSize(FileUtils.ONE_MB);
    }

    @Test
    public void testNoSpeedCheckingShare()
    {
        SimpleShufflingShareFinder finder = new SimpleShufflingShareFinder(new Properties());
        MockSpeedChecker speedChecker = new MockSpeedChecker(false);
        Share share = share("1", 0);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(share), speedChecker);

        assertEquals(null, foundShare);
        speedChecker.verify(dataSet, share);
        context.assertIsSatisfied();
    }

    @Test
    public void testFoundNothing()
    {
        SimpleShufflingShareFinder finder = new SimpleShufflingShareFinder(new Properties());
        MockSpeedChecker speedChecker = new MockSpeedChecker(true);
        Share share = share("1", 10 * FileUtils.ONE_MB);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(share), speedChecker);

        assertSame(null, foundShare);
        speedChecker.verify(dataSet, share);
        context.assertIsSatisfied();
    }

    @Test
    public void testFoundMaximum()
    {
        Properties properties = new Properties();
        properties.setProperty(MINIMUM_FREE_SPACE_KEY, Long.toString(2));
        SimpleShufflingShareFinder finder = new SimpleShufflingShareFinder(properties);
        MockSpeedChecker speedChecker = new MockSpeedChecker(true, false, true);
        Share s1 = share("1", 10 * FileUtils.ONE_MB);
        Share s2 = share("2", 0);
        Share s3 = share("3", 30 * FileUtils.ONE_MB);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3), speedChecker);

        assertSame(s3, foundShare);
        speedChecker.verify(dataSet, s1, s2, s3);
        context.assertIsSatisfied();
    }

    @Test
    public void testDoNotFindHomeShare()
    {
        Properties properties = new Properties();
        properties.setProperty(MINIMUM_FREE_SPACE_KEY, Long.toString(2));
        SimpleShufflingShareFinder finder = new SimpleShufflingShareFinder(properties);
        MockSpeedChecker speedChecker = new MockSpeedChecker(true, false, true);
        Share s1 = share("1", 10 * FileUtils.ONE_MB);
        Share s2 = share("2", 0);
        Share s3 = share("3", 30 * FileUtils.ONE_MB);

        dataSet.setDataSetShareId(s3.getShareId());

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3), speedChecker);

        assertNull("Home share must never be found.", foundShare);
        context.assertIsSatisfied();
    }

    private Share share(String id, final long freeSpace)
    {
        final File file = new File(id);
        if (freeSpace > 0)
        {
            context.checking(new Expectations()
                {
                    {
                        try
                        {
                            one(freeSpaceProvider).freeSpaceKb(new HostAwareFile(file));
                            will(returnValue(freeSpace / FileUtils.ONE_KB));
                        } catch (IOException ex)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                        }
                    }
                });
        }
        return new Share(file, 0, freeSpaceProvider);
    }
}
