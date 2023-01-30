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

package ch.systemsx.cisd.openbis.systemtest.plugin.query;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class QueryEditingTest extends QuerySystemTestCase
{

    private final static QueryDatabase DATABASE = new QueryDatabase("1");

    @AfterMethod
    public void tearDown()
    {
        List<TechId> ids = new ArrayList<TechId>();
        for (QueryType queryType : QueryType.values())
        {
            List<QueryExpression> queries =
                    queryClientService.listQueries(queryType, BasicEntityType.UNSPECIFIED);
            for (QueryExpression queryExpression : queries)
            {
                ids.add(new TechId(queryExpression.getId()));
            }
        }
        queryClientService.deleteQueries(ids);
    }

    @Test
    public void testInitDatabases()
    {
        logIntoCommonClientService();

        assertEquals(2, queryClientService.initDatabases());
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testGetQueryDatabases()
    {
        logIntoCommonClientService();

        assertEquals("openBIS meta data", queryClientService.listQueryDatabases().get(0).getLabel());
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testRegisterEditAndDeleteQueryDefinition()
    {
        logIntoCommonClientService();
        assertEquals(0,
                queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED)
                        .size());

        NewQuery query =
                createQuery("query1", "select * from sample_types", true, DATABASE,
                        QueryType.GENERIC, null);
        queryClientService.registerQuery(query);

        List<QueryExpression> queries =
                queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED);
        assertEquals(1, queries.size());
        assertQuery(query, queries.get(0));
        QueryExpression actualQuery = queries.get(0);
        assertQuery(query, actualQuery);

        actualQuery.setDescription("hello");
        actualQuery.setName("new query1");
        actualQuery.setExpression("select * from something");
        actualQuery.setPublic(false);
        queryClientService.updateQuery(actualQuery);

        queries = queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED);
        assertEquals(1, queries.size());
        assertEquals(actualQuery.getName(), queries.get(0).getName());
        assertEquals(actualQuery.getDescription(), queries.get(0).getDescription());
        assertEquals(actualQuery.getExpression(), queries.get(0).getExpression());
        assertEquals(actualQuery.isPublic(), queries.get(0).isPublic());

        queryClientService.deleteQueries(Arrays.asList(new TechId(queries.get(0).getId())));

        assertEquals(0,
                queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED)
                        .size());
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testRegisterQueryDefinitionsWithSameName()
    {
        logIntoCommonClientService();

        NewQuery query =
                createQuery("query", "select * from sample_types", true, DATABASE,
                        QueryType.GENERIC, null);
        queryClientService.registerQuery(query);

        try
        {
            queryClientService.registerQuery(query);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Query definition 'query' already exists "
                    + "in the database and needs to be unique.", ex.getMessage());
        }
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testChangeNameOfQueryDefinitionsToAnExistingOne()
    {
        logIntoCommonClientService();

        NewQuery query1 =
                createQuery("query1", "select * from sample_types", true, DATABASE,
                        QueryType.GENERIC, null);
        NewQuery query2 =
                createQuery("query2", "select * from experiment_types", true, DATABASE,
                        QueryType.GENERIC, null);
        queryClientService.registerQuery(query1);
        queryClientService.registerQuery(query2);

        List<QueryExpression> queries =
                queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED);
        assertEquals(2, queries.size());
        Collections.sort(queries, new Comparator<QueryExpression>()
            {
                @Override
                public int compare(QueryExpression o1, QueryExpression o2)
                {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        QueryExpression queryExpression = queries.get(0);
        assertEquals("query1", queryExpression.getName());
        queryExpression.setName("query2");

        try
        {
            queryClientService.updateQuery(queryExpression);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Query definition 'query2' already exists "
                    + "in the database and needs to be unique.", ex.getMessage());
        }
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testCreateQueryResult()
    {
        logIntoCommonClientService();

        QueryDatabase database = new QueryDatabase("1", "label");
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("id", "1");
        TableModelReference table =
                queryClientService
                        .createQueryResultsReport(
                                database,
                                "select id, code, modification_timestamp from sample_types where id = ${id}",
                                bindings);
        checkTable(table);
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testRegisterQueryAndExecuteIt()
    {
        logIntoCommonClientService();

        NewQuery query =
                createQuery(
                        "query",
                        "select id, code, modification_timestamp from sample_types where id = ${id}",
                        true, DATABASE, QueryType.GENERIC, null);
        queryClientService.registerQuery(query);

        List<QueryExpression> queries =
                queryClientService.listQueries(QueryType.GENERIC, BasicEntityType.UNSPECIFIED);
        assertEquals(1, queries.size());
        QueryExpression actualQuery = queries.get(0);
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("id", "1");

        TableModelReference table =
                queryClientService.createQueryResultsReport(new TechId(actualQuery.getId()),
                        bindings);

        checkTable(table);
    }

    @Test(dependsOnMethods = "testInitDatabases")
    public void testRegisterAndListSampleQueries()
    {
        logIntoCommonClientService();

        final String noTypeQueryName = "query no type";
        final String cellPlateQueryName = "query cell plate";
        final String cellPlateType = "CELL_PLATE";
        final String wellQueryName = "query well";
        final String wellType = "WELL";
        final String cRegexpQueryName = "query c*";
        final String cRegexpType = "C.*";
        NewQuery query =
                createQuery(noTypeQueryName, "select * from samples", true, DATABASE,
                        QueryType.SAMPLE, null);
        queryClientService.registerQuery(query);
        query.setName(cellPlateQueryName);
        query.setEntityTypeCode(cellPlateType);
        queryClientService.registerQuery(query);
        query.setName(wellQueryName);
        query.setEntityTypeCode(wellType);
        queryClientService.registerQuery(query);
        query.setName(cRegexpQueryName);
        query.setEntityTypeCode(cRegexpType);
        queryClientService.registerQuery(query);

        List<QueryExpression> queriesNoType =
                queryClientService.listQueries(QueryType.SAMPLE, BasicEntityType.UNSPECIFIED);
        assertEquals(4, queriesNoType.size());
        assertEquals(noTypeQueryName, queriesNoType.get(0).getName());
        assertEquals(cellPlateQueryName, queriesNoType.get(1).getName());
        assertEquals(wellQueryName, queriesNoType.get(2).getName());
        assertEquals(cRegexpQueryName, queriesNoType.get(3).getName());
        List<QueryExpression> queriesForCellPlate =
                queryClientService
                        .listQueries(QueryType.SAMPLE, new BasicEntityType(cellPlateType));
        assertEquals(3, queriesForCellPlate.size());
        assertEquals(noTypeQueryName, queriesForCellPlate.get(0).getName());
        assertEquals(cellPlateQueryName, queriesForCellPlate.get(1).getName());
        assertEquals(cRegexpQueryName, queriesForCellPlate.get(2).getName());
        List<QueryExpression> queriesForWell =
                queryClientService.listQueries(QueryType.SAMPLE, new BasicEntityType(wellType));
        assertEquals(2, queriesForWell.size());
        assertEquals(noTypeQueryName, queriesForWell.get(0).getName());
        assertEquals(wellQueryName, queriesForWell.get(1).getName());
        List<QueryExpression> queriesForControlLayout =
                queryClientService.listQueries(QueryType.SAMPLE, new BasicEntityType(
                        "CONTROL_LAYOUT"));
        assertEquals(2, queriesForControlLayout.size());
        assertEquals(noTypeQueryName, queriesForControlLayout.get(0).getName());
        assertEquals(cRegexpQueryName, queriesForControlLayout.get(1).getName());
    }

    private void checkTable(TableModelReference table)
    {
        List<TableModelColumnHeader> headers = table.getHeader();
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(DataTypeCode.INTEGER, headers.get(0).getDataType());
        assertEquals(true, headers.get(0).isNumeric());
        assertEquals("code", headers.get(1).getTitle());
        assertEquals(DataTypeCode.VARCHAR, headers.get(1).getDataType());
        assertEquals(false, headers.get(1).isNumeric());
        assertEquals(DataTypeCode.TIMESTAMP, headers.get(2).getDataType());
        assertEquals(3, headers.size());

        DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> config =
                new DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>>();
        config.setCacheConfig(ResultSetFetchConfig.createFetchFromCache(table.getResultSetKey()));
        TypedTableResultSet<ReportRowModel> rs = commonClientService.listReport(config);
        GridRowModels<TableModelRowWithObject<ReportRowModel>> l = rs.getResultSet().getList();
        assertEquals("[1, MASTER_PLATE, Mon Mar 23 15:34:44 CET 2009]", l.get(0)
                .getOriginalObject().getValues().toString());
        assertEquals(1, rs.getResultSet().getTotalLength());
    }

    private NewQuery createQuery(String name, String expression, boolean isPublic,
            QueryDatabase database, QueryType queryType, String entityTypeCodeOrNull)
    {
        NewQuery query = new NewQuery();
        query.setName(name);
        query.setDescription("A simple query named '" + name + "'.");
        query.setExpression(expression);
        query.setPublic(isPublic);
        query.setQueryDatabase(database);
        query.setQueryType(queryType);
        query.setEntityTypeCode(entityTypeCodeOrNull);
        return query;
    }

    private void assertQuery(NewExpression expectedQuery, QueryExpression actualQuery)
    {
        assertEquals(expectedQuery.getName(), actualQuery.getName());
        assertEquals(expectedQuery.getDescription(), actualQuery.getDescription());
        assertEquals(expectedQuery.getExpression(), actualQuery.getExpression());
        assertEquals(expectedQuery.isPublic(), actualQuery.isPublic());
    }
}
