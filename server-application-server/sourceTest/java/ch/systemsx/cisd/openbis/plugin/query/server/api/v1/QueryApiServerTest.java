/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.query.server.api.v1;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.DisplaySettingsProvider;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
public class QueryApiServerTest extends AbstractServerTestCase
{
    private IQueryServer queryServer;

    private ICommonServer commonServer;

    private IQueryApiServer queryApiServer;

    @BeforeMethod
    public void before()
    {
        queryServer = context.mock(IQueryServer.class);
        commonServer = context.mock(ICommonServer.class);
        final QueryApiServer queryApiServerInt =
                new QueryApiServer(queryServer, commonServer, sessionManager, daoFactory,
                        propertiesBatchManager);
        queryApiServerInt.setDisplaySettingsProvider(new DisplaySettingsProvider());
        queryApiServer = queryApiServerInt;
    }

    @Test
    public void testTryToAuthenticateAtQueryServer()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession("Albert", "E=mc2");
                    will(returnValue(SESSION_TOKEN));

                    Session session = new Session("Albert", SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
                    roleAssignment.setRole(RoleCode.ADMIN);

                    PersonPE person = new PersonPE();
                    person.setUserId("Albert");
                    person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays
                            .asList(roleAssignment)));
                    person.setActive(true);
                    allowing(personDAO).tryFindPersonByUserId(session.getUserName());
                    will(returnValue(person));

                    one(personDAO).lock(person);

                    one(personDAO).updatePerson(person);

                    one(queryServer).initDatabases(SESSION_TOKEN);
                }
            });

        queryApiServer.tryToAuthenticateAtQueryServer("Albert", "E=mc2");

        context.assertIsSatisfied();
    }

    @Test
    public void testListQueries()
    {
        context.checking(new Expectations()
            {
                {
                    one(queryServer).listQueries(SESSION_TOKEN, QueryType.GENERIC,
                            BasicEntityType.UNSPECIFIED);
                    QueryExpression queryExpression = new QueryExpression("");
                    queryExpression.setupParameters(Arrays.asList("y", "z::blabla"));
                    queryExpression.setId(42L);
                    queryExpression.setName("test");
                    queryExpression.setDescription("my SQL query");
                    will(returnValue(Arrays.asList(queryExpression)));
                }
            });

        List<QueryDescription> queries = queryApiServer.listQueries(SESSION_TOKEN);

        assertEquals(42L, queries.get(0).getId());
        assertEquals("test", queries.get(0).getName());
        assertEquals("my SQL query", queries.get(0).getDescription());
        assertEquals("[y, z]", queries.get(0).getParameters().toString());
        assertEquals(1, queries.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteQuery()
    {
        final RecordingMatcher<QueryParameterBindings> bindingMatcher =
                new RecordingMatcher<QueryParameterBindings>();
        context.checking(new Expectations()
            {
                {
                    one(queryServer).queryDatabase(with(SESSION_TOKEN), with(new TechId(42)),
                            with(bindingMatcher));
                    SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
                    builder.addFullHeader("Integer", "Double", "String");
                    builder.addRow(Arrays.asList(new IntegerTableCell(42), new DoubleTableCell(
                            Math.PI), new StringTableCell("hello")));
                    will(returnValue(builder.getTableModel()));
                }
            });
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put("a", "alpha");

        QueryTableModel tableModel =
                queryApiServer.executeQuery(SESSION_TOKEN, 42, parameterBindings);

        assertEquals("a=alpha", bindingMatcher.recordedObject().toString());
        List<QueryTableColumn> columns = tableModel.getColumns();
        assertEquals("Integer", columns.get(0).getTitle());
        assertEquals(QueryTableColumnDataType.LONG, columns.get(0).getDataType());
        assertEquals("Double", columns.get(1).getTitle());
        assertEquals(QueryTableColumnDataType.DOUBLE, columns.get(1).getDataType());
        assertEquals("String", columns.get(2).getTitle());
        assertEquals(QueryTableColumnDataType.STRING, columns.get(2).getDataType());
        assertEquals(3, columns.size());
        List<Serializable[]> rows = tableModel.getRows();
        assertEquals(42L, rows.get(0)[0]);
        assertEquals(Math.PI, rows.get(0)[1]);
        assertEquals("hello", rows.get(0)[2]);
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListTableReportDescriptions()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(new Session("u", SESSION_TOKEN, new Principal(), "", 1)));

                    one(dataStoreDAO).listDataStores();
                    DataStorePE dataStore = new DataStorePE();
                    dataStore.setCode("DS");
                    DataStoreServicePE s1 = new DataStoreServicePE();
                    DataSetTypePE dataSetType1 = new DataSetTypePE();
                    dataSetType1.setCode("type1");
                    DataSetTypePE dataSetType2 = new DataSetTypePE();
                    dataSetType2.setCode("type2");
                    s1.setDatasetTypes(new LinkedHashSet<DataSetTypePE>(Arrays.asList(dataSetType1,
                            dataSetType2)));
                    s1.setKind(DataStoreServiceKind.QUERIES);
                    s1.setReportingPluginTypeOrNull(ReportingPluginType.TABLE_MODEL);
                    s1.setKey("S1");
                    s1.setLabel("my service");
                    DataStoreServicePE s2 = new DataStoreServicePE();
                    s2.setKind(DataStoreServiceKind.QUERIES);
                    DataStoreServicePE s3 = new DataStoreServicePE();
                    s3.setReportingPluginTypeOrNull(ReportingPluginType.TABLE_MODEL);
                    dataStore.setServices(new HashSet<DataStoreServicePE>(Arrays.asList(s1, s2, s3)));
                    will(returnValue(Arrays.asList(dataStore)));
                }
            });

        List<ReportDescription> reportDescriptions =
                queryApiServer.listTableReportDescriptions(SESSION_TOKEN);

        assertEquals("DS", reportDescriptions.get(0).getDataStoreCode());
        assertEquals("S1", reportDescriptions.get(0).getKey());
        assertEquals("my service", reportDescriptions.get(0).getLabel());
        assertEquals("[type1, type2]", reportDescriptions.get(0).getDataSetTypes().toString());
        assertEquals(1, reportDescriptions.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateReportFromDataSets()
    {
        final List<String> dataSetCodes = Arrays.asList("1", "2");
        final RecordingMatcher<DatastoreServiceDescription> descriptionMatcher =
                new RecordingMatcher<DatastoreServiceDescription>();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).createReportFromDatasets(with(SESSION_TOKEN),
                            with(descriptionMatcher), with(dataSetCodes));
                    SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
                    builder.addFullHeader("Integer", "Double", "String");
                    builder.addRow(Arrays.asList(new IntegerTableCell(42), new DoubleTableCell(
                            Math.PI), new StringTableCell("hello")));
                    will(returnValue(builder.getTableModel()));
                }
            });

        QueryTableModel tableModel =
                queryApiServer.createReportFromDataSets(SESSION_TOKEN, "DS", "S1", dataSetCodes);

        DatastoreServiceDescription description = descriptionMatcher.recordedObject();
        assertEquals("S1", description.getKey());
        assertEquals("", description.getLabel());
        assertEquals("DS", description.getDatastoreCode());
        assertEquals("[]", Arrays.asList(description.getDatasetTypeCodes()).toString());
        assertEquals(DataStoreServiceKind.QUERIES, description.getServiceKind());
        List<QueryTableColumn> columns = tableModel.getColumns();
        assertEquals("Integer", columns.get(0).getTitle());
        assertEquals(QueryTableColumnDataType.LONG, columns.get(0).getDataType());
        assertEquals("Double", columns.get(1).getTitle());
        assertEquals(QueryTableColumnDataType.DOUBLE, columns.get(1).getDataType());
        assertEquals("String", columns.get(2).getTitle());
        assertEquals(QueryTableColumnDataType.STRING, columns.get(2).getDataType());
        assertEquals(3, columns.size());
        List<Serializable[]> rows = tableModel.getRows();
        assertEquals(42L, rows.get(0)[0]);
        assertEquals(Math.PI, rows.get(0)[1]);
        assertEquals("hello", rows.get(0)[2]);
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListAggregationServices()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(new Session("u", SESSION_TOKEN, new Principal(), "", 1)));

                    one(dataStoreDAO).listDataStores();
                    DataStorePE dataStore = new DataStorePE();
                    dataStore.setCode("DS");
                    DataStoreServicePE s1 = new DataStoreServicePE();
                    s1.setDatasetTypes(new LinkedHashSet<DataSetTypePE>());
                    s1.setKind(DataStoreServiceKind.QUERIES);
                    s1.setReportingPluginTypeOrNull(ReportingPluginType.AGGREGATION_TABLE_MODEL);
                    s1.setKey("S1");
                    s1.setLabel("my service");
                    DataStoreServicePE s2 = new DataStoreServicePE();
                    s2.setKind(DataStoreServiceKind.QUERIES);
                    DataStoreServicePE s3 = new DataStoreServicePE();
                    s3.setReportingPluginTypeOrNull(ReportingPluginType.TABLE_MODEL);
                    dataStore.setServices(new HashSet<DataStoreServicePE>(Arrays.asList(s1, s2, s3)));
                    will(returnValue(Arrays.asList(dataStore)));
                }
            });

        List<AggregationServiceDescription> reportDescriptions =
                queryApiServer.listAggregationServices(SESSION_TOKEN);

        assertEquals("DS", reportDescriptions.get(0).getDataStoreCode());
        assertEquals("S1", reportDescriptions.get(0).getServiceKey());
        assertEquals(1, reportDescriptions.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateReportFromAggregationService()
    {
        final RecordingMatcher<DatastoreServiceDescription> descriptionMatcher =
                new RecordingMatcher<DatastoreServiceDescription>();
        final HashMap<String, Object> serviceParams = new HashMap<String, Object>();
        serviceParams.put("param1", "foo");
        serviceParams.put("param2", Arrays.asList("1", "2"));
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(new Session("u", SESSION_TOKEN, new Principal(), "", 1)));

                    one(commonServer).createReportFromAggregationService(with(SESSION_TOKEN),
                            with(descriptionMatcher), with(serviceParams));
                    SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
                    builder.addFullHeader("Integer", "Double", "String");
                    builder.addRow(Arrays.asList(new IntegerTableCell(42), new DoubleTableCell(
                            Math.PI), new StringTableCell("hello")));
                    will(returnValue(builder.getTableModel()));
                }
            });

        QueryTableModel tableModel =
                queryApiServer.createReportFromAggregationService(SESSION_TOKEN, "DS", "S1",
                        serviceParams);

        DatastoreServiceDescription description = descriptionMatcher.recordedObject();
        assertEquals("S1", description.getKey());
        assertEquals("", description.getLabel());
        assertEquals("DS", description.getDatastoreCode());
        assertEquals("[]", Arrays.asList(description.getDatasetTypeCodes()).toString());
        assertEquals(DataStoreServiceKind.QUERIES, description.getServiceKind());
        assertEquals(ReportingPluginType.AGGREGATION_TABLE_MODEL,
                description.tryReportingPluginType());
        List<QueryTableColumn> columns = tableModel.getColumns();
        assertEquals("Integer", columns.get(0).getTitle());
        assertEquals(QueryTableColumnDataType.LONG, columns.get(0).getDataType());
        assertEquals("Double", columns.get(1).getTitle());
        assertEquals(QueryTableColumnDataType.DOUBLE, columns.get(1).getDataType());
        assertEquals("String", columns.get(2).getTitle());
        assertEquals(QueryTableColumnDataType.STRING, columns.get(2).getDataType());
        assertEquals(3, columns.size());
        List<Serializable[]> rows = tableModel.getRows();
        assertEquals(42L, rows.get(0)[0]);
        assertEquals(Math.PI, rows.get(0)[1]);
        assertEquals("hello", rows.get(0)[2]);
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }
}
