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

package ch.systemsx.cisd.dbmigration.h2;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.*;
import org.apache.log4j.Logger;
import org.h2.tools.DeleteDbFiles;
import org.springframework.jdbc.support.SQLErrorCodesFactory;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.db.Script;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Implementation of {@link IDatabaseAdminDAO} for H2.
 *
 * @author Bernd Rinn
 */
public class H2AdminDAO extends AbstractDatabaseAdminDAO
{
    private static final String DROP_ALL_OBJECTS_SQL = "drop all objects;";

    private static final String SQL_FILE_TYPE = ".sql";

    private static final Pattern dbDirPartPattern = Pattern.compile(".*:file:(.*?)/.*");

    private static final String CREATE_TABLE_DATABASE_VERSION_LOGS_SQL =
            "create table "
                    + DatabaseVersionLogDAO.DB_VERSION_LOG
                    + " (db_version varchar(4) not null, "
                    + "module_name varchar(250), run_status varchar(10), run_status_timestamp timestamp, "
                    + "module_code bytea, run_exception bytea);";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, H2AdminDAO.class);

    private final String databaseDir;

    /**
     * Creates an instance.
     *
     * @param dataSource Data source able to create/drop the specified database.
     * @param scriptExecutor An executor of SQL scripts within the new database.
     * @param massUploader A class that can perform mass (batch) uploads into database tables.
     * @param databaseName Name of the database.
     * @param databaseURL URL of the database.
     */
    public H2AdminDAO(DataSource dataSource, ISqlScriptExecutor scriptExecutor,
            IMassUploader massUploader, String databaseName, String databaseURL)
    {
        super(dataSource, scriptExecutor, massUploader, null, null, null, databaseName, databaseURL);
        final Matcher dbDirPartMatcherOrNull = dbDirPartPattern.matcher(databaseURL);
        if (dbDirPartMatcherOrNull.matches())
        {
            this.databaseDir = dbDirPartMatcherOrNull.group(1);
        } else
        {
            this.databaseDir = ".";
        }
    }

    @Override
    public String getDatabaseServerVersion()
    {
        return null;
    }

    @Override
    public void createOwner()
    {
        // Creation of the user happens "on the fly" with H2
    }

    @Override
    public void createGroups()
    {
        // Creation of the user happens "on the fly" with H2
    }

    @Override
    public void createDatabase()
    {
        // Creation of databases happens "on the fly" with H2, we only need to create the
        // database_version_logs table
        createDatabaseVersionLogsTable();
    }

    @Override
    public void initializeErrorCodes()
    {
        SQLErrorCodesFactory.getInstance().getErrorCodes(getJdbcTemplate().getDataSource());
    }

    private void createDatabaseVersionLogsTable()
    {
        try
        {
            scriptExecutor.execute(new Script("create database_version_logs table",
                    CREATE_TABLE_DATABASE_VERSION_LOGS_SQL), true, null);
        } catch (RuntimeException ex)
        {
            operationLog.error("Failed to create database_version_logs table.", ex);
            throw ex;
        }
    }

    @Override
    public void dropDatabase()
    {
        scriptExecutor.execute(new Script("drop database", DROP_ALL_OBJECTS_SQL), true, null);
        try
        {
            DeleteDbFiles.execute(databaseDir, databaseName, true);
        } catch (SQLException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    @Override
    public void restoreDatabaseFromDump(File dumpFolder, String version)
    {
        createDatabaseVersionLogsTable();
        final Script schemaScript = tryLoadScript(dumpFolder, "schema", version);
        scriptExecutor.execute(schemaScript, true, null);
        final File[] massUploadFiles = getMassUploadFiles(dumpFolder);
        massUploader.performMassUpload(massUploadFiles);
        final Script finishScript = tryLoadScript(dumpFolder, "finish", version);
        scriptExecutor.execute(finishScript, true, null);
    }

    @Override
    public void applyFullTextSearchScripts(final ISqlScriptProvider scriptProvider, final String version, final boolean applyMainScript)
    {
        // No implementation.
    }

    private Script tryLoadScript(final File dumpFolder, String prefix, String version)
            throws ConfigurationFailureException
    {
        final File scriptFile = new File(dumpFolder, prefix + "-" + version + SQL_FILE_TYPE);
        if (scriptFile.canRead() == false)
        {
            final String message = "No " + prefix + " script found for version " + version;
            operationLog.error(message);
            throw new ConfigurationFailureException(message);
        }
        final Script script =
                new Script(scriptFile.getPath(), FileUtilities.loadToString(scriptFile), version);
        return script;
    }

    /**
     * Returns the files determined for mass uploading.
     */
    private File[] getMassUploadFiles(File dumpFolder)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Searching for mass upload files in directory '"
                    + dumpFolder.getAbsolutePath() + "'.");
        }
        String[] csvFiles = dumpFolder.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return MassUploadFileType.CSV.isOfType(name)
                            || MassUploadFileType.TSV.isOfType(name);
                }
            });
        if (csvFiles == null)
        {
            operationLog.warn("Path '" + dumpFolder.getAbsolutePath() + "' is not a directory.");
            return new File[0];
        }
        Arrays.sort(csvFiles);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Found " + csvFiles.length + " files for mass uploading.");
        }
        final File[] csvPaths = new File[csvFiles.length];
        for (int i = 0; i < csvFiles.length; ++i)
        {
            csvPaths[i] = new File(dumpFolder, csvFiles[i]);
        }
        return csvPaths;
    }

}
