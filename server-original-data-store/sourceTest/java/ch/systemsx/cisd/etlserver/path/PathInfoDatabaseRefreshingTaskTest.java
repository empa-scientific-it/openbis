/*
 * Copyright 2017 ETH Zuerich, SIS
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

import static ch.systemsx.cisd.etlserver.path.PathInfoDatabaseRefreshingTask.CHUNK_SIZE_KEY;
import static ch.systemsx.cisd.etlserver.path.PathInfoDatabaseRefreshingTask.STATE_FILE_KEY;
import static ch.systemsx.cisd.etlserver.path.PathInfoDatabaseRefreshingTask.TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.ARCHIVED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;

import java.io.File;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseRefreshingTaskTest extends AbstractFileSystemTestCase
{
    private static final String LOG_PREFIX = "INFO  OPERATION.PathInfoDatabaseRefreshingTask - ";

    private static final String T1 = "2017-01-21 15:42:47";

    private static final String T2 = "2017-01-21 15:42:48";

    private static final String T3 = "2017-01-21 15:42:49";

    private static final String T4 = "2017-01-23 15:42:48";

    private static final String T5 = "2017-01-24 15:42:48";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private IHierarchicalContentFactory contentFactory;

    private IDataSetDirectoryProvider directoryProvider;

    private PathInfoDatabaseRefreshingTask task;

    private File store;

    private IShareIdManager shareIdManager;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IPathsInfoDAO.class);
        contentFactory = new DefaultFileBasedHierarchicalContentFactory();
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        store = new File(workingDirectory, "store");
        store.mkdir();
        context.checking(new Expectations()
            {
                {
                    allowing(directoryProvider).getStoreRoot();
                    will(returnValue(store));

                    allowing(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        task = new PathInfoDatabaseRefreshingTask(service, dao, contentFactory, directoryProvider);
    }

    @AfterMethod
    public void tearDown(Method method)
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
    public void testNoProperties()
    {
        try
        {
            task.setUp(null, new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Either property '" + TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY + "' is defined or '"
                    + createDefaultStateFile().getAbsolutePath() + "' exists.", ex.getMessage());
        }
    }

    @Test
    public void testInvalidTimeStampOfYoungestDataSetProperty()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY, "abcde");
        try
        {
            task.setUp(null, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid property '" + TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY + "': abcde", ex.getMessage());
        }
    }

    @Test
    public void testStateFileIsADirectory()
    {
        Properties properties = new Properties();
        properties.setProperty(STATE_FILE_KEY, store.getPath());
        try
        {
            task.setUp(null, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("File '" + store.getAbsolutePath() + "' (specified by property '"
                    + STATE_FILE_KEY + "') is a directory.", ex.getMessage());
        }
    }

    @Test
    public void testAllDataSetTypesFirstRun()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY, T4);
        task.setUp(null, properties);
        PhysicalDataSet ds1 = new DataSetBuilder(1).code("archived").registrationDate(asDate(T1))
                .status(ARCHIVED).getDataSet();
        ContainerDataSet ds2 = new ContainerDataSetBuilder(2).code("container").registrationDate(asDate(T2))
                .getContainerDataSet();
        PhysicalDataSet ds3 = new DataSetBuilder(3).code("ds-3").registrationDate(asDate(T2))
                .status(AVAILABLE).location("1/ds-3").getDataSet();
        PhysicalDataSet ds4 = new DataSetBuilder(3).code("too-young").registrationDate(asDate(T5))
                .status(AVAILABLE).getDataSet();
        PhysicalDataSet ds5 = new DataSetBuilder(3).code("ds-5").registrationDate(asDate(T3))
                .status(AVAILABLE).location("1/ds-5").getDataSet();
        RecordingMatcher<SearchCriteria> criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3, ds4, ds5);
        prepareDeleteAndLockDataSet(ds3);
        prepareDeleteAndLockDataSet(ds5);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,{SearchCriteria.TimeAttributeMatchClause[ATTRIBUTE,"
                + "REGISTRATION_DATE,2017-01-23 15:42:48,LESS_THAN_OR_EQUAL]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-5 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-3 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        File stateFile = createDefaultStateFile();
        assertEquals(T2 + " [ds-3]", FileUtilities.loadToString(stateFile).trim());
    }

    @Test
    public void testDataSetTypeAndSecondRun()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY, T5);
        File stateFile = new File(store, "ts.txt");
        FileUtilities.writeToFile(stateFile, T3);
        properties.setProperty(PathInfoDatabaseRefreshingTask.STATE_FILE_KEY, stateFile.getPath());
        properties.setProperty(PathInfoDatabaseRefreshingTask.DATA_SET_TYPE_KEY, "A");
        task.setUp(null, properties);
        PhysicalDataSet ds1 = new DataSetBuilder(1).code("ds-1").registrationDate(asDate(T1))
                .status(AVAILABLE).location("1/ds-1").getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder(1).code("ds-2").registrationDate(asDate(T2))
                .status(AVAILABLE).location("1/ds-2").getDataSet();
        PhysicalDataSet ds3 = new DataSetBuilder(1).code("too-young").registrationDate(asDate(T4))
                .status(AVAILABLE).getDataSet();
        RecordingMatcher<SearchCriteria> criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3);
        prepareDeleteAndLockDataSet(ds1);
        prepareDeleteAndLockDataSet(ds2);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,"
                + "{SearchCriteria.TimeAttributeMatchClause[ATTRIBUTE,REGISTRATION_DATE,"
                + "2017-01-21 15:42:49,LESS_THAN_OR_EQUAL],"
                + "SearchCriteria.AttributeMatchClause[ATTRIBUTE,TYPE,A,EQUALS]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-2 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-1 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        assertEquals(T1 + " [ds-1]", FileUtilities.loadToString(stateFile).trim());
    }

    @Test
    public void testChunkSize()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY, T4);
        properties.setProperty(CHUNK_SIZE_KEY, "2");
        task.setUp(null, properties);
        PhysicalDataSet ds1 = new DataSetBuilder(1).code("ds-1").registrationDate(asDate(T1))
                .status(AVAILABLE).location("1/ds-1").getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder(1).code("ds-2").registrationDate(asDate(T2))
                .status(AVAILABLE).location("1/ds-2").getDataSet();
        PhysicalDataSet ds3 = new DataSetBuilder(1).code("ds-3").registrationDate(asDate(T3))
                .status(AVAILABLE).location("1/ds-3").getDataSet();
        RecordingMatcher<SearchCriteria> criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3);
        prepareDeleteAndLockDataSet(ds2);
        prepareDeleteAndLockDataSet(ds3);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,{SearchCriteria.TimeAttributeMatchClause[ATTRIBUTE,"
                + "REGISTRATION_DATE,2017-01-23 15:42:48,LESS_THAN_OR_EQUAL]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-3 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-2 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        File stateFile = createDefaultStateFile();
        assertEquals(T2 + " [ds-2]", FileUtilities.loadToString(stateFile).trim());
    }

    @Test
    public void testExecuteThreeTimes()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_STAMP_OF_YOUNGEST_DATA_SET_KEY, T4);
        properties.setProperty(CHUNK_SIZE_KEY, "2");
        task.setUp(null, properties);
        PhysicalDataSet ds1 = new DataSetBuilder(1).code("ds-1").registrationDate(asDate(T1))
                .status(AVAILABLE).location("1/ds-1").getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder(1).code("ds-2").registrationDate(asDate(T2))
                .status(AVAILABLE).location("1/ds-2").getDataSet();
        PhysicalDataSet ds3 = new DataSetBuilder(1).code("ds-3").registrationDate(asDate(T3))
                .status(AVAILABLE).location("1/ds-3").getDataSet();
        RecordingMatcher<SearchCriteria> criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3);
        prepareDeleteAndLockDataSet(ds2);
        prepareDeleteAndLockDataSet(ds3);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,{SearchCriteria.TimeAttributeMatchClause[ATTRIBUTE,"
                + "REGISTRATION_DATE,2017-01-23 15:42:48,LESS_THAN_OR_EQUAL]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-3 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-2 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        File stateFile = createDefaultStateFile();
        assertEquals(T2 + " [ds-2]", FileUtilities.loadToString(stateFile).trim());

        criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3);
        prepareDeleteAndLockDataSet(ds1);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,{SearchCriteria.TimeAttributeMatchClause["
                + "ATTRIBUTE,REGISTRATION_DATE," + T2 + ",LESS_THAN_OR_EQUAL]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-3 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-2 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.\n" +
                LOG_PREFIX + "Refresh path info for 1 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-1 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 1 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        assertEquals(T1 + " [ds-1]", FileUtilities.loadToString(stateFile).trim());

        criteriaMatcher = prepareGetPhysicalDataSets(ds1, ds2, ds3);

        task.execute();

        assertEquals("SearchCriteria[MATCH_ALL_CLAUSES,{SearchCriteria.TimeAttributeMatchClause["
                + "ATTRIBUTE,REGISTRATION_DATE," + T1 + ",LESS_THAN_OR_EQUAL]},[]]",
                criteriaMatcher.recordedObject().toString());
        AssertionUtil.assertContainsLines(LOG_PREFIX + "Refresh path info for 2 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-3 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Paths inside data set ds-2 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 2 physical data sets refreshed in 0 secs.\n" +
                LOG_PREFIX + "Refresh path info for 1 physical data sets.\n" +
                LOG_PREFIX + "Paths inside data set ds-1 successfully added to database. Data set size: 0\n" +
                LOG_PREFIX + "Path info for 1 physical data sets refreshed in 0 secs.",
                logRecorder.getLogContent());
        assertEquals(T1 + " [ds-1]", FileUtilities.loadToString(stateFile).trim());
    }

    private void prepareDeleteAndLockDataSet(final PhysicalDataSet dataSet)
    {
        final String dataSetCode = dataSet.getCode();
        final File file = new File(store, dataSet.getLocation());
        file.mkdirs();
        context.checking(new Expectations()
            {
                {
                    one(dao).deleteDataSet(dataSetCode);

                    one(dao).tryGetDataSetId(dataSetCode);
                    will(returnValue(null));

                    one(dao).createDataSet(dataSetCode, dataSet.getLocation());
                    Long id = dataSet.getId();
                    will(returnValue(id));

                    one(dao).createDataSetFile(id, null, "", dataSetCode, 0L, true, null, null, new Date(file.lastModified()));

                    one(dao).commit();

                    one(shareIdManager).lock(dataSetCode);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(file));

                    one(shareIdManager).releaseLocks();
                }
            });
    }

    private RecordingMatcher<SearchCriteria> prepareGetPhysicalDataSets(final AbstractExternalData... dataSets)
    {
        final RecordingMatcher<SearchCriteria> recordingMatcher = new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(with(recordingMatcher));
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
        return recordingMatcher;
    }

    private Date asDate(String dateString)
    {
        try
        {
            return new SimpleDateFormat(AbstractMaintenanceTaskWithStateFile.TIME_STAMP_FORMAT).parse(dateString);
        } catch (ParseException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private File createDefaultStateFile()
    {
        return new File(store, PathInfoDatabaseRefreshingTask.class.getSimpleName() + "-state.txt");
    }

}
