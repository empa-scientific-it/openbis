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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.db.SqlUnitTestRunner;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.migration.SqlMigrationDataSourceFactory;
import ch.systemsx.cisd.dbmigration.postgresql.PostgreSQLDAOFactory;

/**
 * Test cases for database migration.
 *
 * @author Franz-Josef Elmer
 */
public final class SqlDataMigrationTest
{
    private static final String ORIGINAL_SQL_SOURCE = "source/sql";

    private static final String BASE_DATA_FOLDER_TEMPLATE = "sourceTest/sql/%s/";

    private String getDataFolder(final DatabaseConfigurationContext context)
    {
        return String.format(BASE_DATA_FOLDER_TEMPLATE, context.getDatabaseEngineCode())
                + DatabaseVersionHolder.getDatabaseVersion() + "/";
    }

    private String getTestDataFolder(final DatabaseConfigurationContext context)
    {
        return String.format(BASE_DATA_FOLDER_TEMPLATE, context.getDatabaseEngineCode())
                + "test_database_for_migration/";
    }

    private IDAOFactory createDAOFactory(final DatabaseConfigurationContext configurationContext)
    {
        return new PostgreSQLDAOFactory(configurationContext);
    }

    @Test(groups =
    { "slow" })
    /* runs unit tests written in SQL. The tests should check if the migration went fine. */
    public final void testMigration() throws Exception
    {
        final DatabaseConfigurationContext configurationContext =
                DatabaseCreationUtil.createDatabaseConfigurationContext("test_migration");
        final File migrationFolder =
                new File(getDataFolder(configurationContext) + SqlUnitTestRunner.MIGRATION_FOLDER);
        final File[] folders = migrationFolder.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.isDirectory() && pathname.getName().startsWith(".") == false;
                }
            });
        if (folders != null && folders.length != 0)
        {
            for (final File folder : folders)
            {
                final String databaseVersion = folder.getName();
                System.out.println("Test migration from database version " + databaseVersion
                        + " to " + DatabaseVersionHolder.getDatabaseVersion());
                try
                {
                    configurationContext.setCreateFromScratch(true);
                    configurationContext.initDataSourceFactory(new SqlMigrationDataSourceFactory());
                    configurationContext.setScriptFolder(getTestDataFolder(configurationContext));
                    DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(
                            configurationContext, databaseVersion, "000", "000");
                    configurationContext.closeConnections();
                    configurationContext.setCreateFromScratch(false);
                    configurationContext.setScriptFolder(ORIGINAL_SQL_SOURCE);
                    DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(
                            configurationContext, DatabaseVersionHolder.getDatabaseVersion(), "000", "000");
                    final ISqlScriptExecutor executor =
                            createDAOFactory(configurationContext).getSqlScriptExecutor();
                    new SqlUnitTestRunner(executor, new PrintWriter(System.out, true)).run(folder);
                } finally
                {
                    configurationContext.closeConnections();
                }
            }
        }
    }
}
