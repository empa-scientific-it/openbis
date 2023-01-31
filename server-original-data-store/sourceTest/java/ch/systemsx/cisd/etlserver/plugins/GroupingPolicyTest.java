/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouping;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class GroupingPolicyTest extends AbstractAutoArchiverPolicyTestCase
{
    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUpLogRecorder()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        System.out.println("======= Log content for " + method.getName() + "():");
        System.out.println(logRecorder.getLogContent());
        System.out.println("=======");
        logRecorder.reset();
    }

    @Test
    public void testInvalidGroupingKey()
    {
        assertInvalidGroupingKeys("hello", "Invalid basic grouping key in property 'grouping-keys': "
                + "hello (valid values are " + Arrays.asList(Grouping.values()) + ")");
        assertInvalidGroupingKeys("Space, Space:blub", "Invalid grouping key in property 'grouping-keys' "
                + "because 'merge' is expected after ':': Space:blub");
    }

    private void assertInvalidGroupingKeys(String groupingKeys, String expectedExceptionMessage)
    {
        try
        {
            createPolicy(0, 1, groupingKeys);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(expectedExceptionMessage, ex.getMessage());
        }
    }

    @Test
    public void testAllEmpty()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        assertEquals("[]", filter(40, 100, "All", dataSets).toString());
        assertEquals("", logRecorder.getLogContent());
    }

    @Test
    public void testAll()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s2", "p2", "e4", "dt2", "ds2", 10L));
        dataSets.add(ctx.createDataset("s3", "p3", "e5", "dt3", "ds3", 10L));
        dataSets.add(ctx.createDataset("s4", "p4", "e6", "dt4", "ds4", 10L));

        List<String> filteredDataSets = filter(40, 100, "All", dataSets);

        assertLogs(searchLog("40 bytes", "100 bytes", dataSets), groupingKeyLog("All", 4, 1),
                groupsMatchLog(1, 0, 0), filteredLog(filteredDataSets));
        assertEquals("[ds1, ds2, ds3, ds4]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceOneGroupTooSmallNoMerge()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 10L));

        List<String> filteredDataSets = filter(25, 100, "Space:merge", dataSets);

        assertLogs(1, "25 bytes", "100 bytes", searchLog("25 bytes", "100 bytes", dataSets),
                groupingKeyLog("Space:merge", 1, 1), groupsMatchLog(0, 1, 0));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceAllGroupsTooSmall()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds2", 10L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds3", 10L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt1", "ds4", 10L));

        List<String> filteredDataSets = filter(25, 100 * FileUtils.ONE_MB, "Space", dataSets);

        assertLogs(4, "25 bytes", "100 MB", searchLog("25 bytes", "100 MB", dataSets),
                groupingKeyLog("Space", 4, 3), groupsMatchLog(0, 3, 0));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceAllGroupsTooSmallMerge()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 7000L, 10L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds2", 4000L, 11L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt1", "ds3", 6000L, 12L));
        dataSets.add(ctx.createDataset("s4", "p1", "e1", "dt1", "ds4", 2000L, 13L));

        List<String> filteredDataSets = filter(25, 100 * FileUtils.ONE_MB, "Space:merge", dataSets);

        assertLogs(searchLog("25 bytes", "100 MB", dataSets), groupingKeyLog("Space:merge", 4, 4),
                groupsMatchLog(0, 4, 0), mergedLog(3), filteredLog(filteredDataSets));
        assertEquals("[ds4, ds2, ds3]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceAllGroupsTooLarge()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 40L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds2", 70L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds3", 101L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt1", "ds4", 101L));

        List<String> filteredDataSets = filter(25, 100, "Space", dataSets);

        assertLogs(4, "25 bytes", "100 bytes", searchLog("25 bytes", "100 bytes", dataSets),
                groupingKeyLog("Space", 4, 3), groupsMatchLog(0, 0, 3));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceAllGroupsTooLargeOrTooSmall()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds2", 10L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds3", 101L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt1", "ds4", 101L));

        List<String> filteredDataSets = filter(25, 100, "Space", dataSets);

        assertLogs(4, "25 bytes", "100 bytes", searchLog("25 bytes", "100 bytes", dataSets),
                groupingKeyLog("Space", 4, 3), groupsMatchLog(0, 1, 2));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceBestGroupsJustAtMaxSize()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds2", 10L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds3", 50L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "ds4", 50L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt1", "ds5", 101L));

        List<String> filteredDataSets = filter(25, 100, "Space", dataSets);

        assertLogs(searchLog("25 bytes", "100 bytes", dataSets), groupingKeyLog("Space", 5, 3),
                groupsMatchLog(1, 1, 1), filteredLog(filteredDataSets));
        assertEquals("[ds3, ds4]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceOldestDataSet()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "ds2", 10L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt3", "ds3", 200000L, 50L));
        dataSets.add(ctx.createDataset("s3", "p1", "e1", "dt4", "ds4", 100000L, 70L));
        dataSets.add(ctx.createDataset("s4", "p1", "e1", "dt5", "ds5", 101L));

        List<String> filteredDataSets = filter(25, 100, "Space", dataSets);

        assertLogs(searchLog("25 bytes", "100 bytes", dataSets), groupingKeyLog("Space", 5, 4),
                groupsMatchLog(2, 1, 1), oldestLog(100000L),
                filteredLog(filteredDataSets));
        assertEquals("[ds4]", filteredDataSets.toString());
    }

    @Test
    public void testSpaceSingleton()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt3", "ds3", 50L));

        List<String> filteredDataSets = filter(25, 100, "Space", dataSets);

        assertLogs(searchLog("25 bytes", "100 bytes", dataSets), groupingKeyLog("Space", 1, 1),
                groupsMatchLog(1, 0, 0), filteredLog(filteredDataSets));
        assertEquals("[ds3]", filteredDataSets.toString());
    }

    @Test
    public void testProjectDataSetTypeAllGroupsTooSmall()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt1", "ds2", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt2", "ds3", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt2", "ds4", 10L));

        List<String> filteredDataSets = filter(25, 100, "Project#DataSetType", dataSets);

        assertLogs(4, "25 bytes", "100 bytes", searchLog("25 bytes", "100 bytes", dataSets),
                groupingKeyLog("Project#DataSetType", 4, 2), groupsMatchLog(0, 2, 0));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testCombinationProjectAndDataSetType()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 20000L, 71L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "ds2", 10000L, 42L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "ds3", 30000L, 42L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "ds4", 15000L, 73L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "ds5", 40000L, 74L));

        List<String> filteredDataSets = filter(25, 100, "Project#DataSetType", dataSets);

        assertLogs(searchLog("25 bytes", "100 bytes", dataSets), groupingKeyLog("Project#DataSetType", 5, 4),
                groupsMatchLog(4, 0, 0), oldestLog(15000), filteredLog(filteredDataSets));
        assertEquals("[ds4]", filteredDataSets.toString());
    }

    @Test
    public void testProjectAllGroupsTooLargeButSequenceProjectAndProjectDataSetTypeFits()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 20000L, 71L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "ds2", 10000L, 42L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "ds3", 30000L, 42L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "ds4", 15000L, 73L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "ds5", 40000L, 74L));

        assertEquals("[]", filter(25, 100, "Project", dataSets).toString());

        logRecorder.resetLogContent();
        List<String> filteredDataSets = filter(25, 100, "Project, Project#DataSetType", dataSets);

        assertLogs(searchLog("25 bytes", "100 bytes", dataSets), groupingKeyLog("Project", 5, 2),
                groupsMatchLog(0, 0, 2), groupingKeyLog("Project#DataSetType", 5, 4), groupsMatchLog(4, 0, 0),
                oldestLog(15000), filteredLog(filteredDataSets));
        assertEquals("[ds4]", filteredDataSets.toString());
    }

    @Test
    public void testByExperimentTooSmall()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "ds1", 11L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "ds2", 12L));
        dataSets.add(ctx.createDataset("s1", "p1", "e3", "dt1", "ds3", 15L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "ds4", 13L));
        assertEquals("[]", filter(20, 50, "DataSetType#Experiment", dataSets).toString());
        logRecorder.resetLogContent();

        List<String> filteredDataSets = filterByExperiment(20, 50, dataSets);

        assertLogs(searchLog("20 bytes", "50 bytes", dataSets), groupingKeyLog("DataSetType#Experiment", 4, 4),
                groupsMatchLog(0, 4, 0), groupingKeyLog("DataSetType#Project", 4, 2), groupsMatchLog(1, 1, 0),
                filteredLog(filteredDataSets));
        assertEquals("[ds1, ds2, ds3]", filteredDataSets.toString());
    }

    @Test
    public void testByExperimentTooLarge()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 19L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds2", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp2", "ds3", 18L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "smp1", "ds4", 10L));

        List<String> filteredDataSets = filterByExperiment(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets), groupingKeyLog("DataSetType#Experiment", 4, 2),
                groupsMatchLog(0, 1, 1), groupingKeyLog("DataSetType#Project", 4, 2), groupsMatchLog(0, 1, 1),
                groupingKeyLog("DataSetType#Experiment#Sample", 4, 3), groupsMatchLog(1, 2, 0),
                filteredLog(filteredDataSets));
        assertEquals("[ds1, ds2]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooSmall()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 6 * FileUtils.ONE_KB));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds2", 2 * FileUtils.ONE_KB));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "smp2", "ds3", 8 * FileUtils.ONE_KB));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "smp1", "ds4", 1 * FileUtils.ONE_KB));

        List<String> filteredDataSets = filterBySpace(20 * FileUtils.ONE_KB, 30 * FileUtils.ONE_KB, dataSets);

        assertLogs(4, "20 KB", "30 KB", searchLog("20 KB", "30 KB", dataSets),
                groupingKeyLog("DataSetType#Space", 4, 3), groupsMatchLog(0, 3, 0),
                groupingKeyLog("DataSetType#Project:merge", 4, 3), groupsMatchLog(0, 3, 0),
                mergedTooSmallLog(3, "17 KB", "20 KB"),
                groupingKeyLog("DataSetType#Experiment:merge", 4, 3), groupsMatchLog(0, 3, 0),
                mergedTooSmallLog(3, "17 KB", "20 KB"),
                groupingKeyLog("DataSetType#Experiment#Sample:merge", 4, 3), groupsMatchLog(0, 3, 0),
                mergedTooSmallLog(3, "17 KB", "20 KB"),
                groupingKeyLog("DataSet:merge", 4, 4), groupsMatchLog(0, 4, 0),
                mergedTooSmallLog(4, "17 KB", "20 KB"));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigButProjectLevelFits()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "smp1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds2", 11L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds3", 12L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "smp1", "ds4", 13L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "smp2", "ds5", 14L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets), groupingKeyLog("DataSetType#Space", 5, 3),
                groupsMatchLog(0, 2, 1), groupingKeyLog("DataSetType#Project:merge", 5, 4),
                groupsMatchLog(1, 3, 0), filteredLog(filteredDataSets));
        assertEquals("[ds2, ds3]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigOrTooSmallProjectLevelTooSmallButProjectGroupsMerged()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt2", "smp1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds2", 11L));
        dataSets.add(ctx.createDataset("s1", "p3", "e1", "dt1", "smp1", "ds3", 12L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt1", "smp1", "ds4", 13L));
        dataSets.add(ctx.createDataset("s2", "p1", "e1", "dt1", "smp2", "ds5", 14L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets), groupingKeyLog("DataSetType#Space", 5, 3),
                groupsMatchLog(0, 2, 1), groupingKeyLog("DataSetType#Project:merge", 5, 5),
                groupsMatchLog(0, 5, 0), mergedLog(2), filteredLog(filteredDataSets));
        assertEquals("[ds1, ds2]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigButExperimentLevelFits()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 10L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp1", "ds2", 11L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp1", "ds3", 11L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "smp1", "ds4", 12L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp1", "ds5", 13L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp2", "ds6", 14L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets), groupingKeyLog("DataSetType#Space", 6, 2),
                groupsMatchLog(0, 0, 2), groupingKeyLog("DataSetType#Project:merge", 6, 2),
                groupsMatchLog(0, 0, 2), groupingKeyLog("DataSetType#Experiment:merge", 6, 4),
                groupsMatchLog(2, 2, 0), oldestLog(0), filteredLog(filteredDataSets));
        assertEquals("[ds2, ds3]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigExperimentLevelTooSmallAndMergedTooBig()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 15L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp1", "ds2", 18L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp2", "ds3", 19L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "smp1", "ds4", 16L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp1", "ds5", 18L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp2", "ds6", 19L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(6, "20 bytes", "30 bytes", searchLog("20 bytes", "30 bytes", dataSets),
                groupingKeyLog("DataSetType#Space", 6, 2), groupsMatchLog(0, 0, 2),
                groupingKeyLog("DataSetType#Project:merge", 6, 2), groupsMatchLog(0, 0, 2),
                groupingKeyLog("DataSetType#Experiment:merge", 6, 4), groupsMatchLog(0, 2, 2),
                mergedTooLargeLog(2, "31 bytes", "30 bytes"),
                groupingKeyLog("DataSetType#Experiment#Sample:merge", 6, 6), groupsMatchLog(0, 6, 0),
                mergedTooLargeLog(2, "31 bytes", "30 bytes"),
                groupingKeyLog("DataSet:merge", 6, 6), groupsMatchLog(0, 6, 0),
                mergedTooLargeLog(2, "31 bytes", "30 bytes"));
        assertEquals("[]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigButSampleLevelFits()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 22000L, 20L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp2", "ds2", 24000L, 21L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp1", "ds3", 26000L, 22L));
        dataSets.add(ctx.createDataset("s1", "p1", "e2", "dt1", "smp2", "ds4", 28000L, 23L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "smp1", "ds5", 21000L, 24L));
        dataSets.add(ctx.createDataset("s1", "p2", "e1", "dt2", "smp2", "ds6", 23000L, 25L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp1", "ds7", 25000L, 26L));
        dataSets.add(ctx.createDataset("s1", "p2", "e2", "dt2", "smp2", "ds8", 27000L, 27L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets),
                groupingKeyLog("DataSetType#Space", 8, 2), groupsMatchLog(0, 0, 2),
                groupingKeyLog("DataSetType#Project:merge", 8, 2), groupsMatchLog(0, 0, 2),
                groupingKeyLog("DataSetType#Experiment:merge", 8, 4), groupsMatchLog(0, 0, 4),
                groupingKeyLog("DataSetType#Experiment#Sample:merge", 8, 8), groupsMatchLog(8, 0, 0),
                oldestLog(21000), filteredLog(filteredDataSets));
        assertEquals("[ds5]", filteredDataSets.toString());
    }

    @Test
    public void testBySpaceTooBigButDataSetLevelFits()
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds1", 19L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp1", "ds2", 19L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp2", "ds3", 20L));
        dataSets.add(ctx.createDataset("s1", "p1", "e1", "dt1", "smp2", "ds4", 19L));

        List<String> filteredDataSets = filterBySpace(20, 30, dataSets);

        assertLogs(searchLog("20 bytes", "30 bytes", dataSets),
                groupingKeyLog("DataSetType#Space", 4, 1), groupsMatchLog(0, 0, 1),
                groupingKeyLog("DataSetType#Project:merge", 4, 1), groupsMatchLog(0, 0, 1),
                groupingKeyLog("DataSetType#Experiment:merge", 4, 1), groupsMatchLog(0, 0, 1),
                groupingKeyLog("DataSetType#Experiment#Sample:merge", 4, 2), groupsMatchLog(0, 0, 2),
                groupingKeyLog("DataSet:merge", 4, 4), groupsMatchLog(1, 3, 0),
                filteredLog(filteredDataSets));
        assertEquals("[ds3]", filteredDataSets.toString());
    }

    private List<String> filterBySpace(long min, long max, List<AbstractExternalData> dataSets)
    {
        return filter(min, max, "DataSetType#Space, DataSetType#Project:merge, DataSetType#Experiment:merge, "
                + "DataSetType#Experiment#Sample:merge, DataSet:merge", dataSets);
    }

    private List<String> filterByExperiment(long min, long max, List<AbstractExternalData> dataSets)
    {
        return filter(min, max, "DataSetType#Experiment, DataSetType#Project, DataSetType#Experiment#Sample", dataSets);
    }

    private List<String> filter(long min, long max, String groupingKeys, List<AbstractExternalData> dataSets)
    {
        return Code.extractCodes(createPolicy(min, max, groupingKeys).filter(dataSets));
    }

    private IAutoArchiverPolicy createPolicy(long min, long max, String groupingKeys)
    {
        Properties properties = createPolicyProperties(min, max);
        properties.setProperty(GroupingPolicy.GROUPING_KEYS_KEY, groupingKeys);
        return new GroupingPolicy(properties);
    }

    private String searchLog(String minSize, String maxSize, List<AbstractExternalData> dataSets)
    {
        return "Search for a group of data sets with total size between " + minSize + " and " + maxSize
                + ". Data sets: " + Code.extractCodes(dataSets);
    }

    private String groupingKeyLog(String key, int dataSets, int groups)
    {
        return String.format("Grouping key: '%s' has grouped %d data sets into %d groups.", key, dataSets, groups);
    }

    private String groupsMatchLog(int match, int tooSmall, int tooLarge)
    {
        return String.format("%d groups match in size, %d groups are too small and %d groups are too large.",
                match, tooSmall, tooLarge);
    }

    private String oldestLog(long timestamp)
    {
        return String.format("All data sets of the selected group have been accessed at %s or before.",
                new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(new Date(timestamp)));
    }

    private String mergedLog(int groups)
    {
        return groups + " groups have been merged.";
    }

    private String mergedTooSmallLog(int groups, String size, String minSize)
    {
        return "Merging all " + groups + " groups gives a total size of " + size
                + " which is still below required minimum of " + minSize;
    }

    private String mergedTooLargeLog(int groups, String size, String maxSize)
    {
        return groups + " groups have been merged, but the total size of " + size
                + " is above the required maximum of " + maxSize;
    }

    private String filteredLog(Object filteredDataSets)
    {
        return String.format("filtered data sets: %s", filteredDataSets);
    }

    private void assertLogs(int dataSets, String minSize, String maxSize, String... expectedLogEntries)
    {
        StringBuilder builder = createBasicLogExpectation(expectedLogEntries);
        builder.append(String.format("WARN  NOTIFY.GroupingPolicy - "
                + "From %d data sets no group could be found to be fit between %s and %s\n\nLog:\n",
                dataSets, minSize, maxSize));
        for (String logEntry : expectedLogEntries)
        {
            builder.append(logEntry).append('\n');
        }
        assertEquals(builder.toString().trim(), logRecorder.getLogContent());
    }

    private void assertLogs(String... expectedLogEntries)
    {
        assertEquals(createBasicLogExpectation(expectedLogEntries).toString().trim(), logRecorder.getLogContent());
    }

    private StringBuilder createBasicLogExpectation(String... expectedLogEntries)
    {
        StringBuilder builder = new StringBuilder();
        for (String logEntry : expectedLogEntries)
        {
            builder.append("INFO  OPERATION.GroupingPolicy - ").append(logEntry).append('\n');
        }
        return builder;
    }
}
