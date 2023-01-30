/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
@Test
public class CreateQueryTest extends AbstractQueryTest
{

    @Test
    public void testCreate()
    {
        QueryCreation creation = testCreation();

        Query query = createQuery(TEST_USER, PASSWORD, creation);

        assertNotNull(query.getPermId());
        assertEquals(query.getName(), creation.getName());
        assertEquals(query.getDescription(), creation.getDescription());
        assertEquals(query.getDatabaseId(), creation.getDatabaseId());
        assertEquals(query.getEntityTypeCodePattern(), creation.getEntityTypeCodePattern());
        assertEquals(query.getQueryType(), creation.getQueryType());
        assertEquals(query.isPublic(), creation.isPublic());
        assertEquals(query.getSql(), creation.getSql());
        assertEquals(query.getRegistrator().getUserId(), TEST_USER);
        assertToday(query.getRegistrationDate());
        assertToday(query.getModificationDate());
    }

    @Test
    public void testCreateWithDatabaseWithSpaceNullAndDefaultCreatorRoleUsingSpaceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createQuery(TEST_GROUP_OBSERVER, PASSWORD, creation);
                }
            }, creation.getDatabaseId());
    }

    @Test
    public void testCreateWithDatabaseWithSpaceNullAndDefaultCreatorRoleUsingInstanceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);

        Query query = createQuery(TEST_INSTANCE_OBSERVER, PASSWORD, creation);

        assertEquals(query.getName(), creation.getName());
    }

    @Test
    public void testCreateWithDatabaseWithSpaceNotNullAndDefaultCreatorRoleUsingSpaceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createQuery(TEST_OBSERVER_CISD, PASSWORD, creation);
                }
            }, creation.getDatabaseId());
    }

    @Test
    public void testCreateWithDatabaseWithSpaceNotNullAndDefaultCreatorRoleUsingSpacePowerUser()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));

        Query query = createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);

        assertEquals(query.getName(), creation.getName());
    }

    @Test
    public void testCreateWithNameNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setName(null);
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Name cannot be empty");
    }

    @Test
    public void testCreateWithNameExisting()
    {
        QueryCreation creation = testCreation();

        createQuery(TEST_USER, PASSWORD, creation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Query already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithDatabaseNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setDatabaseId(null);
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Database id cannot be null");
    }

    @Test
    public void testCreateWithDatabaseNonexistent()
    {
        QueryDatabaseName databaseId = new QueryDatabaseName("idontexist");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setDatabaseId(databaseId);
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, databaseId);
    }

    @Test
    public void testCreateWithQueryTypeNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setQueryType(null);
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Query type cannot be null");
    }

    @Test
    public void testCreateWithEntityTypeCodePatternNotNullAndQueryTypeGeneric()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setQueryType(QueryType.GENERIC);
                    creation.setEntityTypeCodePattern("SOME_PATTERN");
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Entity type code pattern cannot be specified for a query with type GENERIC");
    }

    @Test
    public void testCreateWithSqlNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setSql(null);
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Sql cannot be empty");
    }

    @Test
    public void testCreateWithNonSelectSql()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setSql("update spaces set code = 'YOU_HAVE_BEEN_HACKED' where code = 'CISD'");
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Sorry, only select statements are allowed");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryCreation creation = testCreation();
                    creation.setSql("select * from spaces; update spaces set code = 'YOU_HAVE_BEEN_HACKED' where code = 'CISD'");
                    createQuery(TEST_USER, PASSWORD, creation);
                }
            }, "Sorry, only one query statement is allowed: A ';' somewhere in the middle has been found.");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryCreation creation = testCreation();
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setName("LOG_TEST_1");

        QueryCreation creation2 = testCreation();
        creation2.setDatabaseId(DB_TEST_CISD_ID);
        creation2.setName("LOG_TEST_2");

        v3api.createQueries(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-queries  NEW_QUERIES('[QueryCreation[databaseId=1,name=LOG_TEST_1], QueryCreation[databaseId=test-database,name=LOG_TEST_2]]')");
    }

}
