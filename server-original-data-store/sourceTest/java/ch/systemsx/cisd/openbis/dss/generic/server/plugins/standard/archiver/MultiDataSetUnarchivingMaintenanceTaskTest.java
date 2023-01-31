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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class MultiDataSetUnarchivingMaintenanceTaskTest extends AssertJUnit
{
    private static final String LOG_PREFIX = "INFO  OPERATION.MultiDataSetUnarchivingMaintenanceTask - ";

    private static final class MockMultiDataSetUnarchivingMaintenanceTask extends MultiDataSetUnarchivingMaintenanceTask
    {

        private IEncapsulatedOpenBISService service;

        private IHierarchicalContentProvider hierarchicalContentProvider;

        private IDataStoreServiceInternal dataStoreService;

        private IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;

        private IMultiDataSetArchiverDBTransaction transaction;

        public void setService(IEncapsulatedOpenBISService service)
        {
            this.service = service;
        }

        public void setHierarchicalContentProvider(IHierarchicalContentProvider hierarchicalContentProvider)
        {
            this.hierarchicalContentProvider = hierarchicalContentProvider;
        }

        public void setDataStoreService(IDataStoreServiceInternal dataStoreService)
        {
            this.dataStoreService = dataStoreService;
        }

        public void setReadonlyQuery(IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery)
        {
            this.readonlyQuery = readonlyQuery;
        }

        public void setTransaction(IMultiDataSetArchiverDBTransaction transaction)
        {
            this.transaction = transaction;
        }

        @Override
        IEncapsulatedOpenBISService getASService()
        {
            return service;
        }

        @Override
        IHierarchicalContentProvider getHierarchicalContentProvider()
        {
            return hierarchicalContentProvider;
        }

        @Override
        IDataStoreServiceInternal getDataStoreService()
        {
            return dataStoreService;
        }

        @Override
        IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
        {
            return readonlyQuery;
        }

        @Override
        IMultiDataSetArchiverDBTransaction getTransaction()
        {
            return transaction;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IDataStoreServiceInternal dataStoreService;

    private IMultiDataSetArchiverReadonlyQueryDAO readonlyQueryDAO;

    private IMultiDataSetArchiverDBTransaction transaction;

    private MockMultiDataSetUnarchivingMaintenanceTask maintenanceTask;

    private IArchiverPlugin archiverPlugin;

    private IDataSetDirectoryProvider directoryProvider;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        hierarchicalContentProvider = context.mock(IHierarchicalContentProvider.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        readonlyQueryDAO = context.mock(IMultiDataSetArchiverReadonlyQueryDAO.class);
        transaction = context.mock(IMultiDataSetArchiverDBTransaction.class);
        archiverPlugin = context.mock(IArchiverPlugin.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        maintenanceTask = new MockMultiDataSetUnarchivingMaintenanceTask();
        maintenanceTask.setService(service);
        maintenanceTask.setHierarchicalContentProvider(hierarchicalContentProvider);
        maintenanceTask.setDataStoreService(dataStoreService);
        maintenanceTask.setReadonlyQuery(readonlyQueryDAO);
        maintenanceTask.setTransaction(transaction);
        context.checking(new Expectations()
            {
                {
                    allowing(dataStoreService).getArchiverPlugin();
                    will(returnValue(archiverPlugin));
                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(directoryProvider));
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        if (result.getStatus() == ITestResult.FAILURE)
        {
            String logContent = logRecorder.getLogContent();
            fail(result.getName() + " failed. Log content:\n" + logContent);
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        MultiDataSetArchiverContainerDTO container1 = container(12);
        MultiDataSetArchiverContainerDTO container2 = container(18);
        prepareListContainersForUnarchiving(container1, container2);
        MultiDataSetArchiverDataSetDTO dataSet1 = dataSet(42);
        MultiDataSetArchiverDataSetDTO dataSet2 = dataSet(43);
        MultiDataSetArchiverDataSetDTO dataSet3 = dataSet(44);
        MultiDataSetArchiverDataSetDTO dataSet4 = dataSet(45);
        RecordingMatcher<ArchiverTaskContext> recordingMatcher1 =
                prepareForDataSetsOfContainer(container1.getId(), dataSet1, dataSet2, dataSet3);
        RecordingMatcher<ArchiverTaskContext> recordingMatcher2 =
                prepareForDataSetsOfContainer(container2.getId(), dataSet4);
        prepareTransactionCommit(2);

        maintenanceTask.execute();

        AssertionUtil.assertContainsLines(LOG_PREFIX + "Start unarchiving [DS-42, DS-43, DS-44]\n"
                + LOG_PREFIX + "Unarchiving finished for [DS-42, DS-43, DS-44]\n"
                + LOG_PREFIX + "Start unarchiving [DS-45]\n"
                + LOG_PREFIX + "Unarchiving finished for [DS-45]", logRecorder.getLogContent());
        assertExpectedArchiverTaskContext(recordingMatcher1.recordedObject());
        assertExpectedArchiverTaskContext(recordingMatcher2.recordedObject());
        context.assertIsSatisfied();
    }

    private void assertExpectedArchiverTaskContext(ArchiverTaskContext archiverTaskContext)
    {
        assertSame(directoryProvider, archiverTaskContext.getDirectoryProvider());
        assertSame(hierarchicalContentProvider, archiverTaskContext.getHierarchicalContentProvider());
        assertEquals(true, archiverTaskContext.isForceUnarchiving());

    }

    private void prepareListContainersForUnarchiving(final MultiDataSetArchiverContainerDTO... containers)
    {
        context.checking(new Expectations()
            {
                {
                    one(readonlyQueryDAO).listContainersForUnarchiving();
                    will(returnValue(Arrays.asList(containers)));

                    for (MultiDataSetArchiverContainerDTO container : containers)
                    {
                        one(transaction).resetRequestUnarchiving(container.getId());
                    }
                }
            });
    }

    private RecordingMatcher<ArchiverTaskContext> prepareForDataSetsOfContainer(final long containerId,
            final MultiDataSetArchiverDataSetDTO... dataSets)
    {
        final RecordingMatcher<ArchiverTaskContext> contextRecorder = new RecordingMatcher<ArchiverTaskContext>();
        context.checking(new Expectations()
            {
                {
                    one(readonlyQueryDAO).listDataSetsForContainerId(containerId);
                    will(returnValue(Arrays.asList(dataSets)));

                    final List<String> dataSetCodes = new ArrayList<String>();
                    List<AbstractExternalData> dataSets2 = new ArrayList<AbstractExternalData>();
                    DataStore dataStore = new DataStoreBuilder("DSS").getStore();
                    for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
                    {
                        dataSetCodes.add(dataSet.getCode());
                        DataSetBuilder builder = new DataSetBuilder(dataSet.getId()).code(dataSet.getCode())
                                .store(dataStore).fileFormat("UNKNOWN");
                        dataSets2.add(builder.getDataSet());
                    }
                    one(service).listDataSetsByCode(dataSetCodes);
                    will(returnValue(dataSets2));

                    one(archiverPlugin).unarchive(with(new BaseMatcher<List<DatasetDescription>>()
                        {
                            @SuppressWarnings("unchecked")
                            @Override
                            public boolean matches(Object obj)
                            {
                                assertEquals(dataSetCodes.toString(), DatasetDescription.extractCodes(
                                        (List<DatasetDescription>) obj).toString());
                                return true;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                                description.appendText(dataSets.toString());
                            }
                        }), with(contextRecorder));
                }
            });
        return contextRecorder;
    }

    private void prepareTransactionCommit(final int count)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(count).of(transaction).commit();
                    exactly(count).of(transaction).close();
                }
            });
    }

    private MultiDataSetArchiverContainerDTO container(long id)
    {
        MultiDataSetArchiverContainerDTO container = new MultiDataSetArchiverContainerDTO();
        container.setId(id);
        container.setUnarchivingRequested(true);
        return container;
    }

    private MultiDataSetArchiverDataSetDTO dataSet(long id)
    {
        return new MultiDataSetArchiverDataSetDTO(id, "DS-" + id, 1, id * 100);
    }

}
