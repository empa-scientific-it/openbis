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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseDefinition;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseMetaData;
import ch.systemsx.cisd.dbmigration.SimpleTableMetaData;
import ch.systemsx.cisd.dbmigration.TableColumnDefinition;
import ch.systemsx.cisd.dbmigration.TableDefinition;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SequenceNameMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Class which allows to import another database instance.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseInstanceImporter
{
    private static final String COLUMN_CODE = ColumnNames.CODE_COLUMN;

    private static final String COLUMN_UUID = ColumnNames.UUID_COLUMN;

    private static final String COLUMN_IS_ORIGINAL_SOURCE = ColumnNames.IS_ORIGINAL_SOURCE_COLUMN;

    private static final String DATABASE_INSTANCES = TableNames.DATABASE_INSTANCES_TABLE;

    private static IDatabaseAdminDAO createExportDAO(final Parameters parameters)
    {
        final DatabaseConfigurationContext configContext = createConfigContext(parameters);
        configContext.setUrlHostPart(parameters.getDatabaseName());
        return configContext.createDAOFactory().getDatabaseDAO();
    }

    private static IDatabaseAdminDAO createUploadDAO(final Parameters parameters)
    {
        final DatabaseConfigurationContext configContext = createConfigContext(parameters);
        configContext.setSequenceNameMapper(new SequenceNameMapper());
        configContext.setSequenceUpdateNeeded(true);
        final IDAOFactory daoFactory = configContext.createDAOFactory();
        return daoFactory.getDatabaseDAO();
    }

    @Private
    static DatabaseConfigurationContext createConfigContext(final Parameters parameters)
    {
        final DatabaseConfigurationContext dbContext = new DatabaseConfigurationContext();
        final String databaseName = parameters.getDatabaseName();
        final int indexOfDelimiter = databaseName.indexOf('_');
        if (indexOfDelimiter < 0)
        {
            throw new UserFailureException("Missing '_' in database name '" + databaseName + "'.");
        }
        dbContext.setBasicDatabaseName(databaseName.substring(0, indexOfDelimiter));
        dbContext.setDatabaseKind(databaseName.substring(indexOfDelimiter + 1));
        dbContext.setDatabaseEngineCode(parameters.getDatabaseEngine());
        return dbContext;
    }

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatabaseInstanceImporter.class);

    private final File importedDumpFile;

    private final File uploadFolder;

    private final String codeForImportedDatabaseInstanceOrNull;

    private final File currentDumpFile;

    private final File currentDatabaseFolder;

    private final File importDatabaseFolder;

    private IDatabaseAdminDAO exportDAO;

    private IDatabaseAdminDAO uploadDAO;

    private final IDatabaseDumper databaseDumper;

    DatabaseInstanceImporter(final Parameters parameters)
    {
        this(parameters, createExportDAO(parameters), createUploadDAO(parameters),
                new IDatabaseDumper()
                    {
                        //
                        // IDatabaseDumper
                        //

                        @Override
                        public final boolean createDatabaseDump(final String dataBaseName,
                                final File dumpFile)
                        {
                            return DumpPreparator.createDatabaseDump(dataBaseName, dumpFile);
                        }
                    });
    }

    DatabaseInstanceImporter(final Parameters parameters, final IDatabaseAdminDAO exportDAO,
            final IDatabaseAdminDAO uploadDAO, final IDatabaseDumper databaseDumper)
    {
        this.exportDAO = exportDAO;
        this.uploadDAO = uploadDAO;
        this.databaseDumper = databaseDumper;
        this.importedDumpFile = new File(parameters.getDumpFileName());
        this.uploadFolder = new File(parameters.getUploadFolder());
        this.codeForImportedDatabaseInstanceOrNull = parameters.getDatabaseInstanceCode();
        currentDumpFile = new File(uploadFolder, "db_dump.sql");
        currentDatabaseFolder = new File(uploadFolder, "current-database");
        currentDatabaseFolder.mkdirs();
        importDatabaseFolder = new File(uploadFolder, "import-database");
        importDatabaseFolder.mkdirs();
    }

    void importDatabase()
    {
        final String databaseName = exportDAO.getDatabaseName();
        dumpDatabase(databaseName);
        logInfo("Create upload files in directory '" + currentDatabaseFolder.getAbsolutePath()
                + "'.");
        final SimpleDatabaseMetaData currentMetaData =
                createUploadFiles(currentDumpFile, currentDatabaseFolder);
        logInfo("Start importing database from dump file '" + importedDumpFile.getAbsolutePath()
                + "'.");
        logInfo("Create upload files in directory '" + importDatabaseFolder.getAbsolutePath()
                + "'.");
        final SimpleDatabaseMetaData metaData =
                createUploadFiles(importedDumpFile, importDatabaseFolder);
        checkMetaData(currentMetaData, metaData);
        checkAndModifyDatabaseInstanceFile(currentMetaData, metaData);
        mergeTables(exportDAO, currentMetaData, metaData);
        replaceCurrentDatabase(databaseName, currentMetaData);
    }

    private void replaceCurrentDatabase(final String databaseName,
            final SimpleDatabaseMetaData currentMetaData)
    {
        final String databaseVersion = currentMetaData.getDatabaseVersion();
        logInfo("Replace current database '" + databaseName + "' (version " + databaseVersion
                + ") by the merged one.");
        logInfo("Drop current database.");
        uploadDAO.dropDatabase();
        logInfo("Upload merged database.");
        uploadDAO.restoreDatabaseFromDump(currentDatabaseFolder, databaseVersion);
        logInfo("Merged database successfully uploaded.");
    }

    private void mergeTables(final IDatabaseAdminDAO adminDAO,
            final SimpleDatabaseMetaData currentMetaData, final SimpleDatabaseMetaData metaData)
    {
        final DatabaseDefinition databaseDefinition = adminDAO.getDatabaseDefinition();
        final Set<TableDefinition> tables =
                databaseDefinition.getTablesDependingOn(DATABASE_INSTANCES);
        tables.add(databaseDefinition.getTableDefinition(DATABASE_INSTANCES));
        for (final TableDefinition tableDefinition : tables)
        {
            final String tableName = tableDefinition.getTableName();
            logInfo("Merge table '" + tableName + "'.");
            final SimpleTableMetaData tableMetaData = metaData.tryToGetTableMetaData(tableName);
            final int[] indexMap = createIndexMap(tableName, currentMetaData, metaData);
            final long[] offsets = new long[indexMap.length];
            for (final TableColumnDefinition tableColumnDefinition : tableDefinition)
            {
                final String columnName = tableColumnDefinition.getColumnName();
                if (tableColumnDefinition.isPrimaryKey())
                {
                    final long offset = tableColumnDefinition.getLargestPrimaryKey();
                    final int columnIndex = tableMetaData.getIndexOfColumn(columnName);
                    offsets[columnIndex] = offset;
                } else
                {
                    final TableColumnDefinition reference =
                            tableColumnDefinition.getForeignKeyReference();
                    if (reference != null && tables.contains(reference.getTableDefinition()))
                    {
                        final long offset = reference.getLargestPrimaryKey();
                        final int columnIndex = tableMetaData.getIndexOfColumn(columnName);
                        offsets[columnIndex] = offset;
                    }
                }
            }
            final File currentTabFile =
                    new File(currentDatabaseFolder, currentMetaData
                            .tryToGetTableMetaData(tableName).getTableFileName());
            final File importedTabFile =
                    new File(importDatabaseFolder, tableMetaData.getTableFileName());
            modifyKeys(currentTabFile, importedTabFile, indexMap, offsets);
        }
    }

    private int[] createIndexMap(String tableName, SimpleDatabaseMetaData currentMetaData,
            SimpleDatabaseMetaData metaData)
    {
        final SimpleTableMetaData tableMetaData = metaData.tryToGetTableMetaData(tableName);
        final SimpleTableMetaData currentTableMetaData =
                currentMetaData.tryToGetTableMetaData(tableName);
        List<String> columnNames = tableMetaData.getColumnNames();
        int tableColumnsCount = columnNames.size();
        List<String> currentColumnNames = currentTableMetaData.getColumnNames();
        int currentTableColumnsCount = currentColumnNames.size();
        if (tableColumnsCount != currentTableColumnsCount)
        {
            throw new UserFailureException("Current table has " + currentTableColumnsCount
                    + " columns but imported one " + tableColumnsCount + " columns.");
        }
        final int[] indexMap = new int[tableColumnsCount];
        for (int i = 0; i < indexMap.length; i++)
        {
            String currentColumnName = currentColumnNames.get(i);
            indexMap[i] = columnNames.indexOf(currentColumnName);
        }
        return indexMap;
    }

    private void modifyKeys(final File currentTabFile, final File importedTabFile,
            final int[] indexMap, final long[] offsets)
    {
        final List<String> importedTabLines = FileUtilities.loadToStringList(importedTabFile);
        final File newUploadFile = new File(uploadFolder, "temp.tsv");
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(newUploadFile);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        final PrintWriter writer = new PrintWriter(new BufferedWriter(fileWriter), true);
        try
        {
            copyTo(writer, currentTabFile);
            for (final String line : importedTabLines)
            {
                final String[] tokens = line.split("\t");
                if (indexMap.length != tokens.length)
                {
                    throw new IllegalArgumentException(indexMap.length
                            + " tokens expected instead of " + tokens.length + ": " + line);
                }
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < indexMap.length; i++)
                {
                    int indexInImportedLine = indexMap[i];
                    final long offset = offsets[indexInImportedLine];
                    String token = tokens[indexInImportedLine];
                    if (offset > 0)
                    {
                        try
                        {
                            token = Long.toString(Long.parseLong(token) + offset);
                        } catch (final NumberFormatException ex)
                        {
                            if (token.equals("\\N") == false)
                            {
                                throw new IllegalArgumentException(i + ":"
                                        + (indexInImportedLine + 1) + ". token is not a number: "
                                        + line);
                            }
                        }
                    }
                    builder.append(token);
                    if (i < offsets.length - 1)
                    {
                        builder.append('\t');
                    }
                }
                writer.println(builder);
            }
            writer.close();
            if (currentTabFile.delete() == false)
            {
                throw new IOException("Couldn't delete file '" + currentTabFile.getAbsolutePath()
                        + "'.");
            }
            if (newUploadFile.renameTo(currentTabFile) == false)
            {
                throw new IOException("Couldn't rename file '" + newUploadFile.getAbsolutePath()
                        + "' to '" + currentTabFile.getAbsolutePath() + "'.");
            }
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    private final static void copyTo(final PrintWriter writer, final File file) throws IOException
    {
        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            IOUtils.copy(fileReader, writer);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private void checkAndModifyDatabaseInstanceFile(final SimpleDatabaseMetaData currentMetaData,
            final SimpleDatabaseMetaData metaData)
    {
        final List<DatabaseInstance> currentDatabaseInstances =
                listDatabaseInstances(currentMetaData, currentDatabaseFolder);
        final List<DatabaseInstance> databaseInstances =
                listDatabaseInstances(metaData, importDatabaseFolder);

        if (databaseInstances.size() == 0)
        {
            throw new UserFailureException("Database to be imported has no database instances.");
        } else if (databaseInstances.size() > 1)
        {
            throw new UserFailureException("Databases to be imported has "
                    + databaseInstances.size() + " database instances.");
        }
        final DatabaseInstance databaseInstance = databaseInstances.get(0);
        for (final DatabaseInstance currentDatabaseInstance : currentDatabaseInstances)
        {
            if (currentDatabaseInstance.getUUID().equals(databaseInstance.getUUID()))
            {
                throw new UserFailureException("Database instance '" + databaseInstance.getCode()
                        + "' couldn't be imported because it has the same UUID "
                        + "as already existing database instance '"
                        + currentDatabaseInstance.getCode() + "'.");
            }
        }

        final String code = databaseInstance.getCode();
        String newCode = codeForImportedDatabaseInstanceOrNull;
        if (newCode == null)
        {
            newCode = code;
        }
        for (final DatabaseInstance currentDatabaseInstance : currentDatabaseInstances)
        {
            if (currentDatabaseInstance.getCode().equals(newCode))
            {
                throw new UserFailureException("There is already a database instance with code '"
                        + newCode
                        + "'. Please, choose another code with the command line option '-d'.");
            }
        }
        if (code.equals(newCode) == false || databaseInstance.isOriginalSource())
        {
            // change database instance table of imported database
            databaseInstance.setCode(newCode);
            databaseInstance.setOriginalSource(false);
            final String fileName =
                    metaData.tryToGetTableMetaData(DATABASE_INSTANCES).getTableFileName();
            final File file = new File(importDatabaseFolder, fileName);
            FileUtilities.writeToFile(file, databaseInstance.toString());
        }
    }

    private List<DatabaseInstance> listDatabaseInstances(final SimpleDatabaseMetaData metaData,
            final File tabFileFolder)
    {
        final SimpleTableMetaData tableMetaData =
                metaData.tryToGetTableMetaData(DATABASE_INSTANCES);
        if (tableMetaData == null)
        {
            throw new UserFailureException("Can not find table '" + DATABASE_INSTANCES + "' in "
                    + tabFileFolder.getName());
        }
        final int numberOfColumns = tableMetaData.getColumnNames().size();
        final int indexOfCode = getIndexOf(COLUMN_CODE, tableMetaData, tabFileFolder);
        final int indexOfUUID = getIndexOf(COLUMN_UUID, tableMetaData, tabFileFolder);
        final int indexOfOriginalSource =
                getIndexOf(COLUMN_IS_ORIGINAL_SOURCE, tableMetaData, tabFileFolder);
        final File tabFile = new File(tabFileFolder, tableMetaData.getTableFileName());
        final List<String> tableRows = FileUtilities.loadToStringList(tabFile);
        final List<DatabaseInstance> list = new ArrayList<DatabaseInstance>();
        for (final String row : tableRows)
        {
            final String[] cells = row.split("\t");
            if (cells.length != numberOfColumns)
            {
                throw new UserFailureException("The following row has " + cells.length
                        + " cells instead of " + numberOfColumns + ": " + row);
            }
            final DatabaseInstance databaseInstance =
                    new DatabaseInstance(cells, indexOfCode, indexOfUUID, indexOfOriginalSource);
            list.add(databaseInstance);
        }
        return list;
    }

    private int getIndexOf(final String columnName, final SimpleTableMetaData tableMetaData,
            final File tabFileFolder)
    {
        final int indexOfCode = tableMetaData.getIndexOfColumn(columnName);
        if (indexOfCode < 0)
        {
            throw new UserFailureException("Missing column '" + columnName + "'  in table '"
                    + DATABASE_INSTANCES + "' in " + tabFileFolder.getName());
        }
        return indexOfCode;
    }

    private void checkMetaData(final SimpleDatabaseMetaData currentMetaData,
            final SimpleDatabaseMetaData metaData)
    {
        final String currentVersion = currentMetaData.getDatabaseVersion();
        final String importedVersion = metaData.getDatabaseVersion();
        if (currentVersion.equals(importedVersion) == false)
        {
            throw new UserFailureException("Version of current database is " + currentVersion
                    + " which does not match the version of the database to be imported: "
                    + importedVersion);
        }
        final Set<String> currentTables = getTableNames(currentMetaData);
        final Set<String> importedTables = getTableNames(metaData);
        if (currentTables.equals(importedTables) == false)
        {
            Set<String> missingTables = new HashSet<String>(currentTables);
            missingTables.removeAll(importedTables);
            if (missingTables.size() > 0)
            {
                throw new UserFailureException("Current database has tables " + missingTables
                        + "\n which do not exist in the database to be imported.");
            }
            Set<String> unknownTables = new HashSet<String>(importedTables);
            unknownTables.removeAll(currentTables);
            if (unknownTables.size() > 0)
            {
                throw new UserFailureException("Current database does not have tables "
                        + unknownTables + "\n which exist in the database to be imported.");
            }
        }
    }

    private Set<String> getTableNames(final SimpleDatabaseMetaData metaData)
    {
        final Set<String> list = new HashSet<String>();
        for (final SimpleTableMetaData tableMetaData : metaData.getTables())
        {
            list.add(tableMetaData.getTableName());
        }
        return list;
    }

    private SimpleDatabaseMetaData createUploadFiles(final File dumpFile, final File folder)
    {
        try
        {
            return DumpPreparator.createUploadFiles(dumpFile, folder, true);
        } catch (final IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private void dumpDatabase(final String databaseName)
    {
        logInfo("Dump current database '" + databaseName + "' into '"
                + currentDumpFile.getAbsolutePath() + "'.");
        if (databaseDumper.createDatabaseDump(databaseName, currentDumpFile) == false)
        {
            throw new UserFailureException("Couldn't dump database");
        }
    }

    private void logInfo(final Object message)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(message);
        }
    }

    public static void main(final String[] args)
    {
        LogInitializer.init();
        final Parameters parameters = new Parameters(args, SystemExit.SYSTEM_EXIT);

        final DatabaseInstanceImporter importer = new DatabaseInstanceImporter(parameters);

        try
        {
            importer.importDatabase();
        } catch (final UserFailureException e)
        {
            System.out.println();
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

}
