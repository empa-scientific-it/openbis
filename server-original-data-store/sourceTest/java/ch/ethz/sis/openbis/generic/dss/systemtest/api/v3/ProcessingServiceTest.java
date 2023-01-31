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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ProcessingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ProcessingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ProcessingServiceSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.ILogMonitoringStopCondition;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.ParsedLogEntry;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * @author Franz-Josef Elmer
 */
public class ProcessingServiceTest extends AbstractFileTest
{
    private static SimpleComparator<ProcessingService, String> PROCESSING_SERVICE_COMPARATOR = new SimpleComparator<ProcessingService, String>()
        {
            @Override
            public String evaluate(ProcessingService item)
            {
                return item.getPermId().getPermId();
            }
        };

    private static final File EMAIL_FOLDER = new File("targets/email");

    @BeforeClass
    public void deleteEMails()
    {
        FileUtilities.deleteRecursively(EMAIL_FOLDER);
    }

    @Test
    public void testSearchProcessingServiceWithId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));
        ProcessingServiceSearchCriteria searchCriteria = new ProcessingServiceSearchCriteria();
        searchCriteria.withId().thatEquals(id);
        ProcessingServiceFetchOptions fetchOptions = new ProcessingServiceFetchOptions();

        // When
        List<ProcessingService> services = as.searchProcessingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals("Demo Processing", services.get(0).getLabel());
        assertEquals(id.toString(), services.get(0).getPermId().toString());
        assertEquals(fetchOptions.toString(), services.get(0).getFetchOptions().toString());
        assertEquals(1, services.size());

        as.logout(sessionToken);
    }

    @Test
    public void testSearchProcessingServiceWithName()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ProcessingServiceSearchCriteria searchCriteria = new ProcessingServiceSearchCriteria();
        searchCriteria.withName().thatContains("process");
        ProcessingServiceFetchOptions fetchOptions = new ProcessingServiceFetchOptions();

        // When
        List<ProcessingService> services = as.searchProcessingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        services.sort(PROCESSING_SERVICE_COMPARATOR);
        assertEquals("[demo-processor]",
                services.stream().map(s -> s.getPermId().getPermId()).collect(Collectors.toList()).toString());
        assertEquals("[demo-processor]",
                services.stream().map(s -> s.getName()).collect(Collectors.toList()).toString());
        assertEquals("[Demo Processing]",
                services.stream().map(s -> s.getLabel()).collect(Collectors.toList()).toString());
        assertEquals("[CONTAINER_TYPE, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]", getSortedDataSetTypeCodes(services.get(0)).toString());

        as.logout(sessionToken);
    }

    @Test
    public void testSearchProcessingService()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ProcessingServiceSearchCriteria searchCriteria = new ProcessingServiceSearchCriteria();
        ProcessingServiceFetchOptions fetchOptions = new ProcessingServiceFetchOptions();

        // When
        List<ProcessingService> services = as.searchProcessingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        services.sort(PROCESSING_SERVICE_COMPARATOR);
        assertEquals("[demo-processor]",
                services.stream().map(s -> s.getPermId().getPermId()).collect(Collectors.toList()).toString());
        assertEquals("[demo-processor]",
                services.stream().map(s -> s.getName()).collect(Collectors.toList()).toString());
        assertEquals("[Demo Processing]",
                services.stream().map(s -> s.getLabel()).collect(Collectors.toList()).toString());
        assertEquals("[CONTAINER_TYPE, HCS_IMAGE, HCS_IMAGE_ANALYSIS_DATA]", getSortedDataSetTypeCodes(services.get(0)).toString());

        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithSpecifiedDataStore() throws Exception
    {
        // Given
        String dataSetCode = registerDataSet();
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets(dataSetCode).withParameter("greetings", "hello world");

        // When
        as.executeProcessingService(sessionToken, id, options);

        // Then
        List<String> log = new ArrayList<>();
        waitUntil(new ILogMonitoringStopCondition()
            {
                @Override
                public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
                {
                    String logMessage = logEntry.getLogMessage();
                    if (logMessage.contains("DemoProcessingPlugin"))
                    {
                        log.add(logMessage.replace(dataSetCode, "<data set code>"));
                    }
                    return logMessage.contains("Processing done.");
                }
            }, 10);
        Collections.sort(log);
        assertEquals("OPERATION.DemoProcessingPlugin - Parameter: greetings=hello world\n"
                + "OPERATION.DemoProcessingPlugin - Parameter: user=test\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/file1.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/file2.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/file3.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/file4.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/subdir2\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/subdir2/file5.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir3\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir3/file6.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing done.\n"
                + "OPERATION.DemoProcessingPlugin - Processing of the following datasets has been requested: [Dataset '<data set code>']",
                String.join("\n", log));
        File[] emails = EMAIL_FOLDER.listFiles();
        if (emails == null)
        {
            fail("Empty e-mails folder: " + EMAIL_FOLDER.getAbsolutePath());
        }
        File emailFile = Collections.max(new ArrayList<>(Arrays.asList(emails)));
        String email = FileUtilities.loadToString(emailFile);
        assertContains("'Demo Processing' [demo-processor] processing finished on 1 data set(s):", email);
        assertContains(dataSetCode, email);
        PersonPermId personPermId = new PersonPermId(TEST_USER);
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        String emailAddress = as.getPersons(sessionToken, Arrays.asList(personPermId), fetchOptions).get(personPermId).getEmail();
        assertContains("To: " + emailAddress, email);
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingService() throws Exception
    {
        // Given
        String dataSetCode = registerDataSet();
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor");
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets(dataSetCode).withParameter("greetings", "hello world");
        
        // When
        as.executeProcessingService(sessionToken, id, options);
        
        // Then
        List<String> log = new ArrayList<>();
        waitUntil(new ILogMonitoringStopCondition()
        {
            @Override
            public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
            {
                String logMessage = logEntry.getLogMessage();
                if (logMessage.contains("DemoProcessingPlugin"))
                {
                    log.add(logMessage.replace(dataSetCode, "<data set code>"));
                }
                return logMessage.contains("Processing done.");
            }
        }, 10);
        Collections.sort(log);
        assertEquals("OPERATION.DemoProcessingPlugin - Parameter: greetings=hello world\n"
                + "OPERATION.DemoProcessingPlugin - Parameter: user=test\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/file1.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/file2.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/file3.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/file4.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/subdir2\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir1/subdir2/file5.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir3\n"
                + "OPERATION.DemoProcessingPlugin - Processing <data set code>/original/<data set code>/subdir3/file6.txt\n"
                + "OPERATION.DemoProcessingPlugin - Processing done.\n"
                + "OPERATION.DemoProcessingPlugin - Processing of the following datasets has been requested: [Dataset '<data set code>']",
                String.join("\n", log));
        File[] emails = EMAIL_FOLDER.listFiles();
        if (emails == null)
        {
            fail("Empty e-mails folder: " + EMAIL_FOLDER.getAbsolutePath());
        }
        File emailFile = Collections.max(new ArrayList<>(Arrays.asList(emails)));
        String email = FileUtilities.loadToString(emailFile);
        assertContains("'Demo Processing' [demo-processor] processing finished on 1 data set(s):", email);
        assertContains(dataSetCode, email);
        PersonPermId personPermId = new PersonPermId(TEST_USER);
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        String emailAddress = as.getPersons(sessionToken, Arrays.asList(personPermId), fetchOptions).get(personPermId).getEmail();
        assertContains("To: " + emailAddress, email);
        as.logout(sessionToken);
    }
    
    @Test
    public void testExecuteProcessingServiceUsingReservedParameter() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("abc").withParameter(Constants.USER_PARAMETER, "testing");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Couldn't execute processing plugin 'demo-processor' because parameter 'user' has already be bound to "
                        + "the value 'testing'. Note, that parameter 'user' is a reserved parameter used internally.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteUnknownProcessingService() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("unknown service", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store 'STANDARD' does not have 'unknown service' processing plugin configured.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingService() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store 'STANDARD' does not have 'example-jython-report' processing plugin configured.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceOfUnknownDataStore() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo processor", new DataStorePermId("UNKNOWN"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Cannot find the data store UNKNOWN");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithAuthorizationFailure() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_SPACE_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("20081105092159188-3");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "User '" + TEST_SPACE_USER + "' does not have enough privileges.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithUnspecifiedOptions() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, null);
                }
            },
                // Then
                "Options cannot be null.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithNoDataSets() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo-processor", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "No data set codes specified.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithMissingServiceId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("123");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, null, options);
                }
            },
                // Then
                "Service id cannot be null.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithUnknownServiceIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new MyId();
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("123");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown service id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithEmptyPermId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("", new DataStorePermId("STANDARD"));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("123");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Service key cannot be empty.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithUnknownDataStoreIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new MyId());
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("123");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown data store id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingServiceWithEmptyDataStoreCode()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new DataStorePermId(""));
        ProcessingServiceExecutionOptions options = new ProcessingServiceExecutionOptions();
        options.withDataSets("123");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeProcessingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store code cannot be empty.");
        as.logout(sessionToken);
    }

    private List<String> getSortedDataSetTypeCodes(ProcessingService ProcessingService)
    {
        List<String> codes = new ArrayList<>(ProcessingService.getDataSetTypeCodes());
        Collections.sort(codes);
        return codes;
    }

    private static final class MyId implements IDssServiceId, IDataStoreId
    {
        private static final long serialVersionUID = 1L;
    }

}
