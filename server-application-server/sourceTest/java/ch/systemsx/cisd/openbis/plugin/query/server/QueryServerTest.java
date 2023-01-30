/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * Test cases for corresponding {@link QueryServer} class.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = QueryServer.class)
public final class QueryServerTest extends AbstractServerTestCase
{
    private static final String MASTER_PLATE = "MASTER_PLATE";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    private static final String CELL_PLATE = "CELL_PLATE";

    private final static String DATABASE_KEY = "1";

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private IQueryDatabaseDefinitionProvider dbDefinitionProvider;

    private final IQueryServer createServer()
    {
        return new QueryServer(sessionManager, daoFactory, propertiesBatchManager,
                sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin, dbDefinitionProvider);
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        dbDefinitionProvider = context.mock(IQueryDatabaseDefinitionProvider.class);
    }

    @Test
    public void testListQueriesSimple()
    {
        prepareGetSession();

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getQueryDAO();
                    will(returnValue(queryDAO));

                    for (QueryType type : QueryType.values())
                    {
                        one(queryDAO).listQueries(type);
                        will(returnValue(new ArrayList<QueryPE>()));
                    }
                    one(queryDAO).listQueries(null);
                    will(returnValue(new ArrayList<QueryPE>()));
                }
            });

        IQueryServer queryServer = createServer();
        for (QueryType type : QueryType.values())
        {
            queryServer.listQueries(SESSION_TOKEN, type, null);
        }
        queryServer.listQueries(SESSION_TOKEN, null, null);
        context.assertIsSatisfied();
    }

    @Test
    public void testListQueriesWithFilteryingByEntityType()
    {
        prepareGetSession();

        final PersonPE person = createSystemUser();
        final DatabaseDefinition dbDefinition = createDatabaseDefinition(DATABASE_KEY);

        final List<QueryPE> queryPEs = new ArrayList<QueryPE>();
        queryPEs.add(createQuery("q", "sample query for samples", "select * from bla", true,
                person, QueryType.SAMPLE, null, DATABASE_KEY));
        queryPEs.add(createQuery("qc", "sample query for C.*", "select * from bla_c", true, person,
                QueryType.SAMPLE, "C.*", DATABASE_KEY));
        queryPEs.add(createQuery("qcp", "sample query for CELL_PLATE", "select * from bla_cp",
                true, person, QueryType.SAMPLE, CELL_PLATE, DATABASE_KEY));
        queryPEs.add(createQuery("qcl", "sample query for CONTROL_LAYOUT", "select * from bla_cl",
                true, person, QueryType.SAMPLE, CONTROL_LAYOUT, DATABASE_KEY));
        queryPEs.add(createQuery("qmp", "sample query for MASTER_PLATE", "select * from bla_mp",
                true, person, QueryType.SAMPLE, MASTER_PLATE, DATABASE_KEY));

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getQueryDAO();
                    will(returnValue(queryDAO));

                    allowing(queryDAO).listQueries(QueryType.SAMPLE);
                    will(returnValue(queryPEs));

                    allowing(dbDefinitionProvider).getDefinition(DATABASE_KEY);
                    will(returnValue(dbDefinition));
                }
            });

        IQueryServer queryServer = createServer();
        // list all sample queries
        List<QueryExpression> queries =
                queryServer.listQueries(SESSION_TOKEN, QueryType.SAMPLE, null);
        assertEquals(5, queries.size());
        // list only CELL_PLATE sample queries
        queries =
                queryServer.listQueries(SESSION_TOKEN, QueryType.SAMPLE, new BasicEntityType(
                        CELL_PLATE));
        assertEquals(3, queries.size());
        assertEquals("q", queries.get(0).getName());
        assertEquals("qc", queries.get(1).getName());
        assertEquals("qcp", queries.get(2).getName());
        // list only CONTROL_LAYOUT sample queries
        queries =
                queryServer.listQueries(SESSION_TOKEN, QueryType.SAMPLE, new BasicEntityType(
                        CONTROL_LAYOUT));
        assertEquals(3, queries.size());
        assertEquals("q", queries.get(0).getName());
        assertEquals("qc", queries.get(1).getName());
        assertEquals("qcl", queries.get(2).getName());
        // list only MASTER_PLATE sample queries
        queries =
                queryServer.listQueries(SESSION_TOKEN, QueryType.SAMPLE, new BasicEntityType(
                        MASTER_PLATE));
        assertEquals(2, queries.size());
        assertEquals("q", queries.get(0).getName());
        assertEquals("qmp", queries.get(1).getName());

        context.assertIsSatisfied();
    }

    // helper methods

    private static DatabaseDefinition createDatabaseDefinition(String databaseKey)
    {
        return new DatabaseDefinition(new SimpleDatabaseConfigurationContext("driverClassName",
                "url", "username", "password", null), databaseKey, databaseKey,
                RoleWithHierarchy.SPACE_USER, null);
    }

    private static QueryPE createQuery(String name, String description, String expression,
            boolean isPublic, PersonPE registrator, QueryType type, String entityTypeCodeOrNull,
            String databaseKey)
    {
        QueryPE query = new QueryPE();
        query.setName(name);
        query.setDescription(description);
        query.setExpression(expression);
        query.setPublic(isPublic);
        query.setRegistrator(registrator);
        query.setQueryType(type);
        query.setEntityTypeCodePattern(entityTypeCodeOrNull);
        query.setQueryDatabaseKey(databaseKey);
        return query;
    }

}
