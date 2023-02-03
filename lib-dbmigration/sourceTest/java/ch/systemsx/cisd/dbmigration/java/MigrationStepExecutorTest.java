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

package ch.systemsx.cisd.dbmigration.java;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.apache.log4j.Level;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.Script;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Test cases for the {@link MigrationStepExecutor}.
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepExecutorTest
{
    private DatabaseConfigurationContext dbContext;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public final void setUp()
    {
        dbContext = new DatabaseConfigurationContext();
        dbContext.setDatabaseEngineCode("postgresql");
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
    }

    @BeforeClass(alwaysRun = true)
    public final void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @Test
    public final void testHappyCase()
    {
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        Script script =
                new Script("001To002.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        assertTrue(logRecorder.getLogContent(), logRecorder.getLogContent().contains(
                "Migration step class 'MigrationStepFrom001To002' found for "
                        + "migration script '001To002.sql'."));
        assertSame(dbContext.getDataSource(), migrationStepExecutor.getDataSource());
        logRecorder.resetLogContent();
        script =
                new Script("001To002.sql", "\n\n  \n"
                        + "--JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        AssertionUtil.assertEnds("Migration step class 'MigrationStepFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
    }

    @Test
    public final void testConfusionAdmin()
    {
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        Script script =
                new Script("001To002.sql",
                        "-- JAVA_ADMIN ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        assertTrue(logRecorder.getLogContent().indexOf(
                "Migration step class 'MigrationStepFrom001To002' found for "
                        + "migration script '001To002.sql'.") < 0);
        logRecorder.resetLogContent();
        script =
                new Script("001To002.sql", "\n\n  \n"
                        + "--JAVA_ADMINch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        assertTrue(logRecorder.getLogContent().indexOf(
                "Migration step class 'MigrationStepFrom001To002' found for "
                        + "migration script '001To002.sql'.") < 0);
    }

    @Test
    public final void testHappyCaseAdmin()
    {
        MigrationStepAdminFrom001To002.instance = null;
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, true);
        Script script =
                new Script("001To002.sql",
                        "-- JAVA_ADMIN ch.systemsx.cisd.dbmigration.java.MigrationStepAdminFrom001To002");
        migrationStepExecutor.init(script);
        assertNotNull(MigrationStepAdminFrom001To002.instance);
        assertSame(dbContext, MigrationStepAdminFrom001To002.instance.context);
        assertFalse(MigrationStepAdminFrom001To002.instance.preMigrationPerformed);
        migrationStepExecutor.performPreMigration();
        assertTrue(MigrationStepAdminFrom001To002.instance.preMigrationPerformed);
        assertFalse(MigrationStepAdminFrom001To002.instance.postMigrationPerformed);
        migrationStepExecutor.performPostMigration();
        assertTrue(MigrationStepAdminFrom001To002.instance.postMigrationPerformed);
        migrationStepExecutor.finish();
        AssertionUtil.assertEnds("Migration step class 'MigrationStepAdminFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
        assertSame(dbContext.getAdminDataSource(), migrationStepExecutor.getDataSource());
        logRecorder.resetLogContent();
        MigrationStepAdminFrom001To002.instance = null;
        script =
                new Script(
                        "001To002.sql",
                        "\n\n  \n"
                                + "--JAVA_ADMIN ch.systemsx.cisd.dbmigration.java.MigrationStepAdminFrom001To002");
        migrationStepExecutor.init(script);
        assertEquals("Migration step class 'MigrationStepAdminFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
    }

    @Test
    public final void testHappyCaseNormalAndAdmin()
    {
        MigrationStepAdminFrom001To002.instance = null;
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        final MigrationStepExecutor migrationStepExecutorAdmin =
                new MigrationStepExecutor(dbContext, true);
        Script script =
                new Script(
                        "001To002.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002\n"
                                + "-- JAVA_ADMIN ch.systemsx.cisd.dbmigration.java.MigrationStepAdminFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        assertTrue(logRecorder.getLogContent(), logRecorder.getLogContent().contains(
                "Migration step class 'MigrationStepFrom001To002' found for "
                        + "migration script '001To002.sql'."));
        logRecorder.resetLogContent();
        migrationStepExecutorAdmin.init(script);
        migrationStepExecutorAdmin.performPreMigration();
        migrationStepExecutorAdmin.performPostMigration();
        migrationStepExecutorAdmin.finish();
        assertEquals("Migration step class 'MigrationStepAdminFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
        logRecorder.resetLogContent();
        MigrationStepAdminFrom001To002.instance = null;
        script =
                new Script(
                        "001To002.sql",
                        "\n\n  \n"
                                + "--JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002\n \n   \n\n"
                                + "-- JAVA_ADMIN ch.systemsx.cisd.dbmigration.java.MigrationStepAdminFrom001To002");
        migrationStepExecutor.init(script);
        assertEquals("Migration step class 'MigrationStepFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
        logRecorder.resetLogContent();
        migrationStepExecutorAdmin.init(script);
        assertEquals("Migration step class 'MigrationStepAdminFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
    }

    @Test
    public final void testFinish()
    {
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        final Script script =
                new Script("001To002.sql",
                        "--   JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        boolean fail = true;
        try
        {
            migrationStepExecutor.performPreMigration();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testUnhappyCases()
    {
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        Script script =
                new Script("002To003.sql", "\n-- This is a comment\n-- This is another comment\n");
        migrationStepExecutor.init(script);
        assertTrue(logRecorder.getLogContent(), logRecorder.getLogContent().contains(
                "No migration step class found for migration script '002To003.sql'."));
        logRecorder.resetLogContent();
        script =
                new Script("002To003.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom002To003");
        migrationStepExecutor.init(script);
        assertTrue(logRecorder.getLogContent(), logRecorder.getLogContent().contains(
                "Migration step class 'MigrationStepFrom002To003' found for "
                        + "migration script '002To003.sql'."));
        try
        {
            migrationStepExecutor.performPreMigration();
            fail();
        } catch (final DataIntegrityViolationException ex)
        {
            // Nothing to do here.
        }
        try
        {
            migrationStepExecutor.performPostMigration();
            fail();
        } catch (final EmptyResultDataAccessException ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testClassNotFound()
    {
        final MigrationStepExecutor migrationStepExecutor =
                new MigrationStepExecutor(dbContext, false);
        final Script script =
                new Script("003To004.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom003To003");
        try
        {
            migrationStepExecutor.init(script);
            fail();
        } catch (final CheckedExceptionTunnel e)
        {
            // Nothing to do here.
        }
    }
}
