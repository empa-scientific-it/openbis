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

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.annotations.Test;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
@TransactionConfiguration(transactionManager = "transaction-manager")
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
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind("test");
        DataSource dataSource = context.getDataSource();
        DAO dao = new DAO(dataSource);
        TableModel model =
                dao.query("select id, code as DATA_SET_KEY, registration_timestamp, is_valid "
                        + "from data where id < 5 order by id");
        List<TableModelColumnHeader> headers = model.getHeader();
        assertColumnHeader("id", DataTypeCode.INTEGER, headers.get(0));
        assertColumnHeader("data_set_key", DataTypeCode.VARCHAR, headers.get(1));
        assertColumnHeader("registration_timestamp", DataTypeCode.VARCHAR, headers.get(2));
        assertColumnHeader("is_valid", DataTypeCode.VARCHAR, headers.get(3));
        assertEquals(4, headers.size());
        List<TableModelRow> rows = model.getRows();
        assertRow("2\t20081105092158673-1\t2008-11-05 09:21:58.798\ttrue", rows.get(0));
        assertRow("4\t20081105092159188-3\t2008-11-05 09:21:59.313\ttrue", rows.get(1));
        assertEquals(2, rows.size());
    }
    
    void assertColumnHeader(String expectedTitle, DataTypeCode expectedDataType, TableModelColumnHeader header)
    {
        assertEquals(expectedTitle, header.getTitle());
        assertEquals("Data type of '" + header.getTitle() + "'", expectedDataType, header.getDataType());
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
