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

package ch.systemsx.cisd.etlserver.path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyCheckTaskTest extends AbstractFileSystemTestCase
{
    private final class MockDataSetAndPathInfoDBConsistencyCheckTask extends DataSetAndPathInfoDBConsistencyCheckTask
    {
        private MockDataSetAndPathInfoDBConsistencyCheckTask(IHierarchicalContentProvider fileProvider,
                IHierarchicalContentProvider pathInfoProvider, IDataSetDirectoryProvider directoryProvider, IApplicationServerApi service,
                ITimeProvider timeProvider)
        {
            super(fileProvider, pathInfoProvider, directoryProvider, service, timeProvider);
        }

        @Override
        String login()
        {
            return SESSION_TOKEN;
        }
    }

    private static final String SESSION_TOKEN = "session-123";
    
    private BufferedAppender logRecorder;

    private Mockery context;

    private IApplicationServerApi service;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private DataSetAndPathInfoDBConsistencyCheckTask task;

    private IDataSetDirectoryProvider directoryProvidor;

    private File storeRoot;

    private long startTimestamp;

    @BeforeMethod
    public void setUpMocks() throws ParseException
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IApplicationServerApi.class);
        fileProvider = context.mock(IHierarchicalContentProvider.class, "fileProvider");
        pathInfoProvider = context.mock(IHierarchicalContentProvider.class, "pathInfoProvider");
        directoryProvidor = context.mock(IDataSetDirectoryProvider.class);
        startTimestamp = createStartTimestamp("1977-06-15 23:53:10");
        task = new MockDataSetAndPathInfoDBConsistencyCheckTask(fileProvider, pathInfoProvider, 
                directoryProvidor, service, new MockTimeProvider(startTimestamp, 10000));
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(service).logout(SESSION_TOKEN);
                    
                    allowing(directoryProvidor).getStoreRoot();
                    will(returnValue(storeRoot));
                }
            });
    }

    private long createStartTimestamp(String timestampString) throws ParseException
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampString).getTime();
    }

    @AfterMethod
    public void checkContext()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testConsistentCase()
    {
        setUpForUninterruptedProcessing();
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder = prepareListDataSets(ds1);
        MockContent fileContent =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9", "a/c:4:1:abcd::");
        MockContent pathInfoContent =
                prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9", 
                        "a/c:4:1:abcd:SHA:81fe8bfe87576c3ecb22426f8e57847382917acf");

        task.execute();

        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1977-06-15 23:53:08"));

        assertEquals(true, fileContent.isClosed());
        assertEquals(true, pathInfoContent.isClosed());
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:53:08 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testErrorCase()
    {
        setUpForUninterruptedProcessing();
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder = prepareListDataSets(ds1);
        MockContent fileContent =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9");
        prepareContentProvider(pathInfoProvider, "ds1", new RuntimeException("Oohps!"));

        task.execute();

        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1977-06-15 23:53:08"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR OPERATION.DataSetAndPathInfoDBConsistencyChecker - "
                + "Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1"));
        assertThat(logRecorder.getLogLines(), hasItem("java.lang.RuntimeException: Oohps!"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "File system and path info DB consistency check report "
                + "for all data sets since 1977-06-15 23:53:08"));
        assertThat(logRecorder.getLogLines(), hasItem("Error when checking datasets:"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR: \"Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1 because of the following exception: Oohps!\""));

        assertEquals(true, fileContent.isClosed());
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:53:08 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testInconsistentCases()
    {
        setUpForUninterruptedProcessing();
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        PhysicalDataSet ds2 = createDataSetBuilder().code("ds2").getDataSet();
        PhysicalDataSet ds3 = createDataSetBuilder().code("ds3").getDataSet();
        PhysicalDataSet ds4 = createDataSetBuilder().code("ds4").getDataSet();
        PhysicalDataSet ds5 = createDataSetBuilder().code("ds5").getDataSet();
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder = prepareListDataSets(ds1, ds2, ds3, ds4, ds5);
        MockContent fileContent1 =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9",
                        "a/c:35:2", "b:33:3", "c/:0:0");
        MockContent pathInfoContent1 =
                prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:7",
                        "a/c:42:2", "c:2:4");
        MockContent fileContent2 = prepareContentProvider(fileProvider, "ds2");
        MockContent pathInfoContent2 = prepareContentProvider(pathInfoProvider, "ds2", ":0:0");
        MockContent fileContent3 =
                prepareContentProvider(fileProvider, "ds3", ":0:0", "a/:0:0", "a/b:34:9:abcd::",
                        "a/c:42:2", "c:2:4");
        MockContent pathInfoContent3 =
                prepareContentProvider(pathInfoProvider, "ds3", ":0:0", "a/:0:0", "a/b:34:9::SHA:78bc65",
                        "a/c:35:2", "b:33:3", "c/:0:0");
        MockContent fileContent4 = prepareContentProvider(fileProvider, "ds4", ":0:0");
        MockContent pathInfoContent4 = prepareContentProvider(pathInfoProvider, "ds4");
        MockContent fileContent5 = prepareContentProvider(fileProvider, "ds5");
        MockContent pathInfoContent5 = prepareContentProvider(pathInfoProvider, "ds5");

        task.execute();

        System.out.println(">>>>>> LOG:\n"+String.join("\n", logRecorder.getLogLines()));
        System.out.println("<<<<<<");
        assertThat(logRecorder.getLogLines(),
                hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - Check 5 data sets registered since 1977-06-15 23:53:08"));

        assertThat(
                logRecorder.getLogLines(),
                hasItem("ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - File system and path info DB consistency check report for all data sets since 1977-06-15 23:53:08"));
        assertThat(logRecorder.getLogLines(), hasItem("Data sets checked:"));
        assertThat(logRecorder.getLogLines(), hasItem("[ds1, ds2, ds3, ds4, ds5]"));
        assertThat(logRecorder.getLogLines(), hasItem("Differences found:"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds1:"));
        assertThat(logRecorder.getLogLines(),
                hasItem("- 'a/b' CRC32 checksum in the file system = 00000009 but in the path info database = 00000007"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'a/c' size in the file system = 35 bytes but in the path info database = 42 bytes."));
        assertThat(logRecorder.getLogLines(), hasItem("- 'b' is on the file system but is not referenced in the path info database"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'c' is a directory in the file system but a file in the path info database"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds2:"));
        assertThat(logRecorder.getLogLines(), hasItem("- exists in the path info database but does not exist in the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds3:"));
        assertThat(logRecorder.getLogLines(),
                hasItem("- 'a/b' checksum in the file system = SHA:81fe8bfe87576c3ecb22426f8e57847382917acf but in the path info database = SHA:78bc65"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'a/c' size in the file system = 42 bytes but in the path info database = 35 bytes."));
        assertThat(logRecorder.getLogLines(), hasItem("- 'b' is referenced in the path info database but does not exist on the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'c' is a directory in the path info database but a file in the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds5:"));
        assertThat(logRecorder.getLogLines(), hasItem("- exists neither in the path info database nor in the file system"));

        assertEquals(true, fileContent1.isClosed());
        assertEquals(true, pathInfoContent1.isClosed());
        assertEquals(true, fileContent2.isClosed());
        assertEquals(true, pathInfoContent2.isClosed());
        assertEquals(true, fileContent3.isClosed());
        assertEquals(true, pathInfoContent3.isClosed());
        assertEquals(true, fileContent4.isClosed());
        assertEquals(true, pathInfoContent4.isClosed());
        assertEquals(true, fileContent5.isClosed());
        assertEquals(true, pathInfoContent5.isClosed());
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:53:08 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder.recordedObject().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testWithChunksAndInconsistentCase()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHECKING_TIME_INTERVAL_KEY, "25000 msec");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.PAUSING_TIME_POINT_KEY, "2:23");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHUNK_SIZE_KEY, "2");
        task.setUp("", properties);
        long start = startTimestamp - 10000;
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").registrationDate(new Date(start + 1000)).getDataSet();
        PhysicalDataSet ds2 = createDataSetBuilder().code("ds2").registrationDate(new Date(start + 2000)).getDataSet();
        PhysicalDataSet ds3 = createDataSetBuilder().code("ds3").registrationDate(new Date(start + 5000)).getDataSet();
        PhysicalDataSet ds4 = createDataSetBuilder().code("ds4").registrationDate(new Date(start + 5000)).getDataSet();
        PhysicalDataSet ds5 = createDataSetBuilder().code("ds5").registrationDate(new Date(start + 5000)).getDataSet();
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder1 = prepareListDataSets(ds1, ds2);
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder2 = prepareListDataSets(ds5, ds4, ds3);
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder3 = prepareListDataSets(ds5, ds4, ds3);
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder4 = prepareListDataSets();
        MockContent fc1 = prepareContentProvider(fileProvider, "ds1", ":0:0", "a:34:9");
        MockContent pic1 = prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a:34:9");
        MockContent fc2 = prepareContentProvider(fileProvider, "ds2", ":0:0", "b:35:10");
        MockContent pic2 = prepareContentProvider(pathInfoProvider, "ds2", ":0:0", "b:35:10");
        MockContent fc3 = prepareContentProvider(fileProvider, "ds3", ":0:0", "c:36:11");
        MockContent pic3 = prepareContentProvider(pathInfoProvider, "ds3", ":0:0", "c:36:11");
        MockContent fc4 = prepareContentProvider(fileProvider, "ds4", ":0:0", "d:37:123");
        MockContent pic4 = prepareContentProvider(pathInfoProvider, "ds4", ":0:0", "d:37:12");
        MockContent fc5 = prepareContentProvider(fileProvider, "ds5", ":0:0", "e:38:13");
        MockContent pic5 = prepareContentProvider(pathInfoProvider, "ds5", ":0:0", "e:38:13");

        // When
        task.execute();

        // Then
        System.out.println("====LOG\n"+logRecorder.getLogContent()+"\n======");
        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 2 data sets registered since 1977-06-15 23:52:55"));
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:52:55 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder1.recordedObject().toString());
        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 3 data sets registered since 1977-06-15 23:53:02"));
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:53:02 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder2.recordedObject().toString());
        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 0 data sets registered since 1977-06-15 23:52:55"));
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:53:05 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder3.recordedObject().toString());
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Wed Jun 15 23:52:55 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder4.recordedObject().toString());
        assertThat(logRecorder.getLogLines(), hasItem("ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "File system and path info DB consistency check report for all data sets "
                + "between 1977-06-15 23:52:55 and 1977-06-15 23:53:05"));
        assertThat(logRecorder.getLogLines(), hasItem("Data sets checked:"));
        assertThat(logRecorder.getLogLines(), hasItem("[ds1, ds2, ds3, ds4, ds5]"));
        assertThat(logRecorder.getLogLines(), hasItem("Differences found:"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds4:"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'd' CRC32 checksum in the file system = 00000123 but in the path info database = 00000012"));
        assertEquals(true, fc1.isClosed());
        assertEquals(true, pic1.isClosed());
        assertEquals(true, fc2.isClosed());
        assertEquals(true, pic2.isClosed());
        assertEquals(true, fc3.isClosed());
        assertEquals(true, pic3.isClosed());
        assertEquals(true, fc4.isClosed());
        assertEquals(true, pic4.isClosed());
        assertEquals(true, fc5.isClosed());
        assertEquals(true, pic5.isClosed());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithInteruption()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHECKING_TIME_INTERVAL_KEY, "25000 msec");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.PAUSING_TIME_POINT_KEY, "2:23");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHUNK_SIZE_KEY, "2");
        task = new MockDataSetAndPathInfoDBConsistencyCheckTask(fileProvider, pathInfoProvider, 
                directoryProvidor, service, new MockTimeProvider(startTimestamp, 60 * 60 * 1000));
        task.setUp("", properties);
        long start = startTimestamp - 10000;
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").registrationDate(new Date(start + 1000)).getDataSet();
        PhysicalDataSet ds2 = createDataSetBuilder().code("ds2").registrationDate(new Date(start + 2000)).getDataSet();
        PhysicalDataSet ds3 = createDataSetBuilder().code("ds3").registrationDate(new Date(start + 5000)).getDataSet();
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder = prepareListDataSets(ds1, ds2, ds3);
        MockContent fc1 = prepareContentProvider(fileProvider, "ds1", ":0:0", "a:34:9");
        MockContent pic1 = prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a:34:9");
        MockContent fc2 = prepareContentProvider(fileProvider, "ds2", ":0:0", "b:35:10");
        MockContent pic2 = prepareContentProvider(pathInfoProvider, "ds2", ":0:0", "b:35:10");
        
        // When
        task.execute();
        
        // Then
        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 3 data sets registered since 1977-06-16 00:52:45"));
        assertEquals("DATASET\n"
                + "    with operator 'AND'\n"
                + "    with attribute 'registration_date' later than or equal to 'Thu Jun 16 00:52:45 CET 1977'\n"
                + "    with physical_data:\n"
                + "        with operator 'AND'\n"
                + "        with attribute 'storageConfirmation' true\n"
                + "        with attribute 'status' AVAILABLE\n", searchCriteriaRecorder.recordedObject().toString());
        assertEquals(true, fc1.isClosed());
        assertEquals(true, pic1.isClosed());
        assertEquals(true, fc2.isClosed());
        assertEquals(true, pic2.isClosed());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testWithInteruptionButNotInTimeInterval()
    {
        // Given
        Properties properties = new Properties();
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHECKING_TIME_INTERVAL_KEY, "25000 msec");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.PAUSING_TIME_POINT_KEY, "2:23");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CONTINUING_TIME_POINT_KEY, "23:55");
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHUNK_SIZE_KEY, "2");
        task = new MockDataSetAndPathInfoDBConsistencyCheckTask(fileProvider, pathInfoProvider, 
                directoryProvidor, service, new MockTimeProvider(startTimestamp, 60 * 60 * 1000));
        task.setUp("", properties);
        
        // When
        task.execute();
        
        // Then
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }
    
    private void prepareContentProvider(final IHierarchicalContentProvider provider,
            final String dataSetCode, final Exception exception)
    {
        context.checking(new Expectations()
            {
                {
                    one(provider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(throwException(exception));
                }
            });
    }

    private MockContent prepareContentProvider(final IHierarchicalContentProvider provider,
            final String dataSetCode, final String... nodeDescriptions)
    {
        final MockContent content = new MockContent(nodeDescriptions);
        context.checking(new Expectations()
            {
                {
                    one(provider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(returnValue(content));
                }
            });
        return content;
    }

    private RecordingMatcher<DataSetSearchCriteria> prepareListDataSets(final PhysicalDataSet... dataSets)
    {
        List<DataSet> translatedDataSets = new ArrayList<DataSet>();
        for (PhysicalDataSet physicalDataSet : dataSets)
        {
            translatedDataSets.add(translate(physicalDataSet));
        }
        RecordingMatcher<DataSetSearchCriteria> searchCriteriaRecorder = new RecordingMatcher<DataSetSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).searchDataSets(with(SESSION_TOKEN), with(searchCriteriaRecorder), 
                            with(new RecordingMatcher<DataSetFetchOptions>()));
                    will(returnValue(new SearchResult<>(translatedDataSets, translatedDataSets.size())));
                }
            });
        return searchCriteriaRecorder;
    }
    
    private void setUpForUninterruptedProcessing()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHECKING_TIME_INTERVAL_KEY,
                "1500 msec");
        task.setUp("", properties);
    }

    private DataSet translate(PhysicalDataSet dataSet)
    {
        DataSet translatedDataSet = new DataSet();
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        translatedDataSet.setFetchOptions(fetchOptions);
        translatedDataSet.setCode(dataSet.getCode());
        translatedDataSet.setRegistrationDate(dataSet.getRegistrationDate());
        PhysicalData physicalData = new PhysicalData();
        physicalData.setLocation(dataSet.getLocation());
        translatedDataSet.setPhysicalData(physicalData);
        return translatedDataSet;
    }

    private DataSetBuilder createDataSetBuilder()
    {
        return new DataSetBuilder().store(new DataStoreBuilder("DSS").getStore()).fileFormat("XML").registrationDate(new Date(startTimestamp));
    }

}
