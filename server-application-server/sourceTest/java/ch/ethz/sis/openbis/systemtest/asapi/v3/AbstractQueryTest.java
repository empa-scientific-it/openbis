/*
 * Copyright 2018 ETH Zuerich, CISD
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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class AbstractQueryTest extends AbstractTest
{

    protected static final String SELECT_SPACE_CODES_SQL = "select code from spaces order by id asc";

    protected static final String SELECT_EXPERIMENT_PERM_IDS_AND_IDENTIFIERS_SQL =
            "select e.perm_id as experiment_key, '/' || s.code || '/' || p.code || '/' || e.code as experiment_identifier from experiments e, projects p, spaces s where e.proj_id = p.id and p.space_id = s.id and e.registration_timestamp < '2018-01-01 00:00:00' order by experiment_identifier asc";

    protected static final String SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL = "select code, description from property_types where code = ${code}";

    protected static final String DB_OPENBIS_METADATA = "1";

    protected static final IQueryDatabaseId DB_OPENBIS_METADATA_ID = new QueryDatabaseName(DB_OPENBIS_METADATA);

    protected static final String DB_TEST_CISD = "test-database";

    protected static final IQueryDatabaseId DB_TEST_CISD_ID = new QueryDatabaseName(DB_TEST_CISD);

    protected static final String PROVIDER_TRUE_FALSE = "true-false";

    protected Person createUser(Role role, ISpaceId spaceId, IProjectId projectId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(UUID.randomUUID().toString() + "_pa_on");

        List<PersonPermId> personIds = v3api.createPersons(sessionToken, Arrays.asList(personCreation));

        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        roleCreation.setProjectId(projectId);
        roleCreation.setUserId(personIds.get(0));

        v3api.createRoleAssignments(sessionToken, Arrays.asList(roleCreation));

        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, personIds, new PersonFetchOptions());
        return persons.get(personIds.get(0));
    }

    protected QueryCreation testCreation()
    {
        QueryCreation creation = new QueryCreation();
        creation.setName("test name");
        creation.setDescription("test description");
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setEntityTypeCodePattern("SOME_EXPERIMENT_TYPE");
        creation.setQueryType(QueryType.EXPERIMENT);
        creation.setPublic(true);
        creation.setSql("select * from experiments where perm_id = ${key}");
        return creation;
    }

    protected QueryCreation selectSpaceCodesQueryCreation(IQueryDatabaseId databaseId)
    {
        QueryCreation creation = new QueryCreation();
        creation.setName("test query");
        creation.setQueryType(QueryType.GENERIC);
        creation.setSql(SELECT_SPACE_CODES_SQL);
        creation.setDatabaseId(databaseId);
        creation.setPublic(true);
        return creation;
    }

    protected QueryCreation selectExperimentPermIdsAndIdentifiersQueryCreation(IQueryDatabaseId databaseId)
    {
        QueryCreation creation = new QueryCreation();
        creation.setName("test query");
        creation.setQueryType(QueryType.EXPERIMENT);
        creation.setSql(SELECT_EXPERIMENT_PERM_IDS_AND_IDENTIFIERS_SQL);
        creation.setDatabaseId(databaseId);
        creation.setPublic(true);
        return creation;
    }

    protected QueryCreation selectPropertyTypeCodeAndDescriptionQueryCreation(IQueryDatabaseId databaseId)
    {
        QueryCreation creation = new QueryCreation();
        creation.setName("test query");
        creation.setQueryType(QueryType.GENERIC);
        creation.setSql(SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL);
        creation.setDatabaseId(databaseId);
        return creation;
    }

    protected Query createQuery(String user, String password, QueryCreation creation)
    {
        String sessionToken = v3api.login(user, password);

        List<QueryTechId> ids = v3api.createQueries(sessionToken, Arrays.asList(creation));
        assertEquals(ids.size(), 1);

        return getQuery(user, password, ids.get(0));
    }

    protected QueryPE createQueryWithExistingDB(String user)
    {
        PersonPE registrator = daoFactory.getPersonDAO().tryFindPersonByUserId(user);

        QueryPE queryPE = new QueryPE();
        queryPE.setName("my-database-exists");
        queryPE.setQueryType(ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType.GENERIC);
        queryPE.setQueryDatabaseKey(DB_OPENBIS_METADATA);
        queryPE.setExpression(SELECT_SPACE_CODES_SQL);
        queryPE.setRegistrator(registrator);

        daoFactory.getQueryDAO().createQuery(queryPE);

        return queryPE;
    }

    protected QueryPE createQueryWithNonExistentDB(String user)
    {
        PersonPE registrator = daoFactory.getPersonDAO().tryFindPersonByUserId(user);

        QueryPE queryPE = new QueryPE();
        queryPE.setName("my-database-does-not-exist");
        queryPE.setQueryType(ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType.GENERIC);
        queryPE.setQueryDatabaseKey("idontexist");
        queryPE.setExpression(SELECT_SPACE_CODES_SQL);
        queryPE.setRegistrator(registrator);

        daoFactory.getQueryDAO().createQuery(queryPE);

        return queryPE;
    }

    protected Query getQuery(String user, String password, IQueryId queryId)
    {
        String sessionToken = v3api.login(user, password);

        QueryFetchOptions fetchOptions = new QueryFetchOptions();
        fetchOptions.withRegistrator();

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(queryId), fetchOptions);

        return map.get(queryId);
    }

    protected Query updateQuery(String user, String password, QueryUpdate update)
    {
        String sessionToken = v3api.login(user, password);

        v3api.updateQueries(sessionToken, Arrays.asList(update));

        return getQuery(user, password, update.getQueryId());
    }

    protected Query deleteQuery(String user, String password, IQueryId queryId)
    {
        String sessionToken = v3api.login(user, password);

        QueryDeletionOptions options = new QueryDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteQueries(sessionToken, Arrays.asList(queryId), options);

        return getQuery(user, password, queryId);
    }

    protected TableModel executeQuery(String user, String password, IQueryId queryId, Map<String, String> parameters)
    {
        String sessionToken = v3api.login(user, password);

        QueryExecutionOptions options = new QueryExecutionOptions();
        options.withParameters(parameters);

        return v3api.executeQuery(sessionToken, queryId, options);
    }

    protected TableModel executeSql(String user, String password, String sql, IQueryDatabaseId databaseId, Map<String, String> parameters)
    {
        String sessionToken = v3api.login(user, password);

        SqlExecutionOptions options = new SqlExecutionOptions();
        options.withDatabaseId(databaseId);
        options.withParameters(parameters);

        return v3api.executeSql(sessionToken, sql, options);
    }

    @DataProvider(name = PROVIDER_TRUE_FALSE)
    protected java.lang.Object[][] provideBoolean()
    {
        return new java.lang.Object[][] { { true }, { false } };
    }

}
