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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.common.multiplexer.ThreadPoolMultiplexer;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningHolder;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningMultiplexer;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningBatchHandler;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningFactory;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningMultiplexerTest extends AssertJUnit
{

    private Mockery context;

    private String dataStore1Url;

    private String dataStore2Url;

    private IDssServiceRpcScreening dataStore1Service;

    private IDssServiceRpcScreening dataStore2Service;

    private DssServiceRpcScreeningHolder dataStore1Holder;

    private DssServiceRpcScreeningHolder dataStore2Holder;

    private IDatasetIdentifier dataStore1DataSet1;

    private IDatasetIdentifier dataStore1DataSet2;

    private IDatasetIdentifier dataStore2DataSet1;

    private IDssServiceRpcScreeningFactory serviceFactory;

    private IDssServiceRpcScreeningBatchHandler<IDatasetIdentifier, String> batchHandler;

    private MessageChannel channel1;

    private MessageChannel channel2;

    private IMultiplexer multiplexer;

    private DssServiceRpcScreeningMultiplexer dssMultiplexer;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();

        dataStore1Url = "datastore1:1111";
        dataStore2Url = "datastore2:2222";

        dataStore1Service = context.mock(IDssServiceRpcScreening.class, "dataStore1Service");
        dataStore2Service = context.mock(IDssServiceRpcScreening.class, "dataStore2Service");

        dataStore1Holder = new DssServiceRpcScreeningHolder(dataStore1Url, dataStore1Service);
        dataStore2Holder = new DssServiceRpcScreeningHolder(dataStore2Url, dataStore2Service);

        dataStore1DataSet1 = new DatasetIdentifier("DATA_STORE_1_DATA_SET_1", dataStore1Url);
        dataStore1DataSet2 = new DatasetIdentifier("DATA_STORE_1_DATA_SET_2", dataStore1Url);
        dataStore2DataSet1 = new DatasetIdentifier("DATA_STORE_2_DATA_SET_1", dataStore2Url);

        serviceFactory = context.mock(IDssServiceRpcScreeningFactory.class);
        batchHandler = context.mock(IDssServiceRpcScreeningBatchHandler.class);

        channel1 = new MessageChannelBuilder(1000).getChannel();
        channel2 = new MessageChannelBuilder(1000).getChannel();

        multiplexer = new ThreadPoolMultiplexer("dss-screening-multiplexer-test");
        dssMultiplexer = new DssServiceRpcScreeningMultiplexer(multiplexer, serviceFactory);
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    private List<IDatasetIdentifier> getAllDataSets()
    {
        return Arrays.asList(dataStore1DataSet1, dataStore1DataSet2, dataStore2DataSet1);
    }

    private List<IDatasetIdentifier> getDataStore1DataSets()
    {
        return Arrays.asList(dataStore1DataSet1, dataStore1DataSet2);
    }

    private List<IDatasetIdentifier> getDataStore2DataSets()
    {
        return Arrays.asList(dataStore2DataSet1);
    }

    private Action getDataStore1ResultsAction()
    {
        return new CustomAction("action")
            {

                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return Arrays.asList("DATA_STORE_1_RESULT_1", "DATA_STORE_1_RESULT_2");
                }

            };
    }

    private Action getDataStore2ResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return Arrays.asList("DATA_STORE_2_RESULT_1");
                }
            };
    }

    private Action getEmptyResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return new ArrayList<String>();
                }
            };
    }

    private Action getNullResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return null;
                }
            };
    }

    private Action getExceptionAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    throw new RuntimeException("MULTIPLEXER_EXCEPTION");
                }
            };
    }

    private Action getSleepAction(final int sleep)
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Thread.sleep(sleep);
                    return null;
                }
            };
    }

    private void assertDataStore1Results(List<String> results)
    {
        assertEquals(Arrays.asList("DATA_STORE_1_RESULT_1", "DATA_STORE_1_RESULT_2"), results);
    }

    private void assertAllResults(List<String> results)
    {
        assertEquals(Arrays.asList("DATA_STORE_1_RESULT_1", "DATA_STORE_1_RESULT_2",
                "DATA_STORE_2_RESULT_1"), results);
    }

    @Test
    public void testWithNullReferenceLists()
    {
        List<String> results = dssMultiplexer.process(null, batchHandler).getMergedBatchResultsWithDuplicates();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testWithEmptyReferenceLists()
    {
        List<String> results =
                dssMultiplexer.process(new ArrayList<IDatasetIdentifier>(), batchHandler)
                        .getMergedBatchResultsWithDuplicates();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testWithNullReferences()
    {
        List<String> results =
                test(Arrays.asList(null, dataStore1DataSet1, null, dataStore1DataSet2,
                        dataStore2DataSet1), getDataStore1DataSets(), getDataStore2DataSets(),
                        getDataStore1ResultsAction(), getDataStore2ResultsAction());
        assertAllResults(results);
    }

    @Test
    public void testWithNotEmptyResults()
    {
        List<String> results =
                test(getAllDataSets(), getDataStore1DataSets(), getDataStore2DataSets(),
                        getDataStore1ResultsAction(), getDataStore2ResultsAction());
        assertAllResults(results);
    }

    @Test
    public void testWithEmptyResult()
    {
        List<String> results =
                test(getAllDataSets(), getDataStore1DataSets(), getDataStore2DataSets(),
                        getDataStore1ResultsAction(), getEmptyResultsAction());
        assertDataStore1Results(results);
    }

    @Test
    public void testWithNullResult()
    {
        List<String> results =
                test(getAllDataSets(), getDataStore1DataSets(), getDataStore2DataSets(),
                        getDataStore1ResultsAction(), getNullResultsAction());
        assertDataStore1Results(results);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "MULTIPLEXER_EXCEPTION")
    public void testWithException()
    {
        long startTime = System.currentTimeMillis();
        try
        {
            test(getAllDataSets(), getDataStore1DataSets(), getDataStore2DataSets(),
                    getExceptionAction(), getSleepAction(500));
        } finally
        {
            long endTime = System.currentTimeMillis();
            assertTrue(endTime - startTime < 500);
        }
    }

    private List<String> test(final List<IDatasetIdentifier> allDatasets,
            final List<IDatasetIdentifier> dataStore1Datasets,
            final List<IDatasetIdentifier> dataStore2Datasets, final Action dataStore1Action,
            final Action dataStore2Action)
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceFactory).createDssService(dataStore1Url);
                    will(returnValue(dataStore1Holder));

                    one(serviceFactory).createDssService(dataStore2Url);
                    will(returnValue(dataStore2Holder));

                    one(batchHandler).handle(dataStore1Holder, dataStore1Datasets);
                    will(new Action()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                channel1.send("DATA_STORE_1_BATCH_START");
                                channel2.assertNextMessage("DATA_STORE_2_BATCH_START");
                                return dataStore1Action.invoke(invocation);
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        });

                    one(batchHandler).handle(dataStore2Holder, dataStore2Datasets);
                    will(new Action()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                channel2.send("DATA_STORE_2_BATCH_START");
                                channel1.assertNextMessage("DATA_STORE_1_BATCH_START");
                                return dataStore2Action.invoke(invocation);
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        });
                }
            });

        return dssMultiplexer.process(allDatasets, batchHandler).getMergedBatchResultsWithDuplicates();
    }

}
