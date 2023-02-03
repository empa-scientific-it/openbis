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

package ch.systemsx.cisd.dbmigration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.reflection.AbstractHashable;

/**
 * Immutable class for some meta data of a database. It holds the version of the database and a list of {@link SimpleTableMetaData} for all tables.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleDatabaseMetaData extends AbstractHashable
{

    private final String databaseVersion;

    private final List<SimpleTableMetaData> tables;

    private final Map<String, SimpleTableMetaData> tableDictonary;

    /**
     * Creates an instance for the specified version and tables.
     */
    public SimpleDatabaseMetaData(String databaseVersion, List<SimpleTableMetaData> tables)
    {
        this.databaseVersion = databaseVersion;
        tableDictonary = new HashMap<String, SimpleTableMetaData>();
        for (SimpleTableMetaData simpleTableMetaData : tables)
        {
            tableDictonary.put(simpleTableMetaData.getTableName(), simpleTableMetaData);
        }
        this.tables = Collections.unmodifiableList(tables);
    }

    /**
     * Returns the version of the database.
     */
    public final String getDatabaseVersion()
    {
        return databaseVersion;
    }

    /**
     * Returns simple meta data for all tables.
     */
    public final List<SimpleTableMetaData> getTables()
    {
        return tables;
    }

    /**
     * Returns the table meta data for the specified table name (upper and lower cases ignored).
     * 
     * @return <code>null</code> if not found.
     */
    public final SimpleTableMetaData tryToGetTableMetaData(String tableName)
    {
        return tableDictonary.get(tableName.toUpperCase());
    }

}
