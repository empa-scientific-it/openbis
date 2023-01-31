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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.AssertingLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class NotifyingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE_1 = "ds-1";

    private static final String DATA_SET_CODE_2 = "ds-2";

    private static final String DATA_SET_CODE_3 = "ds-3";

    private static final String DATA_SET_CODE_4 = "ds-4";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
    }

    @AfterMethod
    public void afterMethod()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test() throws IOException
    {
        Properties properties = new Properties();
        FileInputStream inStream =
                new FileInputStream(new File("sourceTest/java/"
                        + getClass().getName().replace('.', '/') + "-Example.properties"));
        try
        {
            properties.load(inStream);
        } finally
        {
            IOUtils.closeQuietly(inStream);
        }
        final DataSetBuilder dataSet1 = createDataset(DATA_SET_CODE_1, "ooo_XYZ_ooo", "ibrain-2");
        final DataSetBuilder dataSet2 = createDataset(DATA_SET_CODE_2, "accepted-type", "ibrain-3");
        final DataSetBuilder filteredDataSet =
                createDataset(DATA_SET_CODE_3, "filtered-type", null);
        final DataSetBuilder noPropertiesDataSet =
                createDataset(DATA_SET_CODE_4, "accepted-type", null);

        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE_1);
                    will(returnValue(dataSet1.getDataSet()));

                    one(service).tryGetDataSet(DATA_SET_CODE_2);
                    will(returnValue(dataSet2.getDataSet()));

                    one(service).tryGetDataSet(DATA_SET_CODE_3);
                    will(returnValue(filteredDataSet.getDataSet()));

                    one(service).tryGetDataSet(DATA_SET_CODE_4);
                    will(returnValue(noPropertiesDataSet.getDataSet()));
                }
            });
        AssertingLogger logger = new AssertingLogger();
        NotifyingTask notifyingTask = new NotifyingTask(properties, service, logger);

        IPostRegistrationTaskExecutor executor = execute(notifyingTask, DATA_SET_CODE_1);
        ICleanupTask cleanupTask = executor.createCleanupTask();

        assertEquals(NoCleanupTask.class, cleanupTask.getClass());
        assertEquals("storage_provider.storage.status = STORAGE_SUCCESSFUL\n"
                + "storage_provider.dataset.id = ds-1\n" + "ibrain2.dataset.id = ibrain-2",
                FileUtilities.loadExactToString(new File("targets/ibrain-ibrain-2.txt")));

        execute(notifyingTask, DATA_SET_CODE_2);
        assertTrue("no confirnation file for " + DATA_SET_CODE_2, new File(
                "targets/ibrain-ibrain-3.txt").isFile());

        execute(notifyingTask, DATA_SET_CODE_3);
        assertFalse("confirmation file for " + DATA_SET_CODE_3 + " should not be created!",
                new File("targets/ibrain-ibrain-4.txt").exists());

        execute(notifyingTask, DATA_SET_CODE_4);
        logger.assertNumberOfMessage(1);
        logger.assertEq(
                0,
                LogLevel.WARN,
                "Could not produce post registration confirmation file for dataset 'ds-4': Property 'ibrain-data-set-id' is not set.");

        context.assertIsSatisfied();
    }

    private IPostRegistrationTaskExecutor execute(NotifyingTask notifyingTask, String datasetCode)
    {
        IPostRegistrationTaskExecutor executor = notifyingTask.createExecutor(datasetCode, false);
        executor.execute();
        return executor;
    }

    private DataSetBuilder createDataset(String code, String type, String propertyOrNull)
    {
        final DataSetBuilder dataSet = new DataSetBuilder().code(code).type(type);
        if (propertyOrNull != null)
        {
            dataSet.property("IBRAIN-DATA-SET-ID", propertyOrNull);
        }
        return dataSet;
    }
}
