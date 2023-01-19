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
import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.ISqlScriptProvider;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;

import static ch.systemsx.cisd.dbmigration.DBMigrationEngine.FULL_TEXT_SEARCH_DOCUMENT_VERSION_FILE_PATH;

/**
 * Utility methods around database creation.
 *
 * @author Franz-Josef Elmer
 */
public final class DatabaseCreationUtil
{
    private static BeanFactory beanFactory;

    static
    {
        LogInitializer.init();
    }

    private DatabaseCreationUtil()
    {
        // This class can not be instantiated.
    }

    private final static BeanFactory getBeanFactory()
    {
        if (beanFactory == null)
        {
            final AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                    { "dbConfigurationContext.xml" }, true);
            DatabaseCreationUtil.beanFactory = applicationContext;
        }
        return beanFactory;
    }

    /**
     * Creates all files in <code>sourceTest/sql/postgresql</code> necessary to set up a database of the current version by dumping a database
     * migrated from the specified version.
     */
    private static void createFilesFromADumpOfAMigratedDatabase(final String databaseVersion,
            final String fullTextSearchDocumentVersion, final String releasePatchesVersion)
            throws Exception
    {
        final String databaseKind = "migration_dump";
        final DatabaseConfigurationContext context =
                createDatabaseConfigurationContext(databaseKind);
        context.setCreateFromScratch(true);
        new File(FULL_TEXT_SEARCH_DOCUMENT_VERSION_FILE_PATH).delete();
        final ISqlScriptProvider scriptProvider =
                DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                        databaseVersion, fullTextSearchDocumentVersion, releasePatchesVersion);
        context.setCreateFromScratch(false);
        context.setScriptFolder("source/sql");
        new File(FULL_TEXT_SEARCH_DOCUMENT_VERSION_FILE_PATH).delete();
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                DatabaseVersionHolder.getDatabaseVersion(), fullTextSearchDocumentVersion, releasePatchesVersion);
        createDumpForJava(databaseKind,
                scriptProvider.getDumpFolder(DatabaseVersionHolder.getDatabaseVersion()));
        scriptProvider.markAsDumpRestorable(DatabaseVersionHolder.getDatabaseVersion());
    }

    /**
     * Creates all files in <var>destinationDir</var> necessary to set up the database <var>databaseKind</var> in its current state.
     */
    private static final void createDumpForJava(final String databaseKind, final File destinationDir)
            throws IOException
    {
        final String dataBaseName = "openbis_" + databaseKind;
        final File dumpFile = new File("targets/dump.sql");
        final boolean ok = DumpPreparator.createDatabaseDump(dataBaseName, dumpFile);
        if (ok == false)
        {
            throw new EnvironmentFailureException("Database dump failed.");
        }
        DumpPreparator.createUploadFiles(dumpFile, destinationDir, false);
    }

    /**
     * Creates a database configuration based on <code>dbConfigurationContext.xml</code>.
     */
    public final static DatabaseConfigurationContext createDatabaseConfigurationContext(
            final String databaseKind)
    {
        final BeanFactory factory = getBeanFactory();
        final DatabaseConfigurationContext configurationContext = (DatabaseConfigurationContext) factory.getBean("db-configuration-context");
        configurationContext.setDatabaseKind(databaseKind);
        configurationContext.setScriptFolder("sourceTest/sql");
        return configurationContext;
    }

    //
    // Main method
    //

    public static void main(final String[] args) throws Exception
    {
        String sourceDbVersion;
        if (args.length == 0)
        {
            sourceDbVersion = getPreviousVersion(DatabaseVersionHolder.getDatabaseVersion());
            System.out.println("Migrating from the previous database version " + sourceDbVersion);
        } else if (args.length == 1)
        {
            sourceDbVersion = args[0];
        } else
        {
            System.out.println("Usage: java " + DatabaseCreationUtil.class.getName()
                    + "[<database version>]");
            System.exit(1);
            return; // never executed
        }
        createFilesFromADumpOfAMigratedDatabase(sourceDbVersion,
                DatabaseVersionHolder.getDatabaseFullTextSearchDocumentVersion(),
                DatabaseVersionHolder.getReleasePatchesVersion());
    }

    private static String getPreviousVersion(final String currentVersion)
    {
        return String.format("%03d", Integer.parseInt(currentVersion) - 1);
    }
}
