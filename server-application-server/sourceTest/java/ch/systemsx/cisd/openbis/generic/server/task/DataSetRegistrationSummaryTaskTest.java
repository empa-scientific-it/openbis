/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import static ch.systemsx.cisd.openbis.generic.server.task.DataSetRegistrationSummaryTask.DAYS_OF_MONTH_KEY;
import static ch.systemsx.cisd.openbis.generic.server.task.DataSetRegistrationSummaryTask.DAYS_OF_WEEK_KEY;
import static ch.systemsx.cisd.openbis.generic.server.task.DataSetRegistrationSummaryTask.SHOWN_DATA_SET_PROPERTIES_KEY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationSummaryTaskTest extends AssertJUnit
{
    private static final class EMailContentChecker
    {
        private final String expectedSubject;

        private final String expectedReport;

        private final int numberOfEMails;

        private final RecordingMatcher<String> subjectMatcher = new RecordingMatcher<String>();

        private final RecordingMatcher<String> reportMatcher = new RecordingMatcher<String>();

        EMailContentChecker(String expectedSubject, String expectedReport, int numberOfEMails)
        {
            this.expectedSubject = expectedSubject;
            this.expectedReport = expectedReport;
            this.numberOfEMails = numberOfEMails;
        }

        RecordingMatcher<String> getSubjectMatcher()
        {
            return subjectMatcher;
        }

        RecordingMatcher<String> getReportMatcher()
        {
            return reportMatcher;
        }

        void assertContent()
        {
            List<String> recordedObjects = subjectMatcher.getRecordedObjects();
            for (int i = 0; i < recordedObjects.size(); i++)
            {
                assertEquals("subject #" + (i + 1), expectedSubject, recordedObjects.get(i));
            }
            assertEquals(numberOfEMails, recordedObjects.size());
            recordedObjects = reportMatcher.getRecordedObjects();
            for (int i = 0; i < recordedObjects.size(); i++)
            {
                assertEquals("report #" + (i + 1), expectedReport, recordedObjects.get(i));
            }
            assertEquals(numberOfEMails, recordedObjects.size());
        }
    }

    private static final String SESSION_TOKEN = "session-token-123";

    private BufferedAppender logRecorder;

    private Mockery context;

    private MockTimeProvider timeProvider;

    private ICommonServerForInternalUse server;

    private IMailClient mailClient;

    private long nowDate;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder();
        context = new Mockery();
        server = context.mock(ICommonServerForInternalUse.class);
        mailClient = context.mock(IMailClient.class);
        Calendar instance = Calendar.getInstance();
        instance.set(2012, 0, 4, 10, 34, 10); // Wednesday, January 4th, 2012
        nowDate = instance.getTimeInMillis();
        timeProvider = new MockTimeProvider(nowDate, 1000);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingEMailAddresses()
    {
        try
        {
            createTask(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'email-addresses' not found in properties '[]'",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testIsNoTheDayForReporting()
    {
        Properties properties = new Properties();
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a@bc.de");

        createTask(properties).execute();

        assertEquals("Task my-task initialized.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testNoDataSetTypes()
    {
        Properties properties = new Properties();
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a@bc.de, x@y.z");
        properties.setProperty(DAYS_OF_WEEK_KEY, "2, 4");
        properties.setProperty(SHOWN_DATA_SET_PROPERTIES_KEY, "alpha, beta");
        prepareForTryToAthenticateAsSystem();
        prepareForListDataSetTypes();
        EMailContentChecker checker =
                prepareForSendingEMail("New data sets registered between "
                        + "2012-01-02 and 2012-01-04", "Dear user\n" + "\n"
                        + "This report summarizes data sets registered in openBIS between "
                        + "2012-01-02 and 2012-01-04.\n\n" + "The data sets are grouped by type.\n"
                        + "\n" + "\n" + "\n" + "Regards,\n" + "openBIS", "a@bc.de", "x@y.z");

        createTask(properties).execute();

        checker.assertContent();
        assertEquals("Task my-task initialized.\n"
                + "Data set registration report for period from "
                + "2012-01-02 until 2012-01-04 created and sent.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testReportWithNoProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a@bc.de");
        properties.setProperty(DAYS_OF_WEEK_KEY, "4");
        prepareForTryToAthenticateAsSystem();
        prepareForListDataSetTypes("MY-TYPE", "YOUR-TYPE");
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").property("BETA", "456").property("ALPHA", "123")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds2 =
                new DataSetBuilder().code("ds2").property("GAMMA", "4").property("ALPHA", "42")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds3 =
                new DataSetBuilder().code("ds3")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds4 =
                new DataSetBuilder().code("ds4")
                        .registrationDate(new Date(nowDate))
                        .getDataSet();
        PhysicalDataSet ds5 =
                new DataSetBuilder().code("ds5").registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY * 10)).getDataSet();
        RecordingMatcher<DetailedSearchCriteria> myTypeCriteriaMatcher =
                prepareForSearchForDataSets("MY-TYPE", ds2, ds3, ds5, ds1, ds4);
        PhysicalDataSet ds6 =
                new DataSetBuilder().code("ds6")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds7 =
                new DataSetBuilder().code("ds7")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds8 =
                new DataSetBuilder().code("ds8")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds9 =
                new DataSetBuilder().code("ds9")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds10 =
                new DataSetBuilder().code("ds10")
                        .registrationDate(new Date(nowDate - DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        RecordingMatcher<DetailedSearchCriteria> yourTypeCriteriaMatcher =
                prepareForSearchForDataSets("YOUR-TYPE", ds6, ds7, ds8, ds9, ds10);
        EMailContentChecker checker =
                prepareForSendingEMail("New data sets registered between "
                        + "2012-01-01 and 2012-01-04", "Dear user\n\n"
                        + "This report summarizes data sets registered in openBIS between "
                        + "2012-01-01 and 2012-01-04.\n\n" + "The data sets are grouped by type.\n\n"
                        + "MY-TYPE: Total number: 5. Number of new data sets: 4\n"
                        + "\tds1, ds2, ds3, ds4\n"
                        + "YOUR-TYPE: Total number: 5. Number of new data sets: 5\n"
                        + "\tds10, ds6, ds7, ds8, \n" + "\tds9\n\n\n" + "Regards,\n" + "openBIS",
                        "a@bc.de");

        createTask(properties).execute();

        assertTypeCriteriaMatcher("MY-TYPE", myTypeCriteriaMatcher);
        assertTypeCriteriaMatcher("YOUR-TYPE", yourTypeCriteriaMatcher);
        checker.assertContent();
        assertEquals("Task my-task initialized.\n"
                + "Data set registration report for period from "
                + "2012-01-01 until 2012-01-04 created and sent.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testReportWithProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a@bc.de");
        properties.setProperty(DAYS_OF_MONTH_KEY, "4");
        properties.setProperty(SHOWN_DATA_SET_PROPERTIES_KEY, "alpha, beta");
        prepareForTryToAthenticateAsSystem();
        prepareForListDataSetTypes("MY-TYPE", "YOUR-TYPE");
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").property("BETA", "456").property("ALPHA", "123")
                        .registrationDate(new Date(nowDate))
                        .getDataSet();
        PhysicalDataSet ds2 =
                new DataSetBuilder().code("ds2").property("GAMMA", "4").property("ALPHA", "42")
                        .registrationDate(new Date(nowDate - 20 * DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds3 =
                new DataSetBuilder().code("ds3")
                        .registrationDate(new Date(nowDate - 10 * DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds4 =
                new DataSetBuilder().code("ds4")
                        .registrationDate(new Date(nowDate - 40 * DateUtils.MILLIS_PER_DAY))
                        .getDataSet();
        PhysicalDataSet ds5 =
                new DataSetBuilder().code("ds5").registrationDate(new Date(nowDate - 50 * DateUtils.MILLIS_PER_DAY)).getDataSet();
        RecordingMatcher<DetailedSearchCriteria> myTypeCriteriaMatcher =
                prepareForSearchForDataSets("MY-TYPE", ds2, ds3, ds5, ds1, ds4);
        RecordingMatcher<DetailedSearchCriteria> yourTypeCriteriaMatcher =
                prepareForSearchForDataSets("YOUR-TYPE");
        EMailContentChecker checker =
                prepareForSendingEMail("New data sets registered between "
                        + "2011-12-04 and 2012-01-04", "Dear user\n\n"
                        + "This report summarizes data sets registered in openBIS between "
                        + "2011-12-04 and 2012-01-04.\n\n" + "The data sets are grouped by type.\n\n"
                        + "MY-TYPE: Total number: 5. Number of new data sets: 3\n"
                        + "\tds1: ALPHA = 123, BETA = 456\n" + "\tds2: ALPHA = 42\n" + "\tds3\n"
                        + "YOUR-TYPE: Total number: 0. No new data sets.\n" + "\n" + "\n"
                        + "Regards,\n" + "openBIS", "a@bc.de");

        createTask(properties).execute();

        assertTypeCriteriaMatcher("MY-TYPE", myTypeCriteriaMatcher);
        assertTypeCriteriaMatcher("YOUR-TYPE", yourTypeCriteriaMatcher);
        checker.assertContent();
        assertEquals("Task my-task initialized.\n"
                + "Data set registration report for period from "
                + "2011-12-04 until 2012-01-04 created and sent.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    private void assertTypeCriteriaMatcher(String expectedType,
            RecordingMatcher<DetailedSearchCriteria> criteriaMatcher)
    {
        assertEquals("ATTRIBUTE DATA_SET_TYPE: " + expectedType + " (with wildcards)",
                criteriaMatcher.recordedObject().toString());
    }

    private RecordingMatcher<DetailedSearchCriteria> prepareForSearchForDataSets(
            final String dataSetType, final AbstractExternalData... dataSets)
    {
        final RecordingMatcher<DetailedSearchCriteria> recordingMatcher =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(server).searchForDataSets(with(SESSION_TOKEN), with(recordingMatcher));
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
        return recordingMatcher;
    }

    private EMailContentChecker prepareForSendingEMail(final String subject, final String report,
            final String... addresses)
    {
        final EMailContentChecker checker =
                new EMailContentChecker(subject, report, addresses.length);
        context.checking(new Expectations()
            {
                {
                    for (String address : addresses)
                    {
                        one(mailClient).sendEmailMessage(with(checker.getSubjectMatcher()),
                                with(checker.getReportMatcher()), with((EMailAddress) null),
                                with((EMailAddress) null), with(new EMailAddress[]
                                { new EMailAddress(address) }));
                    }
                }
            });
        return checker;
    }

    private void prepareForListDataSetTypes(final String... dataSetTypes)
    {
        context.checking(new Expectations()
            {
                {
                    one(server).listDataSetTypes(SESSION_TOKEN);
                    List<DataSetType> types = new ArrayList<DataSetType>();
                    for (String dataSetType : dataSetTypes)
                    {
                        types.add(new DataSetType(dataSetType));
                    }
                    will(returnValue(types));
                }
            });
    }

    private void prepareForTryToAthenticateAsSystem()
    {
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));
                }
            });
    }

    private DataSetRegistrationSummaryTask createTask(Properties properties)
    {
        DataSetRegistrationSummaryTask task =
                new DataSetRegistrationSummaryTask(server, timeProvider, mailClient);
        task.setUp("my-task", properties);
        return task;
    }

}
