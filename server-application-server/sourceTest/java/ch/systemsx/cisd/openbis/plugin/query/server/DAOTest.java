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

package ch.systemsx.cisd.openbis.plugin.query.server;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
@Test(groups = "db")
public class DAOTest extends AbstractTransactionalTestNGSpringContextTests
{
    static
    {
        TestInitializer.init();
    }

    @Test
    public void testQuery()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where id < 6 order by id";
        testQueryWithBindings(query, null);
    }

    @Test
    public void testQueryWithBindings()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where id < ${id} order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("id", "6");
        testQueryWithBindings(query, bindings);
    }

    private void testQueryWithBindings(String query, QueryParameterBindings bindingsOrNull)
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind("test");
        DataSource dataSource = context.getDataSource();
        DAO dao = new DAO(dataSource);
        TableModel model = dao.query(query, bindingsOrNull);
        List<TableModelColumnHeader> headers = model.getHeader();
        assertColumnHeader("id", DataTypeCode.INTEGER, headers.get(0));
        assertColumnHeader("Data Set", DataTypeCode.VARCHAR, headers.get(1));
        assertEquals(EntityKind.DATA_SET, headers.get(1).tryGetEntityKind());
        assertColumnHeader("registration_timestamp", DataTypeCode.TIMESTAMP, headers.get(2));
        assertColumnHeader("is_valid", DataTypeCode.VARCHAR, headers.get(3));
        assertEquals(4, headers.size());
        List<TableModelRow> rows = model.getRows();
        // NOTE: only 2 data sets should be found even though there is a data set with id 2
        // but it is deleted
        assertRow("4\t20081105092159188-3\tWed Nov 05 09:21:59 CET 2008\ttrue", rows.get(0));
        assertRow("5\t20081105092159111-1\tMon Feb 09 12:20:21 CET 2009\ttrue", rows.get(1));
        assertEquals(2, rows.size());
    }

    @Test
    public void testQueryWithStringBinding()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code like ${code} order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("code", "200811050921591%");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithStringBindingExplicitType()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code like ${code::string} order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("code", "200811050921591%");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithStringBindingOldStyle()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code like '${code}' order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("code", "200811050921591%");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithPostgreSQLArrayBinding()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code = any(${codes}::text[]) order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("codes", "{20081105092159188-3, 20081105092159111-1}");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithPostgreSQLArrayBindingOldStyle()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code = any('{${codes}}'::text[]) order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("codes", "20081105092159188-3, 20081105092159111-1");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithPostgreSQLArrayBindingSimplified()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where code = any({${codes}}) order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("codes", "20081105092159188-3, 20081105092159111-1");
        testQueryWithBindings(query, bindings);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testQueryWithBindingsSQLInjection()
    {
        String query =
                "select id, code as DATA_SET_KEY, registration_timestamp, is_valid from data where id < ${id} order by id";
        QueryParameterBindings bindings = new QueryParameterBindings();
        bindings.addBinding("id",
                "6 union select id, user_id, registration_timestamp, is_active from persons");
        testQueryWithBindings(query, bindings);
    }

    @Test
    public void testQueryWithSpecialCharacters()
    {
        String query = "select code as \"exp.[code]\" from experiments order by id";

        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind("test");
        DataSource dataSource = context.getDataSource();
        DAO dao = new DAO(dataSource);
        TableModel model = dao.query(query, null);
        List<TableModelColumnHeader> headers = model.getHeader();
        assertColumnHeader("exp.[code]", DataTypeCode.VARCHAR, headers.get(0));
        assertEquals(1, headers.size());
        List<TableModelRow> rows = model.getRows();
        assertRow("EXP1", rows.get(0));
    }

    void assertColumnHeader(String expectedTitle, DataTypeCode expectedDataType,
            TableModelColumnHeader header)
    {
        assertEquals(expectedTitle, header.getTitle());
        assertEquals("Data type of '" + header.getTitle() + "'", expectedDataType,
                header.getDataType());
    }

    void assertRow(String expectedRow, TableModelRow row)
    {
        StringBuilder builder = new StringBuilder();
        List<ISerializableComparable> values = row.getValues();
        for (ISerializableComparable value : values)
        {
            builder.append(value).append("\t");
        }
        assertEquals(expectedRow, builder.toString().trim());
    }
}
