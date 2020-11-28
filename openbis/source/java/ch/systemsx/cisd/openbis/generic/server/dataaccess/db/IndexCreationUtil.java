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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;

/**
 * Utility methods around database indexing with <i>Hibernate</i>.
 * 
 * @author Christian Ribeaud
 */
public final class IndexCreationUtil
{
    static final String DATABASE_NAME_PREFIX = "openbis_";

    private static final Template DROP_DATABASE_TEMPLATE = new Template(
            "drop database if exists ${duplicated-database}");

    private static final Template CREATE_DATABASE_TEMPLATE = new Template(
            "create database ${duplicated-database} with owner ${owner} template ${database}");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            IndexCreationUtil.class);

    private IndexCreationUtil()
    {
        // Can not be instantiated.
    }

    //
    // Main method
    //

    public static void main(final String... args) throws Exception
    {
        Parameters parameters = parseArguments(args);

        LogInitializer.init();

        if (parameters.getDuplicatedDatabaseKind() != null)
        {
            dumpDatabase(parameters);
        }
    }

    private static void dumpDatabase(Parameters parameters)
    {
        String databaseKind = parameters.getDatabaseKind();
        String duplicatedDatabaseKind = parameters.getDuplicatedDatabaseKind();
        String indexFolder = parameters.getIndexFolder();

        String databaseName = DATABASE_NAME_PREFIX + databaseKind;
        String duplicatedDatabaseName = DATABASE_NAME_PREFIX + duplicatedDatabaseKind;
        boolean ok = duplicateDatabase(duplicatedDatabaseName, databaseName);
        if (ok == false)
        {
            throw new IllegalStateException("Execution failed");
        }
        File dumpFile = parameters.getDumpFile();
        operationLog.info("Dump '" + duplicatedDatabaseName + "' into '" + dumpFile + "'.");
        DumpPreparator.createDatabaseDump(duplicatedDatabaseName, dumpFile);
        databaseKind = duplicatedDatabaseKind;
        FileUtilities.deleteRecursively(new File(indexFolder));
    }

    private static Parameters parseArguments(final String... args)
    {
        Parameters parameters = null;
        try
        {
            parameters = new Parameters(args);
        } catch (IllegalArgumentException e)
        {
            System.out.println(Parameters.getUsage());
            throw e;
        }
        return parameters;
    }

    static boolean duplicateDatabase(String destinationDatabase, String sourceDatabase)
    {
        operationLog.info("Duplicate database '" + sourceDatabase + "' as '" + destinationDatabase
                + "'.");
        Template dropCmd = DROP_DATABASE_TEMPLATE.createFreshCopy();
        dropCmd.bind("duplicated-database", destinationDatabase);

        boolean ok = execute(dropCmd);
        if (ok == false)
        {
            return false;
        }
        Template createCmd = CREATE_DATABASE_TEMPLATE.createFreshCopy();
        createCmd.bind("database", sourceDatabase);
        createCmd.bind("duplicated-database", destinationDatabase);
        createCmd.bind("owner", System.getProperty("user.name"));
        return execute(createCmd);
    }

    private static boolean execute(Template template)
    {
        String sql = template.createText();
        String psql = DumpPreparator.getPSQLExecutable();
        List<String> cmd = Arrays.asList(psql,
                "-h", DatabaseEngine.getTestEnvironmentHostOrConfigured("localhost"),
                "-U", "postgres", "-c", sql);
        boolean ok = ProcessExecutionHelper.runAndLog(cmd, operationLog, operationLog);
        if (ok == false)
        {
            operationLog.error("Sql command execution failed: " + template.createText());
        }
        return ok;
    }

    private static class Parameters
    {
        static String getUsage()
        {
            return "Usage: java "
                    + IndexCreationUtil.class.getName()
                    + " [-d <duplicated database kind> <dump file>] <database kind> [<index folder>]";
        }

        private String duplicatedDatabaseKind;

        private File dumpFile;

        private String databaseKind;

        private String indexFolder = "sourceTest/lucene/indices";

        private String fromScratch;

        Parameters(String[] args)
        {
            List<String> arguments = new ArrayList<String>(Arrays.asList(args));
            if (arguments.size() > 0 && arguments.get(0).equals("-d"))
            {
                arguments.remove(0);
                throwExceptionIfEmpty(arguments, "duplicated database kind");
                duplicatedDatabaseKind = arguments.remove(0);
                throwExceptionIfEmpty(arguments, "dump file");
                dumpFile = new File(arguments.remove(0));
            }
            throwExceptionIfEmpty(arguments, "database kind");
            databaseKind = arguments.remove(0);
            if (arguments.size() > 0)
            {
                indexFolder = arguments.remove(0);
            }
            fromScratch = arguments.isEmpty() ? "false" : arguments.remove(0);
        }

        private void throwExceptionIfEmpty(List<String> arguments, String entityType)
        {
            if (arguments.size() < 1)
            {
                throw new IllegalArgumentException("Missing argument <" + entityType + ">.");
            }
        }

        final File getDumpFile()
        {
            return dumpFile;
        }

        final String getDuplicatedDatabaseKind()
        {
            return duplicatedDatabaseKind;
        }

        final String getDatabaseKind()
        {
            return databaseKind;
        }

        final String getIndexFolder()
        {
            return indexFolder;
        }

        final String getFromScratch()
        {
            return fromScratch;
        }

    }
}
