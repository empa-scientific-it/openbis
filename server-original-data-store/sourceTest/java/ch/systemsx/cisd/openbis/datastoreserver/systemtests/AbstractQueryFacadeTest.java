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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Level;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * This class groups tests for aggregation, injestion services as well as other reporting services. It abstracts however the way it calls the
 * services, so that the subclasses testing the reports via particular api can be created. This class is extended by {@link QueryFacadeTest} and
 * {@link QueryApiJsonDssServiceRpcGenericTest}, one testing the {@link IQueryApiFacade} and the other {@link IDssServiceRpcGeneric}.
 * 
 * @author Jakub Straszewski
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractQueryFacadeTest extends SystemTestCase
{
    public abstract QueryTableModel createReportFromAggregationService(String serviceKey,
            Map<String, Object> parameters);

    public abstract QueryTableModel createReportFromAggregationService(String user,
            String serviceCode, Map<String, Object> parameters);

    public abstract QueryTableModel createReportFromDataSets(ReportDescription description,
            List<String> dataSetCodes);

    public abstract List<AggregationServiceDescription> listAggregationServices();

    public abstract List<ReportDescription> listTableReportDescriptions();

    public abstract IGeneralInformationService getGeneralInformationService();

    public abstract String getSessionToken();

    /**
     * Unique short identifier to create data with different ids in different implementation of this test class
     */
    public abstract String getTestId();

    @Override
    protected Level getLogLevel()
    {
        return Level.DEBUG;
    }

    public AggregationServiceDescription getAggregationServiceDescription(String key)
    {
        List<AggregationServiceDescription> services = listAggregationServices();
        for (AggregationServiceDescription aggregationServiceDescription : services)
        {
            if (aggregationServiceDescription.getServiceKey().equals(key))
            {
                return aggregationServiceDescription;
            }
        }
        throw new AssertionError("No aggregation service for key '" + key + "'.");
    }

    public ReportDescription getTableReportDescription(String key)
    {
        List<ReportDescription> services = listTableReportDescriptions();
        for (ReportDescription description : services)
        {
            if (description.getKey().equals(key))
            {
                return description;
            }
        }
        throw new AssertionError("No reporting for key '" + key + "'.");
    }

    protected List<String> getHeaders(QueryTableModel tableModel)
    {
        List<QueryTableColumn> columns = tableModel.getColumns();
        List<String> headers = new ArrayList<String>();
        for (QueryTableColumn column : columns)
        {
            String header = column.getTitle();
            headers.add(header);
        }
        return headers;
    }

    protected File getLatestEmail()
    {
        File latestFile = null;
        File[] files = new File("targets/email").listFiles();
        for (File file : files)
        {
            if (latestFile == null || latestFile.lastModified() < file.lastModified())
            {
                latestFile = file;
            }
        }
        return latestFile;
    }

    @Test
    public void testJythonReportWhichSendsEMail() throws Exception
    {
        new File(store, "1/xml/result-8").mkdirs();
        ReportDescription description = getTableReportDescription("example-jython-report");

        QueryTableModel table =
                createReportFromDataSets(description, Arrays.asList("20081105092259000-8"));

        assertEquals("[Data Set, Data Set Type]", getHeaders(table).toString());
        assertEquals("[20081105092259000-8, HCS_IMAGE]", Arrays.asList(table.getRows().get(0))
                .toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table =
                createReportFromAggregationService("example-aggregation-service", parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());
    }

    @Test
    public void testJythonAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table =
                createReportFromAggregationService("example-jython-aggregation-service-report",
                        parameters);

        assertEquals("[Key, Value]", getHeaders(table).toString());
        assertEquals("[name, world]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testDbModifyingAggregationServiceReport() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        String spaceCode = UUID.randomUUID().toString();
        parameters.put("name", "world");
        parameters.put("space", spaceCode);

        QueryTableModel table =
                createReportFromAggregationService("example-db-modifying-aggregation-service",
                        parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());

        IGeneralInformationService generalInformationService = getGeneralInformationService();

        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(
                        getSessionToken(), null);
        boolean foundSpace = false;
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            if (spaceCode.equalsIgnoreCase(space.getCode()))
            {
                foundSpace = true;
            }
        }

        assertTrue("Did not find a space called [NEWDUMMYSPACE]", foundSpace);
    }

    @Test
    public void testJythonDbModifyingAggregationServiceReport() throws Exception
    {
        BufferedAppender previousLogRecorder = logAppender;
        try
        {
            logAppender = new BufferedAppender("%d %p [%t] %c - %m%n", getLogLevel());
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            String code = UUID.randomUUID().toString();
            parameters.put("code", code);

            QueryTableModel table =
                    createReportFromAggregationService(
                            "example-jython-db-modifying-aggregation-service", parameters);

            assertEquals("[CODE, IDENTIFIER]", getHeaders(table).toString());
            assertEquals("[" + code + ", /CISD/" + code + "]", Arrays.asList(table.getRows().get(0))
                    .toString());
            assertEquals(1, table.getRows().size());

            IGeneralInformationService generalInformationService = getGeneralInformationService();

            List<Sample> samples =
                    generalInformationService.listSamplesForExperiment(getSessionToken(),
                            "/CISD/NEMO/EXP-TEST-1");
            boolean foundSample = false;
            for (Sample sample : samples)
            {
                if (code.equalsIgnoreCase(sample.getCode()))
                {
                    waitUntilDataSetImported(new ContainsCondition(SamplePE.class.getName() + ": [" + sample.getId() + "]"));
                    foundSample = true;
                }
            }

            assertTrue("Did not find a sample called " + code, foundSample);
        } catch (Exception ex)
        {
            logAppender = previousLogRecorder;
            throw ex;
        }
    }

    /**
     * The observer trying to access the forbidden dataset via the authorized content provider.
     */
    @Test(expectedExceptions = Exception.class)
    public void testJythonAggregationServiceWithContentProviderAuthentication() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        createReportFromAggregationService("observer", "content-provider-aggregation-service",
                parameters);
    }

    /**
     * The testcase, where the observer tries to acces the dataset that he cannot see, but through the non-authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithoutContentProviderAuthentication() throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
                createReportFromAggregationService("observer",
                        "content-provider-aggregation-service-no-authorization", parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    /**
     * The authorized user tries to access the dataset via the authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithContentProviderAuthenticationAndAuthorizedUser()
            throws Exception
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
                createReportFromAggregationService("content-provider-aggregation-service",
                        parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    /**
     * The testcase, that checks whether the results of the search service calls for different users
     */
    @Test
    public void testDataSetSearchServiceInAggregationService() throws Exception
    {
        waitUntilIndexUpdaterIsIdle();
        int allTest = getSearchServiceAggregationServiceResult("datasetsAll", "test");
        int allObserver = getSearchServiceAggregationServiceResult("datasetsAll", "observer");

        int filteredTest = getSearchServiceAggregationServiceResult("datasetsFiltered", "test");
        int filteredObserver =
                getSearchServiceAggregationServiceResult("datasetsFiltered", "test_space");

        assertEquals(allTest, allObserver);
        assertEquals(allTest, filteredTest);
        assertTrue("There are " + allTest
                + " datasets, and observer should see less, but he did see " + filteredObserver,
                allTest > filteredObserver);
        assertTrue("There should be some datasets visible to the observer, but there are no",
                filteredObserver > 0);
    }

    /**
     * Test searching for samples via search service for different users
     */
    @Test
    public void testSampleSearchServiceInAggregationService() throws Exception
    {
        waitUntilIndexUpdaterIsIdle();
        int allTest = getSearchServiceAggregationServiceResult("samplesAll", "test");
        int allObserver = getSearchServiceAggregationServiceResult("samplesAll", "observer");

        int filteredTest = getSearchServiceAggregationServiceResult("samplesFiltered", "test");
        int filteredObserver =
                getSearchServiceAggregationServiceResult("samplesFiltered", "observer");
        int filteredObserver2 =
                getSearchServiceAggregationServiceResult("samplesFiltered", "test_space");

        assertEquals(allTest, allObserver);
        assertEquals(allTest, filteredTest);
        assertTrue("There are " + allTest
                + " samples, and observer should see less, but he did see " + filteredObserver,
                allTest > filteredObserver);
        assertTrue("There are " + allTest
                + " samples, and test_space should see less, but he did see " + filteredObserver,
                allTest > filteredObserver2);
        assertTrue("There should be some samples visible to the observer, but there are no",
                filteredObserver > 0);
        assertTrue("There should be some samples visible to the test_space, but there are no",
                filteredObserver2 > 0);
    }

    /**
     * Check listing experiments for different users via search service
     */
    @Test
    public void testExperimentSearchServiceInAggregationService() throws Exception
    {
        waitUntilIndexUpdaterIsIdle();
        int allNemoTest =
                getSearchServiceAggregationServiceResult("experimentsAll", "test", "projectId",
                        "/CISD/NEMO");
        int allNemoObserver =
                getSearchServiceAggregationServiceResult("experimentsAll", "observer", "projectId",
                        "/CISD/NEMO");

        assertTrue(allNemoTest > 0);
        assertEquals(allNemoTest, allNemoObserver);

        int filteredNemoTest =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test",
                        "projectId", "/CISD/NEMO");
        int filteredNemoObserver =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "observer",
                        "projectId", "/CISD/NEMO");
        int filteredNemoTestSpace =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test_space",
                        "projectId", "/CISD/NEMO");

        assertEquals(allNemoTest, filteredNemoTest);
        assertEquals(0, filteredNemoObserver);
        assertEquals(0, filteredNemoTestSpace);

        int filteredTestSpace =
                getSearchServiceAggregationServiceResult("experimentsFiltered", "test_space",
                        "projectId", "/TEST-SPACE/TEST-PROJECT");

        assertTrue(filteredTestSpace > 0);
    }

    private int getSearchServiceAggregationServiceResult(String mode, String user, String... params)
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("mode", mode);

        for (int i = 0; 2 * i + 1 < params.length; i += 2)
        {
            parameters.put(params[2 * i], params[2 * i + 1]);
        }

        QueryTableModel table =
                createReportFromAggregationService(user, "search-service-aggregation-service",
                        parameters);

        assertEquals("[result]", getHeaders(table).toString());
        assertEquals(1, table.getRows().size());
        return Integer.parseInt(table.getRows().get(0)[0].toString());
    }

    @Test
    public void testJythonReportListingProperties() throws Exception
    {
        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("dataSetTypes", Arrays.asList("HCS_IMAGE", "UNKNOWN"));

        parameters.put("sampleTypes", Arrays.asList("MASTER_PLATE", "DILUTION_PLATE"));

        parameters.put("experimentTypes", Arrays.asList("COMPOUND_HCS"));

        parameters.put("materialTypes", Arrays.asList("BACTERIUM"));

        QueryTableModel table =
                createReportFromAggregationService("property-definitions-aggregation-service",
                        parameters);

        assertEquals("[Entity Kind, Entity Type, Property Definition]", getHeaders(table)
                .toString());

        assertRowsContent(
                table,
                "[Data Set, HCS_IMAGE, COMMENT Any other comments Comment True 1 False False None True]",
                "[Data Set, HCS_IMAGE, ANY_MATERIAL any_material any_material False 2 False False None True]",
                "[Data Set, HCS_IMAGE, BACTERIUM bacterium bacterium False 3 False False None True]",
                "[Data Set, HCS_IMAGE, GENDER The gender of the living organism Gender False 4 False False None True]",
                "[Data Set, UNKNOWN, N/A]",
                "[Sample, MASTER_PLATE, $PLATE_GEOMETRY Plate Geometry Plate Geometry True 1 False False None True]",
                "[Sample, MASTER_PLATE, DESCRIPTION A Description Description False 2 False False None True]",
                "[Sample, DILUTION_PLATE, OFFSET Offset from the start of the sequence Offset False 1 False False None True]",
                "[Experiment, COMPOUND_HCS, DESCRIPTION A Description Description True 1 False False None True]",
                "[Experiment, COMPOUND_HCS, COMMENT Any other comments Comment False 2 False False None True]",
                "[Experiment, COMPOUND_HCS, ANY_MATERIAL any_material any_material False 3 False False None True]",
                "[Material, BACTERIUM, DESCRIPTION A Description Description True 1 False False None True]",
                "[Material, BACTERIUM, ORGANISM The organism from which cells come Organism False 2 False False None True]"

        );

    }

    @Test
    public void testMetaprojectApi()
    {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("space_id", "META_" + getTestId());
        QueryTableModel table = createReportFromAggregationService("metaproject-api", parameters);

        // assert new metaproject with identical description created
        IGeneralInformationService openBISService = getGeneralInformationService();
        Metaproject oldProject =
                openBISService.getMetaproject(getSessionToken(),
                        new MetaprojectIdentifierId("test", "TEST_METAPROJECTS")).getMetaproject();
        Metaproject newProject =
                openBISService.getMetaproject(
                        getSessionToken(),
                        new MetaprojectIdentifierId("test", "META_" + getTestId()
                                + "_COPY_TEST_METAPROJCTS")).getMetaproject();
        assertEquals(oldProject.getDescription(), newProject.getDescription());

        // find newly created sample and assert that is assigned to newly created metaproject
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "SAMPLE"));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, "META_"
                + getTestId()));

        List<Sample> samples =
                openBISService.searchForSamplesOnBehalfOfUser(getSessionToken(), sc,
                        EnumSet.of(SampleFetchOption.METAPROJECTS), "test");
        assertEquals(1, samples.size());

        List<Metaproject> metaprojects = samples.get(0).getMetaprojects();
        assertEquals(1, metaprojects.size());
        assertEquals("META_" + getTestId() + "_TEST_META", metaprojects.get(0).getName());

        assertRowsContent(table, "[ANOTHER_TEST_METAPROJECTS Another example metaproject test]",
                "[TEST_METAPROJECTS Example metaproject no. 1 test]",
                "[TEST_METAPROJECTS Example metaproject no. 2 test_role]",
                "[TEST_METAPROJECTS_2 Example metaproject no. 2 test_role]", "[ASSIGNMENTS]",
                "[SAMPLE None 201206191219327-1055]", "[EXPERIMENT None 201206190940555-1032]",
                "[DATASET 20120619092259000-22]", "[MATERIAL AD3 (VIRUS)]",
                "[OLD-SAMPLE TEST_METAPROJECTS Example metaproject no. 2 test_role]");
    }

    private void assertRowsContent(QueryTableModel table, String... contents)
    {
        for (int i = 0; i < contents.length; i++)
        {
            assertEquals(contents[i], Arrays.asList(table.getRows().get(i)).toString());
        }
        if (table.getRows().size() > contents.length)
        {
            fail("Unexpected row in the result: "
                    + Arrays.asList(table.getRows().get(contents.length)).toString());
        }
        assertEquals(contents.length, table.getRows().size());
    }
}
