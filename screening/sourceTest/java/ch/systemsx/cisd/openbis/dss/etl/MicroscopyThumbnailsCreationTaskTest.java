/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.ToStringMatcher;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AggregationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author Franz-Josef Elmer
 */
public class MicroscopyThumbnailsCreationTaskTest extends AbstractFileSystemTestCase
{
    private static final String USER_ID = "test";

    private static final String PASSWORD = "pwd";

    private static final String SESSION_TOKEN = "test-1234";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IApplicationServerApi service;

    private IDataSetDirectoryProvider directoryProvider;

    private File store;

    private MockMicroscopyThumbnailsCreationTask task;

    private DataSetFetchOptions fetchOptions;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        service = context.mock(IApplicationServerApi.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        task = new MockMicroscopyThumbnailsCreationTask(service, directoryProvider);
        store = new File(workingDirectory, "store");
        FileUtilities.deleteRecursively(store);
        store.mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(service).login(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    allowing(directoryProvider).getStoreRoot();
                    will(returnValue(store));

                }
            });
    }

    @Test
    public void testOneDataSet()
    {
        // Given
        Properties properties = new Properties();
        task.setUp("test", properties);
        RecordingMatcher<DataSetSearchCriteria> searchCriteria = prepareSearchDataSets(1000,
                task.withSuccess("2021-06-28 14:02:34", false));

        // When
        task.execute();

        // Then
        assertEquals(
                "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Search for data sets of type "
                        + "MICROSCOPY_IMG_CONTAINER which are younger than 1970-01-01 01:00:00\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 1 data sets found.\n"
                        + "INFO  OPERATION.ParallelizedExecutor - Found 1 items to process.\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for data set "
                        + "2021-06-28 14:02:34-0\n"
                        + "INFO  OPERATION.ParallelizedExecutor - thumbnail creation finished successfully [? msec.] .\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Update time stamp file with data set "
                        + "2021-06-28 14:02:34-0\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 1 thumbnail data sets have been created.",
                getLogContent());
        assertTimestampFile("2021-06-28 14:02:34 [2021-06-28 14:02:34-0]\n");
        assertEquals("[DATASET\n"
                + "    with operator 'AND'\n"
                + "    with data_set_type:\n"
                + "        with attribute 'code' equal to 'MICROSCOPY_IMG_CONTAINER'\n"
                + "    with attribute 'registration_date' later than or equal to 'Thu Jan 01 01:00:00 CET 1970'\n]",
                searchCriteria.getRecordedObjects().toString());
    }

    @Test
    public void test5DataSets2FailingInOneThread()
    {
        // Given
        Properties properties = new Properties();
        task.setUp("test", properties);
        RecordingMatcher<DataSetSearchCriteria> searchCriteria = prepareSearchDataSets(1000,
                task.withSuccess("2021-06-28 14:02:34", false), task.withFailure("2021-06-28 15:03:45", false),
                task.withSuccess("2021-06-28 15:03:45", false), task.withFailure("2021-06-28 15:13:38", false),
                task.withSuccess("2021-06-29 05:23:33", false));

        // When
        task.execute();

        // Then
        assertEquals(
                "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Search for data sets of type "
                        + "MICROSCOPY_IMG_CONTAINER which are younger than 1970-01-01 01:00:00\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 5 data sets found.\n"
                        + "INFO  OPERATION.ParallelizedExecutor - Found 5 items to process.\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 14:02:34-0\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 15:03:45-2\n"
                        + "ERROR OPERATION.MicroscopyThumbnailsCreationTask - Generating thumbnails for "
                        + "data set 2021-06-28 15:03:45-2 failed:\n"
                        + "java.lang.RuntimeException: Failed 1\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 15:03:45-4\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 15:13:38-6\n"
                        + "ERROR OPERATION.MicroscopyThumbnailsCreationTask - Generating thumbnails for "
                        + "data set 2021-06-28 15:13:38-6 failed:\n"
                        + "java.lang.RuntimeException: Failed 2\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-29 05:23:33-8\n"
                        + "INFO  OPERATION.ParallelizedExecutor - thumbnail creation finished with ? msec.] .\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Oldest failed data set: "
                        + "2021-06-28 15:03:45-2, youngest not failed data set: 2021-06-28 14:02:34-0\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 3 thumbnail data sets have been created.",
                getLogContent());
        assertTimestampFile("2021-06-28 14:02:34 [2021-06-28 14:02:34-0]\n");
        assertEquals("[DATASET\n"
                + "    with operator 'AND'\n"
                + "    with data_set_type:\n"
                + "        with attribute 'code' equal to 'MICROSCOPY_IMG_CONTAINER'\n"
                + "    with attribute 'registration_date' later than or equal to 'Thu Jan 01 01:00:00 CET 1970'\n]",
                searchCriteria.getRecordedObjects().toString());
    }

    @Test
    public void test5DataSets2FailingInTwoThreads()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty("maximum-number-of-workers", "2");
        task.setUp("test", properties);
        RecordingMatcher<DataSetSearchCriteria> searchCriteria = prepareSearchDataSets(1000,
                task.withSuccess("2021-06-28 14:02:34", false), task.withFailure("2021-06-28 15:03:45", false),
                task.withSuccess("2021-06-28 15:03:45", false), task.withFailure("2021-06-28 15:13:38", false),
                task.withSuccess("2021-06-29 05:23:33", false));

        // When
        task.execute();

        // Then
        String logContent = getLogContent();
        System.out.println("test5DataSets2FailingInTwoThreads log content:\n" + logContent);
        AssertionUtil.assertStarts("INFO  OPERATION.MicroscopyThumbnailsCreationTask - Search for data sets of type "
                + "MICROSCOPY_IMG_CONTAINER which are younger than 1970-01-01 01:00:00\n"
                + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 5 data sets found.\n"
                + "INFO  OPERATION.ParallelizedExecutor - Found 5 items to process.", logContent);
        AssertionUtil.assertEnds("INFO  OPERATION.MicroscopyThumbnailsCreationTask - Oldest failed data set: "
                + "2021-06-28 15:03:45-2, youngest not failed data set: 2021-06-28 14:02:34-0\n"
                + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 3 thumbnail data sets have been created.",
                logContent);
        AssertionUtil.assertContains("Started up 2 worker threads.", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 14:02:34-0", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 15:03:45-2", logContent);
        AssertionUtil.assertContains("Generating thumbnails for data set 2021-06-28 15:03:45-2 failed", logContent);
        AssertionUtil.assertContains("ERROR: \"java.lang.RuntimeException: Failed 1\"", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 15:03:45-4", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 15:13:38-6", logContent);
        AssertionUtil.assertContains("Generating thumbnails for data set 2021-06-28 15:13:38-6 failed", logContent);
        AssertionUtil.assertContains("ERROR: \"java.lang.RuntimeException: Failed 2\"", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-29 05:23:33-8", logContent);
        assertTimestampFile("2021-06-28 14:02:34 [2021-06-28 14:02:34-0]\n");
        assertEquals("[DATASET\n"
                + "    with operator 'AND'\n"
                + "    with data_set_type:\n"
                + "        with attribute 'code' equal to 'MICROSCOPY_IMG_CONTAINER'\n"
                + "    with attribute 'registration_date' later than or equal to 'Thu Jan 01 01:00:00 CET 1970'\n]",
                searchCriteria.getRecordedObjects().toString());
    }

    @Test
    public void test5DataSets2FailingInOneThreadSecondStep()
    {
        // Given
        Properties properties = new Properties();
        task.setUp("test", properties);
        writeTimestampFile("2021-06-28 14:02:34 [2021-06-28 14:02:34-0]\n");
        RecordingMatcher<DataSetSearchCriteria> searchCriteria = prepareSearchDataSets(1000,
                task.withFailure("2021-06-28 15:03:45", false), task.withSuccess("2021-06-28 15:03:45", true),
                task.withFailure("2021-06-28 15:13:38", false), task.withSuccess("2021-06-29 05:23:33", true));

        // When
        task.execute();

        // Then
        assertEquals(
                "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Search for data sets of type "
                        + "MICROSCOPY_IMG_CONTAINER which are younger than 2021-06-28 14:02:34 and code after 2021-06-28 14:02:34-0\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 4 data sets found.\n"
                        + "INFO  OPERATION.ParallelizedExecutor - Found 4 items to process.\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 15:03:45-0\n"
                        + "ERROR OPERATION.MicroscopyThumbnailsCreationTask - Generating thumbnails for "
                        + "data set 2021-06-28 15:03:45-0 failed:\n"
                        + "java.lang.RuntimeException: Failed 1\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Generate thumbnails for "
                        + "data set 2021-06-28 15:13:38-5\n"
                        + "ERROR OPERATION.MicroscopyThumbnailsCreationTask - Generating thumbnails for "
                        + "data set 2021-06-28 15:13:38-5 failed:\n"
                        + "java.lang.RuntimeException: Failed 2\n"
                        + "INFO  OPERATION.ParallelizedExecutor - thumbnail creation finished with ? msec.] .\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Ignoring failed data sets "
                        + "[DataSet 2021-06-28 15:03:45-0, DataSet 2021-06-28 15:13:38-5]\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Update time stamp file with data set "
                        + "2021-06-29 05:23:33-7\n"
                        + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 0 thumbnail data sets have been created.",
                getLogContent());
        assertTimestampFile("2021-06-29 05:23:33 [2021-06-29 05:23:33-7]\n");
        assertEquals("[DATASET\n"
                + "    with operator 'AND'\n"
                + "    with data_set_type:\n"
                + "        with attribute 'code' equal to 'MICROSCOPY_IMG_CONTAINER'\n"
                + "    with attribute 'registration_date' later than or equal to 'Mon Jun 28 14:02:34 CEST 2021'\n"
                + "    with attribute 'code' greater than '2021-06-28 14:02:34-0'\n]",
                searchCriteria.getRecordedObjects().toString());
    }

    @Test
    public void test5DataSets2FailingInTwoThreadSecondStep()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty("maximum-number-of-workers", "2");
        task.setUp("test", properties);
        writeTimestampFile("2021-06-28 14:02:34 [2021-06-28 14:02:34-0]\n");
        RecordingMatcher<DataSetSearchCriteria> searchCriteria = prepareSearchDataSets(1000,
                task.withFailure("2021-06-28 15:03:45", false), task.withSuccess("2021-06-28 15:03:45", true),
                task.withFailure("2021-06-28 15:13:38", false), task.withSuccess("2021-06-29 05:23:33", true));

        // When
        task.execute();

        // Then
        String logContent = getLogContent();
        System.out.println("test5DataSets2FailingInTwoThreadSecondStep log content:\n" + logContent);
        AssertionUtil.assertStarts("INFO  OPERATION.MicroscopyThumbnailsCreationTask - Search for data sets of type "
                + "MICROSCOPY_IMG_CONTAINER which are younger than 2021-06-28 14:02:34 and code after 2021-06-28 14:02:34-0\n"
                + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 4 data sets found.\n"
                + "INFO  OPERATION.ParallelizedExecutor - Found 4 items to process.\n",
                logContent);
        AssertionUtil.assertContains("Started up 2 worker threads.", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 15:03:45-0", logContent);
        AssertionUtil.assertContains("Generating thumbnails for data set 2021-06-28 15:03:45-0 failed", logContent);
        AssertionUtil.assertContains("ERROR: \"java.lang.RuntimeException: Failed 1\"", logContent);
        AssertionUtil.assertContains("Generate thumbnails for data set 2021-06-28 15:13:38-5", logContent);
        AssertionUtil.assertContains("Generating thumbnails for data set 2021-06-28 15:13:38-5 failed", logContent);
        AssertionUtil.assertContains("ERROR: \"java.lang.RuntimeException: Failed 2\"", logContent);
        AssertionUtil.assertEnds("INFO  OPERATION.MicroscopyThumbnailsCreationTask - Ignoring failed data sets "
                + "[DataSet 2021-06-28 15:03:45-0, DataSet 2021-06-28 15:13:38-5]\n"
                + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - Update time stamp file with data set "
                + "2021-06-29 05:23:33-7\n"
                + "INFO  OPERATION.MicroscopyThumbnailsCreationTask - 0 thumbnail data sets have been created.",
                logContent);
        assertTimestampFile("2021-06-29 05:23:33 [2021-06-29 05:23:33-7]\n");
        assertEquals("[DATASET\n"
                + "    with operator 'AND'\n"
                + "    with data_set_type:\n"
                + "        with attribute 'code' equal to 'MICROSCOPY_IMG_CONTAINER'\n"
                + "    with attribute 'registration_date' later than or equal to 'Mon Jun 28 14:02:34 CEST 2021'\n"
                + "    with attribute 'code' greater than '2021-06-28 14:02:34-0'\n]",
                searchCriteria.getRecordedObjects().toString());
    }

    private void writeTimestampFile(String content)
    {
        FileUtilities.writeToFile(createTimestampFile(), content);
    }

    private void assertTimestampFile(String expectedTimestampAndCode)
    {
        assertEquals(expectedTimestampAndCode, FileUtilities.loadToString(createTimestampFile()));
    }

    private File createTimestampFile()
    {
        return new File(store, MockMicroscopyThumbnailsCreationTask.class.getSimpleName() + "-state.txt");
    }

    private String getLogContent()
    {
        return logRecorder.getLogContent().replaceAll("\\d.* msec", "? msec").replaceAll("\tat .*\n", "");
    }

    private RecordingMatcher<DataSetSearchCriteria> prepareSearchDataSets(int maxCount, DataSet... dataSets)
    {
        RecordingMatcher<DataSetSearchCriteria> criteria = new RecordingMatcher<DataSetSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
                    fetchOptions.withComponents().withType();
                    fetchOptions.withComponents().withExperiment();
                    fetchOptions.withComponents().withSample();
                    fetchOptions.sortBy().registrationDate();
                    fetchOptions.sortBy().code();
                    if (maxCount > 0)
                    {
                        fetchOptions.from(0);
                        fetchOptions.count(maxCount);
                    }
                    one(service).searchDataSets(with(SESSION_TOKEN), with(criteria),
                            with(new ToStringMatcher<DataSetFetchOptions>(fetchOptions)));
                    will(returnValue(new SearchResult<>(Arrays.asList(dataSets), dataSets.length)));
                }
            });
        return criteria;
    }

    private static final class MockMicroscopyThumbnailsCreationTask extends MicroscopyThumbnailsCreationTask
    {

        private final IApplicationServerApi service;

        private final IDataSetDirectoryProvider directoryProvider;

        private final DataSetFetchOptions fetchOptions;

        private final DummyAggregationService dummyAggregationService = new DummyAggregationService();

        private Map<String, TableModel> results = new HashMap<>();

        private List<String> sessionTokens = new ArrayList<>();

        private List<DataSet> dataSets = new ArrayList<>();

        private int counter;

        public MockMicroscopyThumbnailsCreationTask(IApplicationServerApi service,
                IDataSetDirectoryProvider directoryProvider)
        {
            this.service = service;
            this.directoryProvider = directoryProvider;
            fetchOptions = new DataSetFetchOptions();
            fetchOptions.withType();
            fetchOptions.withComponents();
        }

        DataSet withSuccess(String timestamp, boolean withThumbnail)
        {
            DataSet dataSet = createDataSet(timestamp, withThumbnail);
            results.put(dataSet.getCode(), new TableModel(Arrays.asList(new TableModelColumnHeader()),
                    Arrays.asList(new TableModelRow(Arrays.asList(
                            new IntegerTableCell(1))))));
            return dataSet;
        }

        DataSet withFailure(String timestamp, boolean withThumbnail)
        {
            DataSet dataSet = createDataSet(timestamp, withThumbnail);
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            results.put(dataSet.getCode(), dummyAggregationService.createAggregationReport(parameters, null));
            return dataSet;
        }

        private DataSet createDataSet(String timestamp, boolean withThumbnail)
        {
            DataSet dataSet = createBasicDataSet(timestamp, "MICROSCOPY_IMG_CONTAINER");
            List<DataSet> components = new ArrayList<>();
            components.add(createBasicDataSet(timestamp, "MICROSCOPY_IMG"));
            if (withThumbnail)
            {
                components.add(createBasicDataSet(timestamp, "MICROSCOPY_IMG_THUMBNAIL"));
            }
            dataSet.setComponents(components);
            try
            {
                SimpleDateFormat format = new SimpleDateFormat(AbstractMaintenanceTaskWithStateFile.TIME_STAMP_FORMAT);
                dataSet.setRegistrationDate(format.parse(timestamp));
            } catch (ParseException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
            return dataSet;
        }

        private DataSet createBasicDataSet(String timestamp, String dataSetTypeCode)
        {
            DataSet dataSet = new DataSet();
            dataSet.setCode(timestamp + "-" + counter++);
            DataSetType type = new DataSetType();
            type.setCode(dataSetTypeCode);
            dataSet.setType(type);
            dataSet.setFetchOptions(fetchOptions);
            return dataSet;
        }

        @Override
        double getMachineLoad()
        {
            return 2;
        }

        @Override
        TableModel createThumbnailDataSetViaIngestionService(String sessionToken, DataSet containerDataSet)
        {
            sessionTokens.add(sessionToken);
            dataSets.add(containerDataSet);
            return results.get(containerDataSet.getCode());
        }

        @Override
        protected IDataSetDirectoryProvider getDirectoryProvider()
        {
            return directoryProvider;
        }

        @Override
        protected IApplicationServerApi getService()
        {
            return service;
        }

        @Override
        protected ICredentials getEtlServerCredentials()
        {
            return new ICredentials()
                {
                    @Override
                    public String getUserId()
                    {
                        return USER_ID;
                    }

                    @Override
                    public String getPassword()
                    {
                        return PASSWORD;
                    }
                };
        }
    }

    private static final class DummyAggregationService extends AggregationService
    {
        private static final long serialVersionUID = 1L;

        private int counter;

        public DummyAggregationService()
        {
            super(new Properties(), new File("."));
        }

        @Override
        public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
        {
            return errorTableModel(parameters, new RuntimeException("Failed " + ++counter));
        }

    }
}
