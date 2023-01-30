/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class DataSourceDefinitionTest extends AssertJUnit
{
    @Test
    public void testToString()
    {
        DataSourceDefinition definition = new DataSourceDefinition();
        definition.setCode("ABC");
        definition.setDriverClassName("my.driver");
        definition.setPassword("</[?a&\"'");

        assertEquals("code=ABC\tdriverClassName=my.driver\tpassword=</[?a&\"'\t",
                definition.toString());
    }

    @Test
    public void testFromString()
    {
        String example = "code=Alpha\thostPart=abc:8889\tsid=my_db\tpassword=1=2^2&<7>?\"abc\"\t";

        DataSourceDefinition definition = DataSourceDefinition.fromString(example);

        assertEquals("Alpha", definition.getCode());
        assertEquals("my_db", definition.getSid());
        assertEquals(null, definition.getDriverClassName());
        assertEquals("abc:8889", definition.getHostPart());
        assertEquals(null, definition.getUsername());
        assertEquals("1=2^2&<7>?\"abc\"", definition.getPassword());
        assertEquals(example, definition.toString());
    }

    @Test
    public void testListFromString()
    {
        String examples = "code=a\tsid=my_db\t\ncode=b\tusername=einstein\t\n";

        List<DataSourceDefinition> definitions = DataSourceDefinition.listFromString(examples);

        assertEquals("[code=a\tsid=my_db\t, code=b\tusername=einstein\t]", definitions.toString());
        assertEquals(examples, DataSourceDefinition.toString(definitions));
    }

    @Test
    public void testListFromEmptyString()
    {
        List<DataSourceDefinition> definitions = DataSourceDefinition.listFromString("");

        assertEquals("[]", definitions.toString());
    }

    @Test
    public void testCreateFromContext()
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setUrlHostPart("my-db:4711");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind("dev");
        context.setOwner("albert");
        context.setPassword("Einstein");
        DataSourceDefinition definition = DataSourceDefinition.createFromContext(context);

        assertEquals(String.format("driverClassName=org.postgresql.Driver\thostPart=%s\t"
                + "sid=openbis_dev\tusername=albert\tpassword=Einstein\t", context.getUrlHostPart()), definition.toString());
    }
}
