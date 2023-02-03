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

package ch.systemsx.cisd.dbmigration.migration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import cz.startnet.utils.pgdiff.PgDiff;
import cz.startnet.utils.pgdiff.PgDiffArguments;

import static ch.systemsx.cisd.dbmigration.DBMigrationEngine.FULL_TEXT_SEARCH_DOCUMENT_VERSION_FILE_PATH;

/**
 * Test cases for database migration.
 *
 * @author Piotr Kupczyk
 */
public abstract class SqlMigrationTestAbstract
{

    private static final int CHECK_NUMBER_OF_MIGRATIONS = 30;

    private File sqlScriptOutputDirectory;

    protected abstract String getSqlScriptInputDirectory();

    protected abstract String getSqlScriptOutputDirectory();

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest() throws Exception
    {
        sqlScriptOutputDirectory = new File(getSqlScriptOutputDirectory());
        if (!sqlScriptOutputDirectory.exists())
        {
            sqlScriptOutputDirectory.mkdir();
        }
    }

    @AfterTest(alwaysRun = true)
    public void afterTest() throws Exception
    {
        if (sqlScriptOutputDirectory != null && sqlScriptOutputDirectory.exists())
        {
            FileUtils.deleteDirectory(sqlScriptOutputDirectory);
        }
    }

    public void testMigration(final String newestVersionString, final String newestFullTextSearchVersionString, final String newestPatchesVersionString)
    {
        new File(FULL_TEXT_SEARCH_DOCUMENT_VERSION_FILE_PATH).delete();

        SqlMigrationVersion newestVersion = new SqlMigrationVersion(newestVersionString);
        SqlMigrationVersion firstVersion =
                new SqlMigrationVersion(Math.max(1, newestVersion.getVersionInt() - CHECK_NUMBER_OF_MIGRATIONS));

        DatabaseConfigurationContext migrationContext = null;
        DatabaseConfigurationContext scratchContext = null;

        try
        {
            // create first version of the migration database
            migrationContext = createMigrationDatabaseContext(true);
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(migrationContext,
                    firstVersion.getVersionString(), newestFullTextSearchVersionString, newestPatchesVersionString);

            // migrate the migration database to the newest version
            migrationContext.setCreateFromScratch(false);
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(migrationContext,
                    newestVersion.getVersionString(), newestFullTextSearchVersionString, newestPatchesVersionString);
            dumpDatabaseSchema(migrationContext, getMigratedDatabaseSchemaFile());

            // create the scratch database with the newest version
            scratchContext = createScratchDatabaseContext();
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(scratchContext,
                    newestVersion.getVersionString(), newestFullTextSearchVersionString, newestPatchesVersionString);
            dumpDatabaseSchema(scratchContext, getScratchDatabaseSchemaFile());

            // check migration and scratch databases are equal
            assertDatabaseSchemasEqual(getMigratedDatabaseSchemaFile(),
                    getScratchDatabaseSchemaFile());

        } finally
        {
            if (migrationContext != null)
            {
                migrationContext.closeConnections();
            }
            if (scratchContext != null)
            {
                scratchContext.closeConnections();
            }
        }
    }

    private DatabaseConfigurationContext createDatabaseContext(String dbKind,
            boolean createFromScratch)
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind(dbKind);
        context.setScriptFolder(getSqlScriptInputDirectory());
        context.initDataSourceFactory(new SqlMigrationDataSourceFactory());
        context.setCreateFromScratch(createFromScratch);
        return context;
    }

    private DatabaseConfigurationContext createMigrationDatabaseContext(boolean createFromScratch)
    {
        return createDatabaseContext("test_migration_migrated", createFromScratch);
    }

    private DatabaseConfigurationContext createScratchDatabaseContext()
    {
        return createDatabaseContext("test_migration_scratch", true);
    }

    private File getMigratedDatabaseSchemaFile()
    {
        return new File(sqlScriptOutputDirectory, "migratedDatabaseSchema.sql");
    }

    private File getScratchDatabaseSchemaFile()
    {
        return new File(sqlScriptOutputDirectory, "scratchDatabaseSchema.sql");
    }

    private void dumpDatabaseSchema(final DatabaseConfigurationContext configurationContext,
            final File migratedSchemaFile)
    {
        final boolean dumpSuccessful =
                DumpPreparator.createDatabaseSchemaDump(configurationContext.getDatabaseName(),
                        migratedSchemaFile);
        AssertJUnit.assertTrue("dump of db failed: " + configurationContext.getDatabaseName(),
                dumpSuccessful);
    }

    private void assertDatabaseSchemasEqual(final File currentSchemaFile,
            final File expectedSchemaFile)
    {
        final StringWriter writer = new StringWriter();
        final PgDiffArguments arguments = new PgDiffArguments();
        arguments.setOldDumpFile(currentSchemaFile.getAbsolutePath());
        arguments.setNewDumpFile(expectedSchemaFile.getAbsolutePath());
        arguments.setIgnoreFunctionWhitespace(true);
        arguments.setIgnoreStartWith(true);
        arguments.setOutputIgnoredStatements(true);
        PgDiff.createDiff(new PrintWriter(writer), arguments);

        String diff = writer.toString();
        if (diff == null)
        {
            diff = "";
        }
        String delta = diff.substring(0, diff.indexOf("/* Original")).trim();

        AssertJUnit.assertEquals("The migrated schema is not identical to the scratch one. "
                + "Consider attaching following script to the migration file.", "", delta);

        List<String> originalDb = new ArrayList<String>();
        List<String> newDb = new ArrayList<String>();

        Scanner scanner = new Scanner(diff);
        List<String> current = originalDb;
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.contains("New database ignored statements"))
            {
                current = newDb;
            }
            if (line.length() == 0 ||
                    line.startsWith("/*") ||
                    line.startsWith("*/") ||
                    line.startsWith("GRANT") ||
                    line.startsWith("REVOKE"))
            {
                continue;
            } else
            {
                current.add(line);
            }
        }
        scanner.close();

        Iterator<String> origIter = originalDb.iterator();
        Iterator<String> newIter = newDb.iterator();

        while (true)
        {
            if (!origIter.hasNext() && !newIter.hasNext())
            {
                break;
            }
            if (!origIter.hasNext())
            {
                String additional = contentOf(newIter);
                AssertJUnit.fail("Only in from-scratch schema: " + additional);
            }

            if (!newIter.hasNext())
            {
                String additional = contentOf(origIter);
                AssertJUnit.fail("Only in migrated schema: " + additional);
            }
            String origValue = origIter.next();
            String newValue = newIter.next();
            if (!origValue.equals(newValue))
            {
                System.out.println(diff);
                AssertJUnit.fail("There's a difference between the migrated schema and the from-scratch schema\nmigrated: " + origValue
                        + "\nfrom-scratch: " + newValue);
            }
        }
    }

    private String contentOf(Iterator<String> newIter)
    {
        String s = "";
        while (newIter.hasNext())
        {
            s += newIter.next() + "\n";
        }
        return s;
    }

}
