/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTaskTest extends
        AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "1";

    private static final String DATA_STORE_CODE = "DSS";

    private static final String LAST_SEEN_DATA_SET_FILE = "last-seen-data-set";

    private static final String INFO_LOG_PREFIX =
            "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - ";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService openBISService;

    private DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask task;

    private File lastSeenDataSetFile;

    private IConfigProvider configProvider;

    private IShareIdManager shareIdManager;

    private IDataStoreServiceInternal dssService;

    private IDataSetDeleter deleter;

    private File store;

    private File share;

    @BeforeMethod
    public void setUpMocks() throws Exception
    {
        context = new Mockery();
        openBISService = context.mock(IEncapsulatedOpenBISService.class);
        configProvider = context.mock(IConfigProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        dssService = context.mock(IDataStoreServiceInternal.class);
        deleter = context.mock(IDataSetDeleter.class);

        store = new File(workingDirectory, "store");
        share = new File(store, SHARE_ID);
        share.mkdirs();

        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean(ServiceProvider.OPEN_BIS_SERVICE_BEAN);
                    will(returnValue(openBISService));

                    allowing(beanFactory).getBean(ServiceProvider.CONFIG_PROVIDER_BEAN);
                    will(returnValue(configProvider));

                    allowing(beanFactory).getBean(ServiceProvider.SHARE_ID_MANAGER_BEAN);
                    will(returnValue(shareIdManager));

                    allowing(beanFactory).getBean(ServiceProvider.DATA_STORE_SERVICE_BEAN);
                    will(returnValue(dssService));

                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));

                    allowing(dssService).getDataSetDeleter();
                    will(returnValue(deleter));
                }
            });
        task = new DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask();
        task.timeProvider = new MockTimeProvider(22, 1000);
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        Properties properties = new Properties();
        properties
                .setProperty(
                        DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask.TIMING_PARAMETERS_KEY
                                + "." + TimingParameters.MAX_RETRY_PROPERTY_NAME, "2");
        properties
                .setProperty(
                        DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask.TIMING_PARAMETERS_KEY
                                + "." + TimingParameters.FAILURE_INTERVAL_NAME, "1");
        lastSeenDataSetFile = new File(workingDirectory, LAST_SEEN_DATA_SET_FILE);
        properties
                .setProperty(
                        DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask.LAST_SEEN_DATA_SET_FILE_PROPERTY,
                        lastSeenDataSetFile.getPath());
        task.setUp("", properties);
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteKnownDataSet()
    {
        final RecordingMatcher<List<? extends IDatasetLocation>> recordedLocations =
                new RecordingMatcher<List<? extends IDatasetLocation>>();
        context.checking(new Expectations()
            {
                {
                    one(openBISService).listDeletedDataSets(null, new Date(22));
                    DeletedDataSet ds1 = new DeletedDataSet(42, "DS1");
                    DeletedDataSetLocation location1 = new DeletedDataSetLocation();
                    location1.setDatastoreCode(DATA_STORE_CODE);
                    location1.setLocation("a/ds1");
                    ds1.setLocationObjectOrNull(location1);
                    will(returnValue(Arrays.asList(ds1)));

                    exactly(2).of(shareIdManager).isKnown("DS1");
                    will(returnValue(true));

                    one(deleter).scheduleDeletionOfDataSets(with(recordedLocations),
                            with(2), with(1000L));
                }
            });

        task.execute();

        AssertionUtil.assertContainsLines(INFO_LOG_PREFIX + "Got 1 deletions to process\n" + INFO_LOG_PREFIX
                + "Is going to delete a known data set: DS1\n" + INFO_LOG_PREFIX
                + "Data set deletion post-processing task took 1 seconds.",
                logRecorder.getLogContent());
        assertEquals("[Dataset[DS1], location[a/ds1]]", recordedLocations.recordedObject()
                .toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteUnknownDataSet()
    {
        File file = new File(share, "a/ds1");
        file.mkdirs();
        context.checking(new Expectations()
            {
                {
                    one(openBISService).listDeletedDataSets(null, new Date(22));
                    DeletedDataSet ds1 = new DeletedDataSet(42, "DS1");
                    DeletedDataSetLocation location1 = new DeletedDataSetLocation();
                    location1.setDatastoreCode(DATA_STORE_CODE);
                    location1.setLocation("a/ds1");
                    location1.setShareId(SHARE_ID);
                    ds1.setLocationObjectOrNull(location1);
                    will(returnValue(Arrays.asList(ds1)));

                    exactly(2).of(shareIdManager).isKnown("DS1");
                    will(returnValue(false));
                }
            });

        task.execute();

        AssertionUtil.assertContainsLines(INFO_LOG_PREFIX + "Got 1 deletions to process\n" + INFO_LOG_PREFIX
                + "Is going to delete an unknown data set: DS1\n" + INFO_LOG_PREFIX
                + "Start deleting data set DS1 at " + file + "\n" + INFO_LOG_PREFIX
                + "Data set DS1 at " + file + " has been successfully deleted.\n" + INFO_LOG_PREFIX
                + "Data set deletion post-processing task took 1 seconds.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteNonExistingDataSet()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBISService).listDeletedDataSets(null, new Date(22));
                    DeletedDataSet ds1 = new DeletedDataSet(42, "DS1");
                    DeletedDataSetLocation location1 = new DeletedDataSetLocation();
                    location1.setDatastoreCode(DATA_STORE_CODE);
                    location1.setLocation("a/ds1");
                    location1.setShareId(SHARE_ID);
                    ds1.setLocationObjectOrNull(location1);
                    will(returnValue(Arrays.asList(ds1)));

                    exactly(2).of(shareIdManager).isKnown("DS1");
                    will(returnValue(false));
                }
            });

        task.execute();

        AssertionUtil.assertContainsLines(INFO_LOG_PREFIX + "Got 1 deletions to process\n"
                + "WARN  OPERATION.DataSetExistenceChecker - Data set 'DS1' no longer exists.\n"
                + INFO_LOG_PREFIX + "Data set deletion post-processing task took 1 seconds.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteNonExistingDataSetInNonExistingShare()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBISService).listDeletedDataSets(null, new Date(22));
                    DeletedDataSet ds1 = new DeletedDataSet(42, "DS1");
                    DeletedDataSetLocation location1 = new DeletedDataSetLocation();
                    location1.setDatastoreCode(DATA_STORE_CODE);
                    location1.setLocation("a/ds1");
                    location1.setShareId("2");
                    ds1.setLocationObjectOrNull(location1);
                    will(returnValue(Arrays.asList(ds1)));

                    exactly(2).of(shareIdManager).isKnown("DS1");
                    will(returnValue(false));
                }
            });

        task.execute();

        AssertionUtil.assertContainsLines(INFO_LOG_PREFIX + "Got 1 deletions to process\n"
                + "ERROR NOTIFY.DataSetExistenceChecker - "
                + "Data set 'DS1' couldn't retrieved because share '" + new File(store, "2")
                + "' doesn't exists after 2 retries (waiting 1000 msec between retries).\n"
                + INFO_LOG_PREFIX + "Data set deletion post-processing task took 1 seconds.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

}
