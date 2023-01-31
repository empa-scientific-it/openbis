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

import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreShufflingTask.CLASS_PROPERTY_NAME;
import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreShufflingTask.SHUFFLING_SECTION_NAME;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory.SHARE_PROPS_FILE;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory.WITHDRAW_SHARE_PROP;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
{ ShareFactory.class, SegmentedStoreShufflingTask.class })
public class SegmentedStoreShufflingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_STORE_CODE = "DATA-STORE-1";

    private static void prepareAsWithdrawShare(File share)
    {
        FileUtilities.writeToFile(new File(share, SHARE_PROPS_FILE), WITHDRAW_SHARE_PROP
                + " = true");
    }

    public static final class MockShuffling implements ISegmentedStoreShuffling
    {
        private final Properties properties;

        private boolean initialized;

        private List<Share> sourceShares;

        private List<Share> targetShares;

        private IEncapsulatedOpenBISService service;

        private IDataSetMover dataSetMover;

        private ISimpleLogger logger;

        public MockShuffling(Properties properties)
        {
            this.properties = properties;
        }

        @Override
        public void init(ISimpleLogger l)
        {
            initialized = true;
        }

        @Override
        public void shuffleDataSets(List<Share> sources, List<Share> targets,
                IEncapsulatedOpenBISService openBisService, IDataSetMover mover,
                ISimpleLogger simpleLogger)
        {
            sourceShares = sources;
            targetShares = targets;
            service = openBisService;
            dataSetMover = mover;
            logger = simpleLogger;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IFreeSpaceProvider spaceProvider;

    private IDataSetMover dataSetMover;

    private ISimpleLogger logger;

    private SegmentedStoreShufflingTask balancerTask;

    private File storeRoot;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        spaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class);
        LinkedHashSet<String> incomingShareIds = new LinkedHashSet<String>(Arrays.asList("1"));
        balancerTask =
                new SegmentedStoreShufflingTask(incomingShareIds, service, spaceProvider,
                        dataSetMover, logger);
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testExecute()
    {
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, DATA_STORE_CODE);
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, storeRoot.getPath());
        properties.setProperty(SHUFFLING_SECTION_NAME + "." + CLASS_PROPERTY_NAME,
                SegmentedStoreShufflingTaskTest.MockShuffling.class.getName());
        File share1 = new File(storeRoot, "1");
        share1.mkdirs();
        File ds1File = new File(share1, "ds1");
        FileUtilities.writeToFile(ds1File, "hello ds1");
        File share2 = new File(storeRoot, "2");
        share2.mkdirs();
        FileUtilities.writeToFile(new File(share2, "ds2"), "hello ds2");
        prepareAsWithdrawShare(share2);
        balancerTask.setUp("mock-balancer", properties);
        final Sequence sequence1 = context.sequence("seq1");
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
                    ds1.setDataStoreCode(DATA_STORE_CODE);
                    ds1.setDataSetCode("ds1");
                    ds1.setDataSetShareId("1");
                    ds1.setDataSetSize(10l);
                    ds1.setDataSetLocation("ds1");
                    SimpleDataSetInformationDTO ds2 = new SimpleDataSetInformationDTO();
                    ds2.setDataStoreCode(DATA_STORE_CODE);
                    ds2.setDataSetCode("ds2");
                    ds2.setDataSetShareId("2");
                    ds2.setDataSetSize(20l);
                    ds2.setDataSetLocation("ds2");
                    SimpleDataSetInformationDTO ds3 = new SimpleDataSetInformationDTO();
                    ds3.setDataStoreCode("other data store");
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                    inSequence(sequence1);

                    SimpleDataSetInformationDTO ds1b = new SimpleDataSetInformationDTO();
                    ds1b.setDataStoreCode(DATA_STORE_CODE);
                    ds1b.setDataSetCode("ds1");
                    ds1b.setDataSetShareId("2");
                    ds1b.setDataSetSize(10l);
                    ds1b.setDataSetLocation("ds1");
                    one(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds1b, ds2)));
                    inSequence(sequence1);

                    allowing(logger).log(
                            with(LogLevel.INFO),
                            with(Matchers.startsWith("Obtained the list of all "
                                    + "datasets in all shares")));
                }
            });
        logRecorder.resetLogContent();

        balancerTask.execute();

        MockShuffling balancer =
                (SegmentedStoreShufflingTaskTest.MockShuffling) balancerTask.shuffling;
        assertEquals("{class=" + balancer.getClass().getName() + "}",
                balancer.properties.toString());
        assertEquals("1", balancer.sourceShares.get(0).getShareId());
        assertEquals("2", balancer.sourceShares.get(1).getShareId());
        assertEquals(2, balancer.sourceShares.size());
        assertEquals("1", balancer.targetShares.get(0).getShareId());
        assertEquals("2", balancer.targetShares.get(1).getShareId());
        assertEquals(2, balancer.targetShares.size());
        assertSame(service, balancer.service);
        assertSame(dataSetMover, balancer.dataSetMover);
        assertSame(logger, balancer.logger);
        assertEquals(true, balancer.initialized);
        assertEquals("INFO  OPERATION.SegmentedStoreShufflingTask - "
                + "Starting segmented store shuffling.\n"
                + "INFO  OPERATION.SegmentedStoreShufflingTask - "
                + "Segmented store shuffling finished.\n"
                + "INFO  NOTIFY.SegmentedStoreShufflingTask - "
                + "The following shares were emptied by shuffling: [1]",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefaultBalancer()
    {
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, DATA_STORE_CODE);
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, storeRoot.getPath());
        balancerTask.setUp("mock-balancer", properties);
        context.checking(new Expectations()
            {
                {
                    one(logger).log(LogLevel.INFO, "Data Store Shares:");
                    allowing(logger).log(
                            with(LogLevel.INFO),
                            with(Matchers.startsWith("Obtained the list of all "
                                    + "datasets in all shares")));
                }
            });

        balancerTask.execute();

        context.assertIsSatisfied();
    }
}
