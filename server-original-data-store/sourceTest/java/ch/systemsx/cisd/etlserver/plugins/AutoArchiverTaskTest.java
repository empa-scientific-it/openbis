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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class AutoArchiverTaskTest extends AssertJUnit
{
    public static final class MockDiscoverer implements IArchiveCandidateDiscoverer
    {
        private List<AbstractExternalData> dataSets;

        public MockDiscoverer(Properties properties)
        {
            List<String> dataSetCodes = PropertyUtils.getList(properties, "data-sets");
            dataSets = new ArrayList<AbstractExternalData>();
            for (String dataSetCode : dataSetCodes)
            {
                dataSets.add(new DataSetBuilder().code(dataSetCode).getDataSet());
            }
        }

        @Override
        public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis,
                ArchiverDataSetCriteria criteria)
        {
            return dataSets;
        }
    }

    public static final class MockPolicy implements IAutoArchiverPolicy
    {
        private Set<String> dataSetToBeFiltered;

        public MockPolicy(Properties properties)
        {
            dataSetToBeFiltered = new HashSet<String>(PropertyUtils.getList(properties, "data-sets"));
        }

        @Override
        public List<AbstractExternalData> filter(List<AbstractExternalData> dataSets)
        {
            List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
            for (AbstractExternalData dataSet : dataSets)
            {
                if (dataSetToBeFiltered.contains(dataSet.getCode()))
                {
                    result.add(dataSet);
                }
            }
            return result;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        service = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        String logContent = logRecorder.getLogContent();
        System.out.println("======= Log content for " + result.getName() + "():");
        System.out.println(logContent);
        System.out.println("=======");
        if (result.getStatus() == ITestResult.FAILURE)
        {
            fail(result.getName() + " failed. Log content:\n" + logContent);
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoCandidates()
    {
        RecordingMatcher<ArchiverDataSetCriteria> criteriaRecorder = prepareListAvailableDataSets();

        createAutoArchiver(new Properties()).execute();

        assertLogs(nothingLog());
        assertEquals(30, criteriaRecorder.recordedObject().getOlderThan());
        assertEquals(null, criteriaRecorder.recordedObject().tryGetDataSetTypeCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivingWithDefaultProperties()
    {
        PhysicalDataSet ds1 = new DataSetBuilder().code("ds1").getDataSet();
        RecordingMatcher<ArchiverDataSetCriteria> criteriaRecorder = prepareListAvailableDataSets(ds1);
        prepareArchiveDataSets(false, Arrays.asList("ds1"));

        createAutoArchiver(new Properties()).execute();

        assertLogs(applyLog(1), archivingLog("ds1"));
        assertEquals(30, criteriaRecorder.recordedObject().getOlderThan());
        assertEquals(null, criteriaRecorder.recordedObject().tryGetDataSetTypeCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivingWithNoFilteredDataSets()
    {
        PhysicalDataSet ds1 = new DataSetBuilder().code("ds1").getDataSet();
        RecordingMatcher<ArchiverDataSetCriteria> criteriaRecorder = prepareListAvailableDataSets(ds1);
        Properties properties = new Properties();
        properties.setProperty("policy.class", MockPolicy.class.getName());

        createAutoArchiver(properties).execute();

        assertLogs(applyLog(1), nothingLog());
        assertEquals(30, criteriaRecorder.recordedObject().getOlderThan());
        assertEquals(null, criteriaRecorder.recordedObject().tryGetDataSetTypeCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivingWithFilteredDataSets()
    {
        PhysicalDataSet ds1 = new DataSetBuilder().code("ds1").getDataSet();
        PhysicalDataSet ds2 = new DataSetBuilder().code("ds2").getDataSet();
        PhysicalDataSet ds3 = new DataSetBuilder().code("ds3").getDataSet();
        RecordingMatcher<ArchiverDataSetCriteria> criteriaRecorder = prepareListAvailableDataSets(ds1, ds2, ds3);
        Properties properties = new Properties();
        properties.setProperty("older-than", "10");
        properties.setProperty("data-set-type", "MY-TYPE");
        properties.setProperty("remove-datasets-from-store", "true");
        properties.setProperty("policy.class", MockPolicy.class.getName());
        properties.setProperty("policy.data-sets", "ds1, ds3");
        prepareArchiveDataSets(true, Arrays.asList("ds1", "ds3"));

        createAutoArchiver(properties).execute();

        assertLogs(applyLog(3), archivingLog("ds1", "ds3"));
        assertEquals(10, criteriaRecorder.recordedObject().getOlderThan());
        assertEquals("MY-TYPE", criteriaRecorder.recordedObject().tryGetDataSetTypeCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivingWithCustomDiscoverer()
    {
        Properties properties = new Properties();
        properties.setProperty("archive-candidate-discoverer.data-sets", "ds1, ds2");
        properties.setProperty("archive-candidate-discoverer.class", MockDiscoverer.class.getName());
        prepareArchiveDataSets(false, Arrays.asList("ds1", "ds2"));

        createAutoArchiver(properties).execute();

        assertLogs(applyLog(2), archivingLog("ds1", "ds2"));
        context.assertIsSatisfied();
    }

    private void prepareArchiveDataSets(final boolean removeFromDataStore, final List<String> dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).archiveDataSets(with(dataSetCodes), with(removeFromDataStore), with(new IsAnything<Map<String, String>>()));
                }
            });
    }

    private RecordingMatcher<ArchiverDataSetCriteria> prepareListAvailableDataSets(final AbstractExternalData... dataSets)
    {
        final RecordingMatcher<ArchiverDataSetCriteria> criteriaRecorder = new RecordingMatcher<ArchiverDataSetCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).listAvailableDataSets(with(criteriaRecorder));
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
        return criteriaRecorder;
    }

    private String applyLog(int numberOfCandidates)
    {
        return "apply policy to " + numberOfCandidates + " candidates for archiving.";
    }

    private String nothingLog()
    {
        return "nothing to archive";
    }

    private String archivingLog(String... dataSetCodes)
    {
        return "archiving: " + Arrays.asList(dataSetCodes);
    }

    private void assertLogs(String... expectedLogEntries)
    {
        assertEquals(createBasicLogExpectation(expectedLogEntries).toString().trim(), logRecorder.getLogContent());
    }

    private StringBuilder createBasicLogExpectation(String... expectedLogEntries)
    {
        StringBuilder builder = new StringBuilder();
        List<String> entries = new ArrayList<String>(Arrays.asList(expectedLogEntries));
        entries.add(0, "Plugin AutoArchiver initialized");
        for (String logEntry : entries)
        {
            builder.append("INFO  OPERATION.AutoArchiverTask - ").append(logEntry).append('\n');
        }
        return builder;
    }

    private AutoArchiverTask createAutoArchiver(Properties properties)
    {
        AutoArchiverTask autoArchiverTask = new AutoArchiverTask();
        autoArchiverTask.setUp("AutoArchiver", properties);
        return autoArchiverTask;
    }
}
