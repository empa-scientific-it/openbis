/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.BatchResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.DataSetArchiveVerificationResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.DataSetArchiveVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IDataSetArchiveVerificationBatch;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.SerialDataSetArchiveVerificationBatch;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationErrorType;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.FileSystemArchiveFileRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.FlatFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.IFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.ShardingFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.pathinfo.JdbcPathInfoRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.CompositeVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.CrcEnabled;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.IArchiveFileMetaDataRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.ZipFileHeaderVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.ZipFileIntegrityVerifier;

/**
 * Creates a DataSetArchiveVerificationBatch based on command line arguments. First argument should point to service.properties of the local DSS. Rest
 * of the arguments are the codes of datasets whose archive files should be verified.
 * 
 * @author anttil
 */
public class DataSetArchiveVerificationBatchFactory
{

    private final String[] args;

    private String servicePropertiesPath;

    public DataSetArchiveVerificationBatchFactory(String[] args)
    {
        this.args = args;
    }

    public IDataSetArchiveVerificationBatch build()
    {
        try
        {
            if (args.length < 2)
            {
                throw new ConfigurationException("Usage: datastore_server.sh verify-archives <dataset_code_1> <dataset_code_2> ...");
            }

            Properties properties = readServiceProperties(args[0]);
            File defaultArchiveDirectory = getDefaultArchiveDirectory(properties);
            IArchiveFileMetaDataRepository pathInfoRepository = getPathInfoRepository(properties);
            List<File> archiveDirectories = getArchiveDirectories(properties, defaultArchiveDirectory);
            IArchiveFileRepository archiveFileRepository = getArchiveFileRepository(properties, archiveDirectories);
            List<IArchiveFileVerifier> verifiers = getVerifiers(properties, pathInfoRepository);
            DataSetArchiveVerifier verifier = new DataSetArchiveVerifier(archiveFileRepository, new CompositeVerifier(verifiers));

            if (pathInfoRepository == null)
            {
                Collection<DataSetArchiveVerificationResult> init = new ArrayList<DataSetArchiveVerificationResult>();
                init.add(new DataSetArchiveVerificationResult(VerificationErrorType.GENERAL_WARNING, "pathinfo db not configured"));
                return new SerialDataSetArchiveVerificationBatch(verifier, init, Arrays.copyOfRange(args, 1, args.length));
            } else
            {
                return new SerialDataSetArchiveVerificationBatch(verifier, Arrays.copyOfRange(args, 1, args.length));
            }

        } catch (ConfigurationException e)
        {
            final String error = e.getMessage();
            return new IDataSetArchiveVerificationBatch()
                {
                    @Override
                    public BatchResult run()
                    {
                        BatchResult result = new BatchResult();
                        result.add("", new DataSetArchiveVerificationResult(VerificationErrorType.FATAL, error));
                        return result;
                    }
                };
        }
    }

    private List<IArchiveFileVerifier> getVerifiers(Properties properties, IArchiveFileMetaDataRepository pathInfoRepository)
    {
        List<IArchiveFileVerifier> verifiers = new ArrayList<IArchiveFileVerifier>();
        verifiers.add(new ZipFileIntegrityVerifier());

        if (pathInfoRepository != null)
        {
            verifiers.add(new ZipFileHeaderVerifier(pathInfoRepository,
                    "true".equalsIgnoreCase(properties.getProperty(PATHINFO_CHECKSUMS_COMPUTED)) ? CrcEnabled.TRUE : CrcEnabled.FALSE));
        }
        return verifiers;
    }

    private FileSystemArchiveFileRepository getArchiveFileRepository(Properties properties, List<File> archiveDirectories)
    {
        boolean sharding = "true".equalsIgnoreCase(properties.getProperty(SHARDING));
        IFileLocator fileLocator;
        if (sharding)
        {
            fileLocator = new ShardingFileLocator();
        } else
        {
            fileLocator = new FlatFileLocator();
        }

        FileSystemArchiveFileRepository fileFinder = new FileSystemArchiveFileRepository(archiveDirectories, fileLocator);
        return fileFinder;
    }

    private List<File> getArchiveDirectories(Properties properties, File defaultArchiveDirectory) throws ConfigurationException
    {
        List<File> mappedArchiveDirectories = new ArrayList<File>();
        String mappingFilePath = properties.getProperty(MAPPING_FILE);
        if (mappingFilePath != null)
        {
            File mappingFile = new File(mappingFilePath);
            if (mappingFile.exists() == false)
            {
                throw new ConfigurationException("Mapping file " + mappingFilePath + " does not exist");
            }

            try
            {
                BufferedReader br = new BufferedReader(new FileReader(mappingFile));
                String line;
                int lineNumber = 0;
                while ((line = br.readLine()) != null)
                {
                    lineNumber++;
                    if (lineNumber == 1)
                    {
                        continue;
                    }
                    String archiveDirectoryPath = line.substring(line.lastIndexOf("\t") + 1);
                    File archiveDirectory = new File(archiveDirectoryPath);
                    if (archiveDirectory.exists() == false)
                    {
                        throw new ConfigurationException("Archive directory " + archiveDirectoryPath + " specified in mapping file "
                                + mappingFilePath
                                + " does not exist");
                    }
                    mappedArchiveDirectories.add(archiveDirectory);
                }
                br.close();
            } catch (IOException e)
            {
                throw new ConfigurationException("I/O error: " + e.getMessage());
            }
        }

        List<File> archiveDirectories = new ArrayList<File>();
        archiveDirectories.add(defaultArchiveDirectory);
        archiveDirectories.addAll(mappedArchiveDirectories);
        return archiveDirectories;
    }

    private IArchiveFileMetaDataRepository getPathInfoRepository(Properties properties) throws ConfigurationException
    {
        IArchiveFileMetaDataRepository pathInfoRepository = null;

        String databaseName = properties.getProperty(DATABASE_NAME);
        String databaseKind = properties.getProperty(DATABASE_KIND);

        if (databaseName != null && databaseKind != null)
        {
            String user = properties.getProperty(DATABASE_USER);
            String password = properties.getProperty(DATABASE_PASSWORD);

            if (user == null || user.isEmpty())
            {
                user = "postgres";
            }

            if (password == null)
            {
                password = "";
            }

            String url = "jdbc:postgresql://localhost/" + databaseName + "_" + databaseKind;

            try
            {
                Connection connection = DriverManager.getConnection(url, user, password);
                pathInfoRepository = new JdbcPathInfoRepository(connection);
            } catch (SQLException ex)
            {
                throw new ConfigurationException("Could not connect to pathinfo db at " + url + " with user name '" + user + "', password '"
                        + password + "'");
            }
        }
        return pathInfoRepository;
    }

    private File getDefaultArchiveDirectory(Properties properties) throws ConfigurationException
    {
        String defaultArchiveDirectoryPath = properties.getProperty(DEFAULT_ARCHIVE_FOLDER);
        if (defaultArchiveDirectoryPath == null)
        {
            throw new ConfigurationException(servicePropertiesPath + " does not contain mandatory property " + DEFAULT_ARCHIVE_FOLDER);
        }

        File defaultArchiveDirectory = new File(defaultArchiveDirectoryPath);
        if (defaultArchiveDirectory.exists() == false)
        {
            throw new ConfigurationException("Default archive directory " + defaultArchiveDirectoryPath + " does not exist");
        }
        return defaultArchiveDirectory;
    }

    private Properties readServiceProperties(String path) throws ConfigurationException
    {
        File serviceProperties = new File(path);
        servicePropertiesPath = serviceProperties.getAbsolutePath();

        if (serviceProperties.exists() == false)
        {
            throw new ConfigurationException("File " + servicePropertiesPath + " does not exist");
        }

        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(serviceProperties));
        } catch (IOException ex)
        {
            throw new ConfigurationException("Could not read " + servicePropertiesPath + ": " + ex.getMessage());
        }
        return properties;
    }

    private class ConfigurationException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private final String message;

        public ConfigurationException(String message)
        {
            this.message = message;
        }

        @Override
        public String getMessage()
        {
            return message;
        }
    }

    private static final String DEFAULT_ARCHIVE_FOLDER = "archiver.default-archive-folder";

    private static final String MAPPING_FILE = "archiver.mapping-file";

    private static final String SHARDING = "archiver.with-sharding";

    private static final String DATABASE_NAME = "path-info-db.basicDatabaseName";

    private static final String DATABASE_KIND = "path-info-db.databaseKind";

    private static final String DATABASE_USER = "path-info-db.owner";

    private static final String DATABASE_PASSWORD = "path-info-db.password";

    private static final String PATHINFO_CHECKSUMS_COMPUTED = "post-registration.pathinfo-feeding.compute-checksum";
}
