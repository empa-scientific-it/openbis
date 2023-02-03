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

import ch.systemsx.cisd.common.db.Script;

/**
 * Provider of SQL scripts for creation and migration of database.
 *
 * @author Franz-Josef Elmer
 */
public interface ISqlScriptProvider
{
    /**
     * Returns <code>true</code> if this script provider is suitable for a dump restore of the given <var>version</var> of the database, and
     * <code>false</code>, if it is suitable for a regular setup.
     */
    public boolean isDumpRestore(String version);

    /** marks that this script provider is suitable for a dump restore */
    public void markAsDumpRestorable(String version);

    /**
     * Returns the folder where all dump files for <var>version</var> reside.
     */
    public File getDumpFolder(String version);

    /**
     * Returns the folder where all scripts with the given version for full text search are located.
     */
    File getFullTextSearchScriptsFolder(String version);

    /**
     * Returns the script to create database schemas.
     *
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetSchemaScript(String version);

    /**
     * Returns the script to create the database functions.
     *
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetFunctionScript(String version);

    /**
     * Returns the script containing all domain definitions for the specified version. The name of the script is expected to be
     *
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/domains-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetDomainsScript(final String version);

    /**
     * Returns the script containing all grant declarations for the specified version. The name of the script is expected to be
     *
     * <pre>
     * &lt;schema script folder&gt;/&lt;version&gt;/grants-&lt;version&gt;.sql
     * </pre>
     */
    public Script tryGetGrantsScript(final String version);

    /**
     * Returns the script to create initial data.
     *
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetDataScript(String version);

    /**
     * Returns the migration script for migrating a database.
     *
     * @param fromVersion The version of the current database.
     * @param toVersion The version of the database after migration.
     * @return <code>null</code> if there isn't such a migration script.
     */
    public Script tryGetMigrationScript(String fromVersion, String toVersion);

    /**
     * Returns the function migration script for migrating a database. The function migration will always be called <i>after</i> the regular migration
     * script.
     *
     * @param fromVersion The version of the current database.
     * @param toVersion The version of the database after migration.
     * @return <code>null</code> if there isn't such a migration script.
     */
    public Script tryGetFunctionMigrationScript(String fromVersion, String toVersion);

    /**
     * Returns the triple of scripts for full text search.
     *
     * @param version full text search script version.
     * @return array with 3 scripts to be called in sequence.
     */
    Script[] tryGetFullTextSearchScripts(String version);

    /**
     * Returns a single patch script.
     *
     * @param version patch script script version.
     * @return one script.
     */
    Script tryGetReleasePatchScripts(String version);
}
