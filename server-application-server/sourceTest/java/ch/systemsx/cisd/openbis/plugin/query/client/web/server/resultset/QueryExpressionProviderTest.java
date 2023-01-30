/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.server.resultset;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractProviderTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * @author Franz-Josef Elmer
 */
public class QueryExpressionProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        final QueryExpression qe1 = new QueryExpression();
        qe1.setName("MY-QUERY");
        qe1.setDescription("my query");
        qe1.setExpression("select * from something");
        qe1.setEntityTypeCode("MY_TYPE");
        qe1.setPublic(true);
        qe1.setQueryDatabase(new QueryDatabase("DB1", "My DB"));
        qe1.setQueryType(QueryType.EXPERIMENT);
        qe1.setRegistrationDate(new Date(4711));
        qe1.setRegistrator(new PersonBuilder().name("Albert", "Einstein").getPerson());
        qe1.setModificationDate(new Date(5711));
        final IQueryServer queryServer = context.mock(IQueryServer.class);
        context.checking(new Expectations()
            {
                {
                    one(queryServer).listQueries(SESSION_TOKEN, QueryType.UNSPECIFIED,
                            BasicEntityType.UNSPECIFIED);
                    will(returnValue(Arrays.asList(qe1)));
                }
            });

        QueryExpressionProvider provider = new QueryExpressionProvider(queryServer, SESSION_TOKEN);
        TypedTableModel<QueryExpression> model = provider.getTableModel(100);

        assertEquals("[NAME, DESCRIPTION, SQL_QUERY, IS_PUBLIC, QUERY_TYPE, ENTITY_TYPE, "
                + "QUERY_DATABASE, REGISTRATOR, REGISTRATION_DATE, MODIFICATION_DATE]",
                getHeaderIDs(model).toString());
        assertEquals("[null, null, null, null, null, null, null, null, null, null]",
                getHeaderEntityKinds(model).toString());
        assertEquals("[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, "
                + "VARCHAR, VARCHAR, TIMESTAMP, TIMESTAMP]", getHeaderDataTypes(model).toString());
        List<TableModelRowWithObject<QueryExpression>> rows = model.getRows();
        assertSame(qe1, rows.get(0).getObjectOrNull());
        assertEquals(
                "[MY-QUERY, my query, select * from something, yes, EXPERIMENT, MY_TYPE, "
                        + "My DB, Einstein, Albert, Thu Jan 01 01:00:04 CET 1970, Thu Jan 01 01:00:05 CET 1970]",
                rows.get(0).getValues().toString());
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }
}
