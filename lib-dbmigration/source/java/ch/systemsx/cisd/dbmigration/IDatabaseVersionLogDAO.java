/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.db.ISqlScriptExecutionLogger;
import ch.systemsx.cisd.common.db.Script;

/**
 * Interface for a Data Access Object for the database version log.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDatabaseVersionLogDAO extends ISqlScriptExecutionLogger
{
    /**
     * Returns <code>true</code> if the database instance with database version log can be connected.
     */
    public boolean canConnectToDatabase();

    /**
     * Creates the table DATABASE_VERSION_LOG by using the specified script.
     */
    public void createTable(Script script);

    /**
     * Returns the last log entry found.
     * 
     * @return <code>null</code> if log is empty.
     */
    public LogEntry getLastEntry();

}
