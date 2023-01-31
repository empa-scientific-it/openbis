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

import static org.apache.commons.io.FileUtils.ONE_MB;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.etlserver.postregistration.EagerShufflingTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { SimpleShuffling.class, EagerShufflingTask.class })
public class SimpleShufflingTest extends AbstractFileSystemTestCase
{
    private static final String DSS_CODE = "dss1";

    private static final String STORE_PATH = "01/02/03/";

    private static final class MockSpaceProvider implements IFreeSpaceProvider
    {
        private final Map<File, List<Long>> freeSpace = new HashMap<File, List<Long>>();

        void addFreeSpaceExpectationFor(Share share, long valueInKb)
        {
            List<Long> list = freeSpace.get(share.getShare());
            if (list == null)
            {
                list = new ArrayList<Long>();
                freeSpace.put(share.getShare(), list);
            }
            list.add(valueInKb);
        }

        @Override
        public long freeSpaceKb(HostAwareFile path) throws IOException
        {
            File file = path.getLocalFile();
            List<Long> list = freeSpace.get(file);
            assertNotNull("Unknown file " + file, list);
            assertFalse("Unexpected invocation for file " + file, list.isEmpty());
            return list.remove(0);
        }

        void assertIsSatiesfied()
        {
            Set<Entry<File, List<Long>>> entrySet = freeSpace.entrySet();
            for (Entry<File, List<Long>> entry : entrySet)
            {
                assertEquals("Unfullfilled free space assertions for " + entry.getKey().getPath(),
                        0, entry.getValue().size());
            }
        }
    }

    private MockSpaceProvider spaceProvider;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IDataSetMover dataSetMover;

    private ISimpleLogger logger;

    private SimpleShuffling balancer;

    private File store;

    private EagerShufflingTask eagerShufflingTask;

    private IShareIdManager shareIdManager;

    private IConfigProvider configProvider;

    private ISimpleLogger notifyer;

    @BeforeMethod
    public void beforeMethod()
    {
        spaceProvider = new MockSpaceProvider();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        configProvider = context.mock(IConfigProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class, "logger");
        notifyer = context.mock(ISimpleLogger.class, "notifyer");
        Properties properties = new Properties();
        properties.setProperty(SimpleShuffling.MINIMUM_FREE_SPACE_KEY, "2");
        properties.setProperty(EagerShufflingTask.SHARE_FINDER_KEY + ".class",
                SimpleShufflingShareFinder.class.getName());
        properties.setProperty(EagerShufflingTask.SHARE_FINDER_KEY + "."
                + SimpleShufflingShareFinder.MINIMUM_FREE_SPACE_KEY, "2");
        store = new File(workingDirectory, "store");
        store.mkdirs();
        File ds1 = new File(store, "1/" + STORE_PATH + "ds1");
        ds1.getParentFile().mkdirs();
        FileUtilities.writeToFile(ds1, "hello ds1");
        FileUtilities.writeToFile(new File(store, "1/" + STORE_PATH + "ds2"), "hello ds2");
        FileUtilities.writeToFile(new File(store, "1/" + STORE_PATH + "ds3"), "hello ds3");
        File share2 = new File(store, "2/" + STORE_PATH);
        share2.mkdirs();
        FileUtilities.writeToFile(new File(share2, "ds4"), "hello ds4");
        new File(store, "3").mkdirs();
        new File(store, "4").mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DSS_CODE));

                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));
                }
            });
        eagerShufflingTask =
                new EagerShufflingTask(properties, new HashSet<String>(Arrays.asList("1", "2")),
                        service, shareIdManager, spaceProvider, dataSetMover, configProvider, null,
                        logger, notifyer);
        balancer = new SimpleShuffling(properties, eagerShufflingTask);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void test()
    {
        final Share share1 = new Share(new File(store, "1"), 0, spaceProvider);
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", "1", 2000);
        share1.addDataSet(ds1);
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", "1", ONE_MB);
        share1.addDataSet(ds2);
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", "1", ONE_MB + 100);
        share1.addDataSet(ds3);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        final Share share2 = new Share(new File(store, "2"), 0, spaceProvider);
        share2.setWithdrawShare(true);
        final SimpleDataSetInformationDTO ds4 = dataSet("ds4", "2", 2 * ONE_MB);
        share2.addDataSet(ds4);
        spaceProvider.addFreeSpaceExpectationFor(share2, 2500l);
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        final Share share3 = new Share(new File(store, "3"), 0, spaceProvider);
        spaceProvider.addFreeSpaceExpectationFor(share3, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1 * 1024l);
        final Share share4 = new Share(new File(store, "4"), 0, spaceProvider);
        spaceProvider.addFreeSpaceExpectationFor(share4, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share4, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share4, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share4, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share4, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share4, 1024l);
        context.checking(new Expectations()
            {
                {
                    allowing(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2, ds3, ds4)));
                    allowing(service).isDataSetOnTrashCanOrDeleted(ds3.getDataSetCode());
                    one(logger).log(LogLevel.INFO,
                            "BEGIN Computing number of data sets to be moved for share 1");
                    one(logger).log(LogLevel.INFO,
                            "\tSpace needed to free: 1994752 bytes (1948.00 kB, 1.90 MB)");
                    one(logger).log(LogLevel.INFO, "\tInspecting 3 data sets.");
                    one(logger).log(LogLevel.INFO,
                            "END Computing number of data sets to move for share 1");
                    one(logger).log(LogLevel.INFO,
                            "\t2 data sets to move, available space : 102500");

                    one(shareIdManager).getShareId(ds3.getDataSetCode());
                    will(returnValue(ds3.getDataSetShareId()));

                    one(dataSetMover).moveDataSetToAnotherShare(
                            new File(share1.getShare(), STORE_PATH + "ds3"), share3.getShare(),
                            null, logger);
                    one(logger).log(LogLevel.INFO,
                            "Data set ds3 successfully moved from share 1 to 3.");

                    one(logger).log(LogLevel.INFO, "All 1 data sets should be moved for share 2");
                    one(shareIdManager).getShareId(ds2.getDataSetCode());
                    will(returnValue(ds2.getDataSetShareId()));

                    allowing(service).isDataSetOnTrashCanOrDeleted(ds2.getDataSetCode());
                    one(dataSetMover).moveDataSetToAnotherShare(
                            new File(share1.getShare(), STORE_PATH + "ds2"), share4.getShare(),
                            null, logger);
                    one(logger).log(LogLevel.INFO,
                            "Data set ds2 successfully moved from share 1 to 4.");

                    one(logger).log(LogLevel.WARN, "No share found for shuffling data set ds4.");
                    allowing(logger).log(
                            with(LogLevel.INFO),
                            with(Matchers.startsWith("Obtained the list of all "
                                    + "datasets in all shares")));
                }
            });

        balancer.shuffleDataSets(Arrays.asList(share1, share2),
                Arrays.asList(share1, share2, share3, share4), service, dataSetMover, logger);

        spaceProvider.assertIsSatiesfied();
    }

    private SimpleDataSetInformationDTO dataSet(String code, String shareId, long size)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(code);
        dataSet.setDataSetShareId(shareId);
        dataSet.setDataSetLocation(STORE_PATH + code);
        dataSet.setDataSetSize(size);
        dataSet.setDataStoreCode(DSS_CODE);
        dataSet.setStatus(DataSetArchivingStatus.AVAILABLE);
        dataSet.setSpeedHint(Constants.DEFAULT_SPEED_HINT);
        return dataSet;
    }
}
