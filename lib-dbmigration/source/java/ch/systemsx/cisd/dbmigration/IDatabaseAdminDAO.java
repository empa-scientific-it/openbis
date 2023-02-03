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

import java.io.File;

/**
 * Interface for administration of a database.
 *
 * @author Franz-Josef Elmer
 */
public interface IDatabaseAdminDAO
{
    /**
     * Returns the version of the database server.
     */
    public String getDatabaseServerVersion();

    /**
     * Returns the name of the database to be created / dropped.
     */
    public String getDatabaseName();

    /**
     * Returns the complete URL of the database to be created / dropped.
     */
    public String getDatabaseURL();

    /**
     * Returns meta data and maximum primary keys of all tables.
     */
    public DatabaseDefinition getDatabaseDefinition();

    /**
     * Creates the owner/user of the database. Implementation should handle the case of already existing owner/user gracefully.
     */
    public void createOwner();

    /**
     * Creates the groups that should be granted read-only and read-write access to all objects in this database. Implementation should handle the
     * case of already existing group gracefully.
     */
    public void createGroups();

    /**
     * Creates the database and the 'database_version_logs' table.
     */
    public void createDatabase();

    /**
     * Restores the database from previously created dump.
     */
    public void restoreDatabaseFromDump(File dumpFolder, String version);

    /**
     * Applies scripts for full text search.
     *  @param scriptProvider script provider.
     * @param version version of scripts.
     * @param applyMainScript if {@code true} the main script should be applied, otherwise only before and after scripts are applied.
     */
    void applyFullTextSearchScripts(ISqlScriptProvider scriptProvider, String version, final boolean applyMainScript);

    /**
     * Drops the database.
     */
    public void dropDatabase();

    /**
     * Initialize error codes for this database.
     */
    public void initializeErrorCodes();

}
