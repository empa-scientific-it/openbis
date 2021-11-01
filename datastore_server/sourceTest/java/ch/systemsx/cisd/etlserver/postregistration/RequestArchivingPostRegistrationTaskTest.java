/*
 * Copyright 2018 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Franz-Josef Elmer
 */
public class RequestArchivingPostRegistrationTaskTest
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IApplicationServerApi v3api;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        v3api = context.mock(IApplicationServerApi.class);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod()
    {
        if (logRecorder != null)
        {
            logRecorder.reset();
        }
        if (context != null)
        {
            // The following line of code should also be called at the end of each test method.
            // Otherwise one do not known which test failed.
            context.assertIsSatisfied();
        }
    }

    @Test
    public void testNotArchivedDataSet()
    {
        // Given
        Properties properties = new Properties();
        IPostRegistrationTask task = createTask(properties);
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<List<DataSetUpdate>>();

        final PhysicalDataSet physicalDataset = new PhysicalDataSet();
        physicalDataset.setCode("ds1");
        physicalDataset.setPresentInArchive(false);

        context.checking(new Expectations()
            {
                {

                    one(service).listDataSetsByCode(with(Collections.singletonList("ds1")));
                    will(returnValue(Collections.singletonList(physicalDataset)));
                    one(service).getSessionToken();
                    will(returnValue(SESSION_TOKEN));
                    one(v3api).updateDataSets(with(SESSION_TOKEN), with(recordedUpdates));
                }
            });

        // When
        task.createExecutor("ds1", false).execute();

        // Then
        List<DataSetUpdate> updates = recordedUpdates.recordedObject();
        DataSetUpdate update = updates.get(0);
        assertEquals(update.getDataSetId().toString(), "DS1");
        assertEquals(update.getPhysicalData().getValue().isArchivingRequested().isModified(), true);
        assertEquals(update.getPhysicalData().getValue().isArchivingRequested().getValue(), Boolean.TRUE);
        assertEquals(updates.size(), 1);
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivedDataSet()
    {
        // Given
        Properties properties = new Properties();
        IPostRegistrationTask task = createTask(properties);

        final PhysicalDataSet physicalDataset = new PhysicalDataSet();
        physicalDataset.setCode("ds1");
        physicalDataset.setPresentInArchive(true);

        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(with(Collections.singletonList("ds1")));
                    will(returnValue(Collections.singletonList(physicalDataset)));
                }
            });

        // When
        task.createExecutor("ds1", false).execute();

        // Then
        AssertionUtil.assertContains("DataSet ds1 is either in archive or archiving is requested.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchivingRequestedDataSet()
    {
        // Given
        Properties properties = new Properties();
        IPostRegistrationTask task = createTask(properties);

        final PhysicalDataSet physicalDataset = new PhysicalDataSet();
        physicalDataset.setCode("ds1");
        physicalDataset.setArchivingRequested(true);

        context.checking(new Expectations()
        {
            {
                one(service).listDataSetsByCode(with(Collections.singletonList("ds1")));
                will(returnValue(Collections.singletonList(physicalDataset)));
            }
        });

        // When
        task.createExecutor("ds1", false).execute();

        // Then
        AssertionUtil.assertContains("DataSet ds1 is either in archive or archiving is requested.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    private IPostRegistrationTask createTask(Properties properties)
    {
        return new RequestArchivingPostRegistrationTask(properties, service)
            {
                @Override
                protected IApplicationServerApi getV3api()
                {
                    return v3api;
                }
            };
    }

}
