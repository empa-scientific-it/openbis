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

package ch.systemsx.cisd.etlserver;

import java.util.Arrays;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.etlserver.DispatcherStorageProcessor.IDispatchableStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Test of {@link DispatcherStorageProcessor}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = DispatcherStorageProcessor.class)
public class DispatcherStorageProcessorTest extends AssertJUnit
{
    private Mockery context;

    private IDispatchableStorageProcessor dummyA, dummyB;

    private IStorageProcessorTransaction transactionA, transactionB;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dummyA = context.mock(IDispatchableStorageProcessor.class, "mock A");
        transactionA = context.mock(IStorageProcessorTransaction.class, "transaction A");
        dummyB = context.mock(IDispatchableStorageProcessor.class, "mock B");
        transactionB = context.mock(IStorageProcessorTransaction.class, "transaction B");
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testDispatching()
    {
        DispatcherStorageProcessor dispatcher =
                new DispatcherStorageProcessor(Arrays.asList(dummyA, dummyB), new Properties());

        final DataSetInformation dataset1 = createDatasetInfo("a");
        final DataSetInformation dataset2 = createDatasetInfo("b");
        context.checking(new Expectations()
            {
                {
                    one(dummyA).accepts(dataset1, null);
                    will(returnValue(false));

                    one(dummyB).accepts(dataset1, null);
                    will(returnValue(true));

                    one(dummyB).createTransaction(
                            with(any(StorageProcessorTransactionParameters.class)));
                    will(returnValue(transactionB));

                    store(one(transactionB), dataset1);
                    one(transactionB).commit();

                    one(dummyA).accepts(dataset2, null);
                    will(returnValue(true));

                    one(dummyA).createTransaction(
                            with(any(StorageProcessorTransactionParameters.class)));
                    will(returnValue(transactionA));
                    store(one(transactionA), dataset2);

                    one(transactionA).rollback(with(any(Throwable.class)));
                }
            });

        storeTransactionally(dispatcher, dataset1).commit();
        storeTransactionally(dispatcher, dataset2).rollback(new Throwable());
    }

    private void store(IStorageProcessorTransaction transaction,
            final DataSetInformation dataset)
    {
        transaction.storeData(null, null, null);
    }

    private IStorageProcessorTransaction storeTransactionally(
            IStorageProcessorTransactional storageProcessor, final DataSetInformation dataset)
    {
        final StorageProcessorTransactionParameters parameters =
                new StorageProcessorTransactionParameters(dataset, null, null);
        IStorageProcessorTransaction transaction = storageProcessor.createTransaction(parameters);
        transaction.storeData(null, null, null);
        return transaction;
    }

    private static DataSetInformation createDatasetInfo(String datasetCode)
    {
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(datasetCode);
        return dataSetInformation;
    }
}
