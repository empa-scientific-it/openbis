/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ProcessDatasetsCommand.ProxyMailClient.class)
public class ProcessDatasetsCommandTest extends AssertJUnit
{
    private static final String EXAMPLE_TASK_LABEL = "My task";

    private static final String E_MAIL = "my@e.mail";

    private static final String USER_ID = "test_user";

    private static final String MESSAGE = "hello";

    private Mockery context;

    private IProcessingPluginTask task;

    private IMailClient mailClient;

    private DatasetDescription ds1;

    private DatasetDescription ds2;

    private ProcessDatasetsCommand command;

    private List<DatasetDescription> dataSets;

    private Map<String, String> parameterBindings;

    private RecordingMatcher<String> subjectRecorder;

    private RecordingMatcher<String> contentRecorder;

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        context = new Mockery();
        task = context.mock(IProcessingPluginTask.class);
        mailClient = context.mock(IMailClient.class);
        ds1 = new DatasetDescription();
        ds1.setDataSetCode("ds1");
        ds2 = new DatasetDescription();
        ds2.setDataSetCode("ds2");
        parameterBindings = new HashMap<String, String>();
        dataSets = Arrays.<DatasetDescription> asList(ds1, ds2);
        command =
                new ProcessDatasetsCommand(task, dataSets, parameterBindings, USER_ID, E_MAIL,
                        null, DatastoreServiceDescription.processing("MY_TASK", EXAMPLE_TASK_LABEL,
                                new String[0], "DSS1"), mailClient)
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        DataSetProcessingContext createDataSetProcessingContext(
                                IHierarchicalContentProvider contentProvider,
                                IDataSetDirectoryProvider dataSetDirectoryProvider, ProxyMailClient proxyMailClient)
                        {
                            return new DataSetProcessingContext(contentProvider, dataSetDirectoryProvider,
                                    parameterBindings, proxyMailClient, getUserId(), tryGetUserEmail());
                        }
                    };
        subjectRecorder = new RecordingMatcher<String>();
        contentRecorder = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    allowing(mailClient).sendMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<String>()), with(new IsNull<From>()),
                            with(new IsAnything<String[]>()));
                }
            });
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testTaskPartiallyProcessedSuccessfully()
    {
        context.checking(new Expectations()
            {
                {
                    one(task).process(with(dataSets), with(createDataSetProcessingContext(null)));
                    ProcessingStatus status = new ProcessingStatus();
                    status.addDatasetStatus(ds1, Status.OK);
                    status.addDatasetStatus(ds2, Status.createError(true, "Oops!"));
                    will(returnValue(status));
                }
            });
        command.execute(null, null);

        assertEquals("['" + EXAMPLE_TASK_LABEL + "' [MY_TASK] processing finished]", subjectRecorder
                .getRecordedObjects().toString());
        assertEquals(
                "[This is an automatically generated report from the completed processing "
                        + "of data sets in openBIS.\n"
                        + "- number of successfully processed data sets: 1. Datasets: ds1\n"
                        + "- processing of 1 data set(s) failed because:  RETRIABLE_ERROR: \"Oops!\". Datasets: ds2\n"
                        + "]", contentRecorder.getRecordedObjects().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testTaskWhichThrowsException()
    {
        context.checking(new Expectations()
            {
                {
                    one(task).process(with(dataSets), with(createDataSetProcessingContext(null)));
                    will(throwException(new IllegalStateException("illegal state!")));
                }
            });
        try
        {
            command.execute(null, null);
            fail("IllegalStateException expected.");
        } catch (IllegalStateException e)
        {
            assertEquals("illegal state!", e.getMessage());
        }

        assertEquals("['" + EXAMPLE_TASK_LABEL + "' [MY_TASK] processing failed]", subjectRecorder
                .getRecordedObjects().toString());
        assertEquals("['My task' [MY_TASK] processing failed on 2 data set(s): \n[ds1, ds2]\n\n"
                + "Error message:\nillegal state!]", contentRecorder.getRecordedObjects()
                .toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testProcessingTaskSendsEMail()
    {
        context.checking(new Expectations()
            {
                {
                    one(task)
                            .process(with(dataSets), with(createDataSetProcessingContext(MESSAGE)));
                }
            });
        command.execute(null, null);

        assertEquals("[null]", subjectRecorder.getRecordedObjects().toString());
        assertEquals("[hello]", contentRecorder.getRecordedObjects().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testProcessingTaskSendsEMailAndFails()
    {
        context.checking(new Expectations()
            {
                {
                    one(task)
                            .process(with(dataSets), with(createDataSetProcessingContext(MESSAGE)));
                    will(throwException(new RuntimeException()));
                }
            });
        try
        {
            command.execute(null, null);
            fail("RuntimeException expected");
        } catch (RuntimeException e)
        {
            assertEquals(null, e.getMessage());
        }

        assertEquals("[null, 'My task' [MY_TASK] processing failed]", subjectRecorder.getRecordedObjects()
                .toString());
        assertEquals("[hello, 'My task' [MY_TASK] processing failed on 2 data set(s): \n[ds1, ds2]\n\n"
                + "Error message:\n]", contentRecorder.getRecordedObjects().toString());
        context.assertIsSatisfied();
    }

    private BaseMatcher<DataSetProcessingContext> createDataSetProcessingContext(
            final String eMailMessageOrNull)
    {
        return new BaseMatcher<DataSetProcessingContext>()
            {
                @Override
                public boolean matches(Object item)
                {
                    DataSetProcessingContext processingContext = (DataSetProcessingContext) item;
                    assertEquals(parameterBindings, processingContext.getParameterBindings());
                    assertEquals(E_MAIL, processingContext.getUserEmailOrNull());
                    IMailClient client = processingContext.getMailClient();
                    if (eMailMessageOrNull != null)
                    {
                        client.sendMessage(null, eMailMessageOrNull, null, null);
                    }
                    return true;
                }

                @Override
                public void describeTo(Description description)
                {
                }
            };
    }
}
