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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class QueryDAOTest extends AbstractDAOTest
{

    private final static String DATABASE_KEY = "1";

    @Test
    public void testCreateQueries() throws Exception
    {
        IQueryDAO queryDAO = daoFactory.getQueryDAO();
        assertEquals(0, queryDAO.listAllEntities().size());
        QueryPE qg =
                createQuery("qg", "generic query", "select * from blabla", true, getSystemPerson(),
                        QueryType.GENERIC, null, DATABASE_KEY);
        queryDAO.createQuery(qg);
        QueryPE qs =
                createQuery("qs", "sample query", "select * from blabla", true, getSystemPerson(),
                        QueryType.SAMPLE, null, DATABASE_KEY);
        queryDAO.createQuery(qs);
        QueryPE qst1 =
                createQuery("qst1", "sample query for CELL_PLATE", "select * from blabla", true,
                        getSystemPerson(), QueryType.SAMPLE, "CELL_PLATE", DATABASE_KEY);
        queryDAO.createQuery(qst1);
        QueryPE qst2 =
                createQuery("qst2", "sample query for CONTROL_LAYOUT", "select * from blabla",
                        true, getSystemPerson(), QueryType.SAMPLE, "CONTROL_LAYOUT", DATABASE_KEY);
        queryDAO.createQuery(qst2);
        QueryPE qe =
                createQuery("qe", "experiment query", "select * from blabla", true,
                        getSystemPerson(), QueryType.EXPERIMENT, null, DATABASE_KEY);
        queryDAO.createQuery(qe);
        QueryPE qet =
                createQuery("qet", "experiment query for SIRNA_HCS", "select * from blabla", true,
                        getSystemPerson(), QueryType.EXPERIMENT, "SIRNA_HCS", DATABASE_KEY);
        queryDAO.createQuery(qet);
        QueryPE qm =
                createQuery("qm", "material query", "select * from blabla", true,
                        getSystemPerson(), QueryType.MATERIAL, null, DATABASE_KEY);
        queryDAO.createQuery(qm);
        QueryPE qmt =
                createQuery("qmt", "material query for BACTERIUM", "select * from blabla", true,
                        getSystemPerson(), QueryType.MATERIAL, "BACTERIUM", DATABASE_KEY);
        queryDAO.createQuery(qmt);
        QueryPE qd =
                createQuery("qd", "data_set query", "select * from blabla", true,
                        getSystemPerson(), QueryType.DATA_SET, null, DATABASE_KEY);
        queryDAO.createQuery(qd);
        QueryPE qdt =
                createQuery("qdt", "data_set query for UNKNOWN", "select * from blabla", true,
                        getSystemPerson(), QueryType.DATA_SET, "UNKNOWN", DATABASE_KEY);
        queryDAO.createQuery(qdt);

        List<QueryPE> queries = queryDAO.listQueries(QueryType.GENERIC);
        assertEquals(1, queries.size());
        assertQueryEquality(qg, queries.get(0));

        queries = queryDAO.listQueries(QueryType.SAMPLE);
        assertEquals(3, queries.size());
        assertQueryEquality(qs, queries.get(0));
        assertQueryEquality(qst1, queries.get(1));
        assertQueryEquality(qst2, queries.get(2));

        queries = queryDAO.listQueries(QueryType.EXPERIMENT);
        assertEquals(2, queries.size());
        assertQueryEquality(qe, queries.get(0));
        assertQueryEquality(qet, queries.get(1));

        queries = queryDAO.listQueries(QueryType.DATA_SET);
        assertEquals(2, queries.size());
        assertQueryEquality(qd, queries.get(0));
        assertQueryEquality(qdt, queries.get(1));

        queries = queryDAO.listQueries(QueryType.MATERIAL);
        assertEquals(2, queries.size());
        assertQueryEquality(qm, queries.get(0));
        assertQueryEquality(qmt, queries.get(1));
    }

    private void assertQueryEquality(QueryPE expectedQuery, QueryPE actualQuery)
    {
        assertEquals(expectedQuery.getName(), actualQuery.getName());
        assertEquals(expectedQuery.getDescription(), actualQuery.getDescription());
        assertEquals(expectedQuery.getExpression(), actualQuery.getExpression());
        assertEquals(expectedQuery.isPublic(), actualQuery.isPublic());
        assertEquals(expectedQuery.getRegistrator(), actualQuery.getRegistrator());
        assertEquals(expectedQuery.getQueryType(), actualQuery.getQueryType());
        assertEquals(expectedQuery.getEntityTypeCodePattern(),
                actualQuery.getEntityTypeCodePattern());
    }

    private QueryPE createQuery(String name, String description, String expression,
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
