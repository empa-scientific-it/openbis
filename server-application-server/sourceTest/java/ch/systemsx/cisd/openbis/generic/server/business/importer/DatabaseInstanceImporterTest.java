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

package ch.systemsx.cisd.openbis.generic.server.business.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.dbmigration.DatabaseDefinition;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.TableColumnDefinition;
import ch.systemsx.cisd.dbmigration.TableDefinition;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
{ IDatabaseDumper.class, Parameters.class })
public class DatabaseInstanceImporterTest extends AbstractFileSystemTestCase
{
    private static final String UUID1 = "DBE9F7D0-66F5-40EB-A5CC-A9D6DCDE454";

    private static final String UUID2 = "ABE9F7D0-66F5-40EB-A5CC-A9D6DCDE454";

    private static final String EXAMPLE_DATABASE = "test_database";

    private static final String EXAMPLE =
            "SET client_encoding = 'UTF8';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "COPY public.database_version_logs (db_version, module_name, run_status) FROM stdin;\n"
                    + "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                    + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "\n"
                    + "COPY public.database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;\n"
                    + "1\tCISD\t%sC\tt\t2008-08-13 10:06:08.49+02\n" + "\\.\n" + "\n"
                    + "COPY public.property_types (id, code, daty_id, dbin_id) FROM stdin;\n"
                    + "1\tDESCRIPTION\t1\t1\n" + "2\tAGE\t2\t1\n" + "\\.\n"
                    + "COPY public.property_values (id, value, prty_id) FROM stdin;\n" + "1\thello\t1\n"
                    + "2\t42\t2\n" + "3\tworld\t1\n" + "\\.\n" + "\n"
                    + "ALTER TABLE ONLY data_all\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n";

    private static final String EXAMPLE_WITH_CYCLCIC_ROTATED_COLUMNS =
            "SET client_encoding = 'UTF8';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "COPY public.database_version_logs (db_version, module_name, run_status) FROM stdin;\n"
                    + "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                    + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "\n"
                    + "COPY public.database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;\n"
                    + "1\tCISD\t%sC\tt\t2008-08-13 10:06:08.49+02\n" + "\\.\n" + "\n"
                    + "COPY public.property_types (id, code, daty_id, dbin_id) FROM stdin;\n"
                    + "1\tDESCRIPTION\t1\t1\n" + "2\tAGE\t2\t1\n" + "\\.\n"
                    + "COPY public.property_values (prty_id, id, value) FROM stdin;\n" + "1\t1\thello\n"
                    + "2\t2\t42\n" + "1\t3\tworld\n" + "\\.\n" + "\n"
                    + "ALTER TABLE ONLY data_all\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n";

    private static final String EXAMPLE_WITH_MISSING_DATABASE_VERSION_LOG =
            "SET client_encoding = 'UTF8';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "\n"
                    + "COPY public.database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;\n"
                    + "1\tCISD\t%sC\tt\t2008-08-13 10:06:08.49+02\n" + "\\.\n" + "\n"
                    + "ALTER TABLE ONLY data_all\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n";

    private static final String EXAMPLE_WITH_MISSING_DATABASE_INSTANCES =
            "SET client_encoding = 'UTF8';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number\n"
                    + "\\.\n"
                    + "COPY public.database_version_logs (db_version, module_name, run_status) FROM stdin;\n"
                    + "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                    + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n"
                    + "\\.\n" + "\n" + "\n"
                    + "ALTER TABLE ONLY data_all\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n";

    private static final String EXAMPLE_WITH_MISSING_DATABASE_INSTANCES2 =
            "SET client_encoding = 'UTF8';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number\n"
                    + "\\.\n"
                    + "COPY public.database_version_logs (db_version, module_name, run_status) FROM stdin;\n"
                    + "012\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                    + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n"
                    + "\\.\n" + "\n" + "\n"
                    + "ALTER TABLE ONLY data_all\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n";

    private static final DatabaseDefinition EXAMPLE_META_DATA = createDatabaseDefinition();

    private static DatabaseDefinition createDatabaseDefinition()
    {
        DatabaseDefinition definition = new DatabaseDefinition();
        definition.add(createTable("data_types", "id", 3, "location", "description"));
        definition.add(createTable("database_version_logs", null, 0, "db_version", "module_name",
                "run_status"));
        definition.add(createTable("database_instances", "id", 1, "uuid", "is_original_source",
                "registration_timestamp"));
        definition.add(createTable("property_types", "id", 2, "code", "daty_id", "dbin_id"));
        definition.add(createTable("property_values", "id", 3, "value", "prty_id"));
        definition.connect("data_types", "id", "property_types", "daty_id");
        definition.connect("database_instances", "id", "property_types", "dbin_id");
        definition.connect("property_types", "id", "property_values", "prty_id");
        return definition;
    }

    private static TableDefinition createTable(String tableName, String primaryKeyOrNull,
            long largestPrimaryKeyValue, String... columnNames)
    {
        TableDefinition table = new TableDefinition(tableName);
        if (primaryKeyOrNull != null)
        {
            addColumn(table, primaryKeyOrNull).setLargestPrimaryKey(largestPrimaryKeyValue);
            table.defineColumnAsPrimaryKey(primaryKeyOrNull);
        }
        for (String columnName : columnNames)
        {
            addColumn(table, columnName);
        }
        return table;
    }

    private static TableColumnDefinition addColumn(TableDefinition table, String columnName)
    {
        TableColumnDefinition columnDefinition = new TableColumnDefinition(table);
        columnDefinition.setColumnName(columnName);
        table.add(columnDefinition);
        return columnDefinition;
    }

    private Mockery context;

    private IDatabaseAdminDAO exportDAO;

    private IDatabaseAdminDAO uploadDAO;

    private IExitHandler exitHandler;

    private File dumpFile;

    private IDatabaseDumper databaseDumper;

    private File uploadFolder;

    private File dbDumpFile;

    private File currentDatabaseFolder;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        exportDAO = context.mock(IDatabaseAdminDAO.class, "export DAO");
        uploadDAO = context.mock(IDatabaseAdminDAO.class, "upload DAO");
        exitHandler = context.mock(IExitHandler.class);
        databaseDumper = context.mock(IDatabaseDumper.class);
        dumpFile = new File(workingDirectory, "dump.sql");
        uploadFolder = new File(workingDirectory, "upload-folder");
        dbDumpFile = new File(uploadFolder, "db_dump.sql");
        currentDatabaseFolder = new File(uploadFolder, "current-database");
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSameUUID()
    {
        createDump(dbDumpFile, UUID1);
        createDump(dumpFile, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter();
        try
        {
            importer.importDatabase();
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Database instance 'CISD' couldn't be imported because it has "
                    + "the same UUID as already existing database instance 'CISD'.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testSameCode()
    {
        createDump(dbDumpFile, UUID2);
        createDump(dumpFile, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter();
        try
        {
            importer.importDatabase();
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("There is already a database instance with code 'CISD'. "
                    + "Please, choose another code with the command line option '-d'.",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingDatabaseVersionLogInCurrentDatabase()
    {
        createDump(dbDumpFile, EXAMPLE_WITH_MISSING_DATABASE_VERSION_LOG, UUID2);
        createDump(dumpFile, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        try
        {
            importer.importDatabase();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex)
        {
            assertEquals("Table 'database_version_logs' missing.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingDatabaseVersionLogInDatabaseToBeImported()
    {
        createDump(dbDumpFile, UUID2);
        createDump(dumpFile, EXAMPLE_WITH_MISSING_DATABASE_VERSION_LOG, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        try
        {
            importer.importDatabase();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex)
        {
            assertEquals("Table 'database_version_logs' missing.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDifferentDatabasesVersions()
    {
        createDump(dbDumpFile, EXAMPLE_WITH_MISSING_DATABASE_INSTANCES, UUID2);
        createDump(dumpFile, EXAMPLE_WITH_MISSING_DATABASE_INSTANCES2, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        try
        {
            importer.importDatabase();
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Version of current database is 011 which does not match "
                    + "the version of the database to be imported: 012", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testIncompatibleDatabases()
    {
        createDump(dbDumpFile, EXAMPLE_WITH_MISSING_DATABASE_INSTANCES, UUID2);
        createDump(dumpFile, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        try
        {
            importer.importDatabase();
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String errMessagePattern =
                    "Current database does not have tables \\[(.*)]\n"
                            + " which exist in the database to be imported.";

            Pattern pattern = Pattern.compile(errMessagePattern);
            Matcher matcher = pattern.matcher(ex.getMessage());
            assertTrue(matcher.matches());

            String[] tableNames = matcher.group(1).split(", ");
            List<String> tableNamesList = Arrays.asList(tableNames);
            Collections.sort(tableNamesList);
            assertEquals("[DATABASE_INSTANCES, PROPERTY_TYPES, PROPERTY_VALUES]",
                    tableNamesList.toString());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingDatabaseInstances()
    {
        createDump(dbDumpFile, EXAMPLE_WITH_MISSING_DATABASE_INSTANCES, UUID2);
        createDump(dumpFile, EXAMPLE_WITH_MISSING_DATABASE_INSTANCES, UUID1);
        prepareForCreatingDumpFile();

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        try
        {
            importer.importDatabase();
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Can not find table 'database_instances' in current-database",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCodeDefinedAsOption()
    {
        createDump(dbDumpFile, UUID2);
        createDump(dumpFile, EXAMPLE_WITH_CYCLCIC_ROTATED_COLUMNS, UUID1);
        prepareForCreatingDumpFile();
        context.checking(new Expectations()
            {
                {
                    one(exportDAO).getDatabaseDefinition();
                    will(returnValue(EXAMPLE_META_DATA));

                    one(uploadDAO).dropDatabase();
                    one(uploadDAO).restoreDatabaseFromDump(
                            with(new FileMatcher(currentDatabaseFolder)), with(equal("011")));
                }
            });

        DatabaseInstanceImporter importer = createImporter("-d", "my-db");
        importer.importDatabase();

        File[] files = currentDatabaseFolder.listFiles();
        Arrays.sort(files);
        checkTabFile(files[0], "data_types", "1\tVARCHAR\tVariable length character",
                "2\tINTEGER\tInteger", "3\tREAL\tReal number");
        checkTabFile(files[1], "database_version_logs",
                "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25",
                "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53");
        checkTabFile(files[2], "database_instances", "1\tCISD\t" + UUID2
                + "C\tt\t2008-08-13 10:06:08.49+02", "2\tmy-db\t" + UUID1
                + "C\tf\t2008-08-13 10:06:08.49+02");
        checkTabFile(files[3], "property_types", "1\tDESCRIPTION\t1\t1", "2\tAGE\t2\t1",
                "3\tDESCRIPTION\t1\t2", "4\tAGE\t2\t2");
        checkTabFile(files[4], "property_values", "1\thello\t1", "2\t42\t2", "3\tworld\t1",
                "4\thello\t3", "5\t42\t4", "6\tworld\t3");
        assertEquals("finish-011.sql", files[5].getName());
        checkFileContent(files[5], "ALTER TABLE ONLY data_all", "    ADD CONSTRAINT data_pk PRIMARY KEY (id);", "");
        assertEquals("schema-011.sql", files[6].getName());
        checkFileContent(files[6], "SET standard_conforming_strings = off;", "");

        context.assertIsSatisfied();
    }

    private void checkTabFile(File tabFile, String expectedTableName, String... expectedContent)
    {
        String tabFileName = tabFile.getName();
        assertEquals("Expected '" + expectedTableName + "' instead of " + tabFile, true,
                tabFileName.endsWith(expectedTableName + ".tsv"));
        checkFileContent(tabFile, expectedContent);
    }

    private void checkFileContent(File file, String... expectedContent)
    {
        List<String> lines = FileUtilities.loadToStringList(file);
        for (int i = 0; i < expectedContent.length; i++)
        {
            assertEquals("Line " + (i + 1), expectedContent[i], lines.get(i));
        }
        if (lines.size() > expectedContent.length)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = expectedContent.length; i < lines.size(); i++)
            {
                builder.append('\n').append(i + 1).append(lines.get(i));
            }
            fail("Unexpected content: " + builder.toString());
        }
    }

    private void prepareForCreatingDumpFile()
    {
        context.checking(new Expectations()
            {
                {
                    one(exportDAO).getDatabaseName();
                    will(returnValue(EXAMPLE_DATABASE));

                    one(databaseDumper).createDatabaseDump(with(equal(EXAMPLE_DATABASE)),
                            with(new FileMatcher(dbDumpFile)));
                    will(returnValue(true));
                }
            });
    }

    private void createDump(File file, String uuid)
    {
        String template = EXAMPLE;
        createDump(file, template, uuid);
    }

    private void createDump(File file, String template, String uuid)
    {
        file.getParentFile().mkdirs();
        FileUtilities.writeToFile(file, String.format(template, uuid));
    }

    private DatabaseInstanceImporter createImporter(String... options)
    {
        ArrayList<String> args = new ArrayList<String>();
        args.add("--upload-folder");
        args.add(uploadFolder.getAbsolutePath());
        args.addAll(Arrays.asList(options));
        args.add("not-used");
        args.add(dumpFile.getAbsolutePath());
        Parameters parameters = new Parameters(args.toArray(new String[0]), exitHandler);
        return new DatabaseInstanceImporter(parameters, exportDAO, uploadDAO, databaseDumper);
    }

    private static class FileMatcher extends BaseMatcher<File>
    {
        private final File expectedFile;

        FileMatcher(File expectedFile)
        {
            this.expectedFile = expectedFile;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendValue(expectedFile);
        }

        @Override
        public boolean matches(Object item)
        {
            if (item instanceof File)
            {
                File file = (File) item;
                return expectedFile.getAbsolutePath().equals(file.getAbsolutePath());
            }
            return false;
        }

    }

}
