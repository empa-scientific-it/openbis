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

package eu.basysbio.cisd.dss;

import static eu.basysbio.cisd.dss.TimeSeriesAndTimePointDataSetHandler.HELPDESK_EMAIL;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsArray;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=TimeSeriesAndTimePointDataSetHandler.class)
public class TimeSeriesAndTimePointDataSetHandlerTest extends AbstractFileSystemTestCase
{
    private static final class StringMatcher extends BaseMatcher<String>
    {
        private final String expectedString;

        StringMatcher(String expectedString)
        {
            this.expectedString = expectedString;
        }

        public boolean matches(Object item)
        {
            if (item instanceof String)
            {
                String string = (String) item;
                assertEquals(expectedString, string);
                return true;
            }
            return false;
        }

        public void describeTo(Description description)
        {
            description.appendText(expectedString);
        }
    }
    
    private Mockery context;
    private IDataSetHandler delegator;
    private IDataSetHandler timePointDataSetHandler;
    private IMailClient mailClient;
    private IDataSetHandler handler;
    private File dropBox;
    private File timePointFolder;
    private ITimeProvider timeProvider;
    private IEncapsulatedOpenBISService service;
    private Sequence sequence;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        LogInitializer.init();
        context = new Mockery();
        sequence = context.sequence("sequence");
        delegator = context.mock(IDataSetHandler.class, "delegator");
        service = context.mock(IEncapsulatedOpenBISService.class);
        timePointDataSetHandler = context.mock(IDataSetHandler.class, "timePointDataSetHandler");
        mailClient = context.mock(IMailClient.class);
        timeProvider = context.mock(ITimeProvider.class);
        dropBox = new File(workingDirectory, "drop-box");
        timePointFolder = new File(workingDirectory, "time-point-folder");
        timePointFolder.mkdirs();
        handler =
                new TimeSeriesAndTimePointDataSetHandler(delegator, service, mailClient,
                        timePointDataSetHandler, timePointFolder, timeProvider);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testFailingTimeSeriesRegistration()
    {
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList()));
                }
            });
        
        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);
        
        assertEquals(0, dataSets.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testHandleSuccesfullyTwoTimePointDataSets() throws IOException
    {
        final File tp1 = new File(timePointFolder, "tp1");
        tp1.createNewFile();
        final DataSetInformation ds1 = new DataSetInformation();
        final File tp2 = new File(timePointFolder, "tp2");
        tp2.createNewFile();
        final DataSetInformation ds2 = new DataSetInformation();
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        dataSetInformation.setDataSetType(new DataSetType(DataSetHandler.TIME_SERIES));
        String expectedSubject = "BaSysBio: Successful uploading of data set 'drop-box'";
        String expectedMessage =
                "The data set 'drop-box' "
                        + "has been successfully uploaded and registered in openBIS.";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, false);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));

                    one(timePointDataSetHandler).handleDataSet(tp1);
                    will(returnValue(Arrays.asList(ds1)));

                    one(timePointDataSetHandler).handleDataSet(tp2);
                    will(returnValue(Arrays.asList(ds2)));
                }
            });

        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);

        assertEquals(3, dataSets.size());
        assertSame(dataSetInformation, dataSets.get(0));
        assertSame(ds1, dataSets.get(1));
        assertSame(ds2, dataSets.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testHandleNotSuccesfullyTwoTimePointDataSets() throws IOException
    {
        final File tp1 = new File(timePointFolder, "tp1");
        tp1.createNewFile();
        final File tp2 = new File(timePointFolder, "tp2");
        tp2.createNewFile();
        final DataSetInformation ds2 = new DataSetInformation();
        ds2.setDataSetCode("ds2");
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode("ds1");
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        dataSetInformation.setDataSetType(new DataSetType(DataSetHandler.TIME_SERIES));
        String expectedSubject = "BaSysBio: Failed uploading of data set 'drop-box'";
        String expectedMessage =
                "Uploading of data set 'drop-box' failed "
                        + "because 1 of 2 time point data sets couldn't be registered.\n\n"
                        + "Please, contact the help desk for support: " + HELPDESK_EMAIL + "\n"
                        + "(Time stamp of failure: 1970-01-01 01:01:14 +0100)";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, true);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));

                    one(timePointDataSetHandler).handleDataSet(tp1);
                    will(returnValue(Arrays.asList()));

                    one(timePointDataSetHandler).handleDataSet(tp2);
                    will(returnValue(Arrays.asList(ds2)));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(42 * 42 * 42L));
                    
                    one(service).deleteDataSet("ds2", "Rollback registration");
                    inSequence(sequence);
                    one(service).deleteDataSet("ds1", "Rollback registration");
                    inSequence(sequence);
                }
            });

        try
        {
            handler.handleDataSet(dropBox);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Not all data sets could be registered. "
                    + "For more details see error messages in the log.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testHandleNotSuccesfullyTwoLcaMicTimeSeriesDataSets() throws IOException
    {
        final File tp1 = new File(workingDirectory, DataSetHandler.LCA_MIC_TIME_SERIES + "1");
        tp1.createNewFile();
        final File tp2 = new File(workingDirectory, DataSetHandler.LCA_MIC_TIME_SERIES + "2");
        tp2.createNewFile();
        final DataSetInformation ds2 = new DataSetInformation();
        ds2.setDataSetCode("ds2");
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode("ds1");
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        dataSetInformation.setDataSetType(new DataSetType(DataSetHandler.LCA_MIC));
        String expectedSubject = "BaSysBio: Failed uploading of data set 'drop-box'";
        String expectedMessage =
            "Uploading of data set 'drop-box' failed "
            + "because 1 of 2 LCA MIC time series data sets couldn't be registered.\n\n"
            + "Please, contact the help desk for support: " + HELPDESK_EMAIL + "\n"
                        + "(Time stamp of failure: 1970-01-01 01:01:14 +0100)";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, true);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));

                    one(delegator).handleDataSet(tp1);
                    will(returnValue(Arrays.asList()));

                    one(delegator).handleDataSet(tp2);
                    will(returnValue(Arrays.asList(ds2)));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(42 * 42 * 42L));
                    
                    one(service).deleteDataSet("ds2", "Rollback registration");
                    inSequence(sequence);
                    one(service).deleteDataSet("ds1", "Rollback registration");
                    inSequence(sequence);
                }
            });

        try
        {
            handler.handleDataSet(dropBox);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Not all data sets could be registered. "
                    + "For more details see error messages in the log.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHandleLcaMicTimeSeriesDataSet() throws IOException
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setUploadingUserEmail("john.doe@abc.de");
        dataSetInformation.setDataSetType(new DataSetType(DataSetHandler.LCA_MIC));
        String expectedSubject = "BaSysBio: Successful uploading of data set 'drop-box'";
        String expectedMessage =
                "The data set 'drop-box' "
                        + "has been successfully uploaded and registered in openBIS.";
        prepareSendingEMail(expectedSubject, expectedMessage, dataSetInformation, false);
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dropBox);
                    will(returnValue(Arrays.asList(dataSetInformation)));
                }
            });

        List<DataSetInformation> dataSets = handler.handleDataSet(dropBox);

        assertEquals(1, dataSets.size());
        assertSame(dataSetInformation, dataSets.get(0));
        context.assertIsSatisfied();
    }
    
    private void prepareSendingEMail(final String expectedSubject, final String expectedMessage,
            final DataSetInformation dataSetInformation, final boolean alsoToHelpDesk)
    {
        context.checking(new Expectations()
            {
                {
                    StringMatcher[] recipients = new StringMatcher[alsoToHelpDesk ? 2 : 1];
                    recipients[0] =
                            new StringMatcher(dataSetInformation.tryGetUploadingUserEmail());
                    if (alsoToHelpDesk)
                    {
                        recipients[1] = new StringMatcher(HELPDESK_EMAIL);
                    }
                    one(mailClient).sendMessage(string(expectedSubject), string(expectedMessage),
                            with(new IsNull<String>()), with(new IsNull<From>()),
                            with(new IsArray<String>(recipients)));
                }

                private String string(String expectedString)
                {
                    return with(new StringMatcher(expectedString));
                }
            });
    }

}
