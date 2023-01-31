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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableStringCell;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ReportingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ReportingServiceSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author Franz-Josef Elmer
 */
public class ReportingServiceTest extends AbstractFileTest
{
    private static SimpleComparator<ReportingService, String> REPORTING_SERVICE_COMPARATOR = new SimpleComparator<ReportingService, String>()
        {
            @Override
            public String evaluate(ReportingService item)
            {
                return item.getPermId().getPermId();
            }
        };

    @Test
    public void testSearchReportingServiceWithId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));
        ReportingServiceSearchCriteria searchCriteria = new ReportingServiceSearchCriteria();
        searchCriteria.withId().thatEquals(id);
        ReportingServiceFetchOptions fetchOptions = new ReportingServiceFetchOptions();

        // When
        List<ReportingService> services = as.searchReportingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals("Test Jython Reporting", services.get(0).getLabel());
        assertEquals(id.toString(), services.get(0).getPermId().toString());
        assertEquals(fetchOptions.toString(), services.get(0).getFetchOptions().toString());
        assertEquals(1, services.size());

        as.logout(sessionToken);
    }

    @Test
    public void testSearchReportingServiceWithName()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ReportingServiceSearchCriteria searchCriteria = new ReportingServiceSearchCriteria();
        searchCriteria.withName().thatContains("view");
        ReportingServiceFetchOptions fetchOptions = new ReportingServiceFetchOptions();

        // When
        List<ReportingService> services = as.searchReportingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        services.sort(REPORTING_SERVICE_COMPARATOR);
        assertEquals("[csv-viewer, tsv-viewer]",
                services.stream().map(s -> s.getPermId().getPermId()).collect(Collectors.toList()).toString());
        assertEquals("[csv-viewer, tsv-viewer]",
                services.stream().map(s -> s.getName()).collect(Collectors.toList()).toString());
        assertEquals("[CSV View, TSV View]",
                services.stream().map(s -> s.getLabel()).collect(Collectors.toList()).toString());
        assertEquals("[[], []]",
                services.stream().map(s -> s.getDataSetTypeCodes()).collect(Collectors.toList()).toString());

        as.logout(sessionToken);
    }

    @Test
    public void testSearchReportingService() throws ParseException
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ReportingServiceSearchCriteria searchCriteria = new ReportingServiceSearchCriteria();
        ReportingServiceFetchOptions fetchOptions = new ReportingServiceFetchOptions();
        DataSetTypeSearchCriteria typeSearchCriteria = new DataSetTypeSearchCriteria();
        List<String> allDataSetTypeCodes = as.searchDataSetTypes(sessionToken, typeSearchCriteria,
                new DataSetTypeFetchOptions()).getObjects().stream()
                .map(t -> t.getCode())
                .collect(Collectors.toList());
        Collections.sort(allDataSetTypeCodes);

        // When
        List<ReportingService> services = as.searchReportingServices(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        services.sort(REPORTING_SERVICE_COMPARATOR);
        assertEquals("[csv-viewer, demo-reporter, example-jython-report, tsv-viewer]",
                services.stream().map(s -> s.getPermId().getPermId()).collect(Collectors.toList()).toString());
        assertEquals("[csv-viewer, demo-reporter, example-jython-report, tsv-viewer]",
                services.stream().map(s -> s.getName()).collect(Collectors.toList()).toString());
        assertEquals("[CSV View, Show Dataset Size, Test Jython Reporting, TSV View]",
                services.stream().map(s -> s.getLabel()).collect(Collectors.toList()).toString());
        assertEquals("[]", services.get(0).getDataSetTypeCodes().toString());
        assertEquals("[]", services.get(3).getDataSetTypeCodes().toString());
        assertDataSetTypesOfReportingService(allDataSetTypeCodes, services.get(1));
        assertDataSetTypesOfReportingService(allDataSetTypeCodes, services.get(2));

        as.logout(sessionToken);
    }

    private void assertDataSetTypesOfReportingService(List<String> allDataSetTypeCodes, ReportingService reportingService)
    {
        TreeSet<String> dataSetCodes = new TreeSet<>(reportingService.getDataSetTypeCodes());
        assertEquals(false, dataSetCodes.isEmpty());
        dataSetCodes.removeAll(allDataSetTypeCodes);
        assertEquals("[]", dataSetCodes.toString());
    }

    @Test
    public void testExecuteReportingServiceWithSpecifiedDataStore() throws Exception
    {
        // Given
        String dataSetCode = registerDataSet();
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets(dataSetCode);

        // When
        TableModel tableModel = as.executeReportingService(sessionToken, id, options);

        // Then
        assertEquals("[Data Set, Data Set Type]", tableModel.getColumns().toString());
        assertEquals("[[" + dataSetCode + ", UNKNOWN]]", tableModel.getRows().toString());
        assertEquals(tableModel.getRows().get(0).get(0).getClass(), TableStringCell.class);
        assertEquals(tableModel.getRows().get(0).get(1).getClass(), TableStringCell.class);

        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingService() throws Exception
    {
        // Given
        String dataSetCode = registerDataSet();
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report");
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets(dataSetCode);
        
        // When
        TableModel tableModel = as.executeReportingService(sessionToken, id, options);
        
        // Then
        assertEquals("[Data Set, Data Set Type]", tableModel.getColumns().toString());
        assertEquals("[[" + dataSetCode + ", UNKNOWN]]", tableModel.getRows().toString());
        assertEquals(tableModel.getRows().get(0).get(0).getClass(), TableStringCell.class);
        assertEquals(tableModel.getRows().get(0).get(1).getClass(), TableStringCell.class);
        
        as.logout(sessionToken);
    }
    
    @Test
    public void testExecuteUnknownReportingService() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("unknown service", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store 'STANDARD' does not have 'unknown service' reporting plugin configured.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteProcessingService() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("demo processor", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store 'STANDARD' does not have 'demo processor' reporting plugin configured.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceOfUnknownDataStore() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("UNKNOWN"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets("abc");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Cannot find the data store UNKNOWN");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithAuthorizationFailure() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_SPACE_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();
        options.withDataSets("20081105092159188-3");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "User '" + TEST_SPACE_USER + "' does not have enough privileges.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithUnspecifiedOptions() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, null);
                }
            },
                // Then
                "Options cannot be null.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithNoDataSets() throws Exception
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DssServicePermId id = new DssServicePermId("example-jython-report", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "No data set codes specified.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithMissingServiceId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, null, options);
                }
            },
                // Then
                "Service id cannot be null.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithUnknownServiceIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new MyId();
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown service id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithEmptyPermId()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("", new DataStorePermId("STANDARD"));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Service key cannot be empty.");
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithUnknownDataStoreIdType()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new MyId());
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Unknown data store id type: " + MyId.class.getName());
        as.logout(sessionToken);
    }

    @Test
    public void testExecuteReportingServiceWithEmptyDataStoreCode()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        IDssServiceId id = new DssServicePermId("key", new DataStorePermId(""));
        ReportingServiceExecutionOptions options = new ReportingServiceExecutionOptions();

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    as.executeReportingService(sessionToken, id, options);
                }
            },
                // Then
                "Data store code cannot be empty.");
        as.logout(sessionToken);
    }

    private static final class MyId implements IDssServiceId, IDataStoreId
    {
        private static final long serialVersionUID = 1L;
    }

}
