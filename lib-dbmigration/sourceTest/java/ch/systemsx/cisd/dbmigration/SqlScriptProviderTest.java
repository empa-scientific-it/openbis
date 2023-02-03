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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.Script;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Tests for {@link SqlScriptExecutor}.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlScriptProviderTest
{
    private static final String BASE_FOLDER =
            "targets" + File.separator + "unit-test-wd" + File.separator + "SqlScriptProviderTest";

    private static final String TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME =
            BASE_FOLDER + File.separator + "temporarySchemaScriptFolder";

    private static final String DB_ENGINE_CODE = "dbengine";

    private static final File TEMP_SCHEMA_SCRIPT_ROOT_FOLDER =
            new File(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME);

    private static final File TEMP_SCHEMA_GENERIC_SCRIPT_FOLDER =
            new File(TEMP_SCHEMA_SCRIPT_ROOT_FOLDER, SqlScriptProvider.GENERIC);

    private static final File TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER =
            new File(TEMP_SCHEMA_SCRIPT_ROOT_FOLDER, DB_ENGINE_CODE);

    private static final String MIGRATION = "migration";

    private static final String VERSION = "042";

    private static final String VERSION2 = "049";

    private SqlScriptProvider sqlScriptProvider;

    private File dumpFile;

    @BeforeClass
    public void setUpTestFiles()
    {
        final File genericSchemaVersionFolder = createGenericSchemaFolder();
        final File specificSchemaVersionFolder = createSpecificSchemaFolder();
        genericSchemaVersionFolder.mkdirs();
        specificSchemaVersionFolder.mkdirs();
        write(new File(specificSchemaVersionFolder, "schema-" + VERSION + ".sql"), "code: schema");
        write(new File(specificSchemaVersionFolder, "function-" + VERSION + ".sql"),
                "code: function");
        final File migrationFolder = new File(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER, MIGRATION);
        migrationFolder.mkdir();
        write(new File(migrationFolder, "migration-" + VERSION + "-" + VERSION2 + ".sql"),
                "code: migration");
        write(new File(specificSchemaVersionFolder, "data-" + VERSION + ".sql"), "code: data");
        sqlScriptProvider =
                new SqlScriptProvider(Arrays.asList(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME),
                        DB_ENGINE_CODE);
        dumpFile = new File(sqlScriptProvider.getDumpFolder(VERSION), ".DUMP");
    }

    private File createSpecificSchemaFolder()
    {
        final File specificSchemaVersionFolder =
                new File(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER, VERSION);
        return specificSchemaVersionFolder;
    }

    private File createGenericSchemaFolder()
    {
        final File specificSchemaVersionFolder =
                new File(TEMP_SCHEMA_GENERIC_SCRIPT_FOLDER, VERSION);
        return specificSchemaVersionFolder;
    }

    private void write(File file, String content)
    {
        try
        {
            PrintWriter printWriter = null;
            try
            {
                printWriter = new PrintWriter(new FileWriter(file));
                printWriter.print(content);
            } finally
            {
                IOUtils.closeQuietly(printWriter);
            }
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    @AfterClass
    public void deleteTestFiles()
    {
        delete(new File(BASE_FOLDER));
    }

    private void delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                delete(child);
            }
        }
        file.delete();
    }

    @Test
    public void testGetSchemaScript()
    {
        final Script script = sqlScriptProvider.tryGetSchemaScript(VERSION);
        assertNotNull(script);
        assertEquals(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER.getPath() + "/" + VERSION + "/schema-"
                + VERSION + ".sql", script.getName());
        assertEquals("code: schema", script.getContent().trim());
    }

    // Note: we make it dependent on testGetSchemaScript(), because we delete the specific schema
    // script
    // in this test case and thus testGetSchemaScript() would fail if run after this test case.
    @Test(dependsOnMethods = "testGetSchemaScript")
    public void testGetGenericSchemaScript()
    {
        final File specificSchemaScript =
                new File(createSpecificSchemaFolder(), "schema-" + VERSION + ".sql");
        specificSchemaScript.delete();
        final File genericSchemaScript =
                new File(createGenericSchemaFolder(), "schema-" + VERSION + ".sql");
        final String genericSchemaScriptContent = "code: generic schema";
        write(genericSchemaScript, genericSchemaScriptContent);
        final Script script = sqlScriptProvider.tryGetSchemaScript(VERSION);
        assertNotNull(script);
        assertEquals(TEMP_SCHEMA_GENERIC_SCRIPT_FOLDER.getPath() + "/" + VERSION + "/schema-"
                + VERSION + ".sql", script.getName());
        assertEquals(genericSchemaScriptContent, script.getContent().trim());
    }

    @Test
    public void testGetNonExistingSchemaScript()
    {
        assertEquals(null, sqlScriptProvider.tryGetSchemaScript("000"));
    }

    @Test
    public void testGetFunctionScript()
    {
        final Script script = sqlScriptProvider.tryGetFunctionScript(VERSION);
        assertNotNull(script);
        assertEquals(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER.getPath() + "/" + VERSION + "/function-"
                + VERSION + ".sql", script.getName());
        assertEquals("code: function", script.getContent().trim());
    }

    @Test
    public void testGetNonExistingFunctionScript()
    {
        assertEquals(null, sqlScriptProvider.tryGetFunctionScript("000"));
    }

    @Test
    public void testGetDataScript()
    {
        Script script = sqlScriptProvider.tryGetDataScript(VERSION);
        assertEquals(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER + "/" + VERSION + "/data-" + VERSION
                + ".sql", script.getName());
        assertEquals("code: data", script.getContent().trim());
    }

    @Test
    public void testGetNonExistingDataScript()
    {
        assertEquals(null, sqlScriptProvider.tryGetDataScript("000"));
    }

    @Test
    public void testGetMigrationScript()
    {
        Script script = sqlScriptProvider.tryGetMigrationScript(VERSION, VERSION2);
        assertEquals(TEMP_SCHEMA_SPECIFIC_SCRIPT_FOLDER.getPath() + "/" + MIGRATION + "/migration-"
                + VERSION + "-" + VERSION2 + ".sql", script.getName());
        assertEquals("code: migration", script.getContent().trim());
    }

    @Test
    public void testGetNonExistingMigrationScript()
    {
        assertEquals(null, sqlScriptProvider.tryGetMigrationScript("000", "001"));
    }

    @Test
    public void testIsDumpSucceeds()
    {
        write(dumpFile, "");
        assertTrue(sqlScriptProvider.isDumpRestore(VERSION));
    }

    @Test
    public void testIsDumpFails()
    {
        dumpFile.delete();
        assertFalse(sqlScriptProvider.isDumpRestore(VERSION));
    }

    @Test
    public void testTwoRootFolders()
    {
        File root1 = new File(BASE_FOLDER, "root1");
        root1.mkdirs();
        File r1generic001 = new File(root1, SqlScriptProvider.GENERIC + "/001");
        r1generic001.mkdirs();
        File r1genericSchema001 = new File(r1generic001, "schema-001.sql");
        FileUtilities.writeToFile(r1genericSchema001, "root 1 generic");
        File r1specific001 = new File(root1, DB_ENGINE_CODE + "/001");
        r1specific001.mkdirs();
        File r1specificSchema001 = new File(r1specific001, "schema-001.sql");
        FileUtilities.writeToFile(r1specificSchema001, "root 1 specific");

        File root2 = new File(BASE_FOLDER, "root2");
        root2.mkdirs();
        File r2generic001 = new File(root2, SqlScriptProvider.GENERIC + "/001");
        r2generic001.mkdirs();
        File r2genericSchema001 = new File(r2generic001, "schema-001.sql");
        FileUtilities.writeToFile(r2genericSchema001, "root 2 generic");
        File r2specific001 = new File(root2, DB_ENGINE_CODE + "/001");
        r2specific001.mkdirs();
        File r2specificSchema001 = new File(r2specific001, "schema-001.sql");
        FileUtilities.writeToFile(r2specificSchema001, "root 2 specific");

        ISqlScriptProvider scriptProvider =
                new SqlScriptProvider(Arrays.asList(root1.getPath(), root2.getPath()),
                        DB_ENGINE_CODE);

        assertEquals("root 1 specific", scriptProvider.tryGetSchemaScript("001").getContent().trim());

        r1specificSchema001.delete();

        assertEquals("root 1 generic", scriptProvider.tryGetSchemaScript("001").getContent().trim());

        r1genericSchema001.delete();

        assertEquals("root 2 specific", scriptProvider.tryGetSchemaScript("001").getContent().trim());

        r2specificSchema001.delete();

        assertEquals("root 2 generic", scriptProvider.tryGetSchemaScript("001").getContent().trim());

        r2genericSchema001.delete();

        assertEquals(null, scriptProvider.tryGetSchemaScript("001"));
    }

}
