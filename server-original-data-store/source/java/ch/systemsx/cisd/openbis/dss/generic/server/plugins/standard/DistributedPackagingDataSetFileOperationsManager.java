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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.FilteredHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.AbstractDataSetFileOperationsManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiveFolders;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IdentifierAttributeMappingManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class DistributedPackagingDataSetFileOperationsManager extends AbstractDataSetFileOperationsManager implements IDataSetFileOperationsManager
{
    static final String MAPPING_FILE_KEY = "mapping-file";

    static final String CREATE_ARCHIVES_KEY = MAPPING_FILE_KEY + ".create-archives";

    static final String DEFAULT_DESTINATION_KEY = "default-archive-folder";

    static final String DEFAULT_SMALL_DATA_SETS_DESTINATION_KEY = "default-small-data-sets-archive-folder";

    static final String SMALL_DATA_SETS_SIZE_LIMIT_KEY = "small-data-sets-size-limit";

    static final String WITH_SHARDING_KEY = "with-sharding";

    static final String IGNORE_EXISTING_KEY = "ignore-existing";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DistributedPackagingDataSetFileOperationsManager.class);

    private static final IHierarchicalContentNodeFilter FILTER = new IHierarchicalContentNodeFilter()
        {
            @Override
            public boolean accept(IHierarchicalContentNode node)
            {
                return AbstractDataSetPackager.META_DATA_FILE_NAME.equals(node.getRelativePath()) == false;
            }
        };

    private final boolean ignoreExisting;

    private final File defaultFolder;

    private final File defaultSmallDataSetsFolder;

    private final Long smallDataSetsSizeLimit;

    private final boolean withSharding;

    private final String mappingFilePathOrNull;

    private final boolean createArchives;

    private transient IdentifierAttributeMappingManager archiveFolderMapping;

    protected IPackageManager packageManager;

    public DistributedPackagingDataSetFileOperationsManager(Properties properties, IPackageManager packageManager)
    {
        this(properties, null, null, packageManager);
    }

    DistributedPackagingDataSetFileOperationsManager(Properties properties,
            IEncapsulatedOpenBISService service, IDataSetDirectoryProvider directoryProvider, IPackageManager packageManager)
    {
        this.service = service;
        this.directoryProvider = directoryProvider;
        this.packageManager = packageManager;

        ignoreExisting = PropertyUtils.getBoolean(properties,
                IGNORE_EXISTING_KEY, false);
        withSharding = PropertyUtils.getBoolean(properties, WITH_SHARDING_KEY, false);

        defaultFolder = new File(PropertyUtils.getMandatoryProperty(properties, DEFAULT_DESTINATION_KEY));
        if (defaultFolder.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Default archive folder '" + defaultFolder.getPath()
                    + "' doesn't exist or is not a folder.");
        }

        String aDefaultSmallDataSetsFolder = PropertyUtils.getProperty(properties, DEFAULT_SMALL_DATA_SETS_DESTINATION_KEY);
        if (false == StringUtils.isBlank(aDefaultSmallDataSetsFolder))
        {
            defaultSmallDataSetsFolder = new File(aDefaultSmallDataSetsFolder);
            if (defaultSmallDataSetsFolder.isDirectory() == false)
            {
                throw new ConfigurationFailureException("Default small data sets archive folder '" + defaultSmallDataSetsFolder.getPath()
                        + "' doesn't exist or is not a folder.");
            }
        }
        else
        {
            defaultSmallDataSetsFolder = null;
        }

        long aSmallDataSetsSizeLimit = PropertyUtils.getLong(properties, SMALL_DATA_SETS_SIZE_LIMIT_KEY, -1);
        if (aSmallDataSetsSizeLimit > 0)
        {
            smallDataSetsSizeLimit = aSmallDataSetsSizeLimit * 1024;
        }
        else
        {
            smallDataSetsSizeLimit = 0l;
        }

        mappingFilePathOrNull = properties.getProperty(MAPPING_FILE_KEY);
        createArchives = PropertyUtils.getBoolean(properties, CREATE_ARCHIVES_KEY, false);
        getArchiveFolderMapping(); // Loads and validates mapping file
    }

    @Override
    public Status copyToDestination(File originalData, DatasetDescription datasetDescription)
    {
        AbstractExternalData dataSet = getDataSetWithAllMetaData(datasetDescription);
        IShareIdManager shareIdManager = getDirectoryProvider().getShareIdManager();
        Status status = Status.OK;
        String dataSetCode = datasetDescription.getDataSetCode();
        File file = getArchiveFile(datasetDescription);
        shareIdManager.lock(dataSetCode);
        try
        {
            packageManager.create(file, dataSet);
        } catch (Exception ex)
        {
            status = Status.createError(ex.toString());
            operationLog.error("Couldn't create package file: " + file, ex);
        } finally
        {
            try
            {
                if (Status.OK.equals(status))
                {
                    Collection<VerificationError> errors = packageManager.verify(file);

                    if (errors.size() > 0)
                    {
                        status = Status.createError(errors.toString());
                        throw new RuntimeException(errors.toString());
                    }
                }

                operationLog.info("Data set '" + dataSetCode + "' archived: " + file);
            } catch (Exception ex)
            {
                operationLog.error("Couldn't create package file: " + file, ex);
            }
            shareIdManager.releaseLock(dataSetCode);
        }
        return status;
    }

    @Override
    public Status retrieveFromDestination(File originalData, DatasetDescription datasetDescription)
    {
        File file = getArchiveFile(datasetDescription);

        Status status = packageManager.extract(file, originalData);

        if (status.isOK())
        {
            operationLog.info("Data set '" + datasetDescription.getDataSetCode() + "' retrieved from archive '"
                    + file.getPath() + "' to '" + originalData + "'.");
        }

        return status;
    }

    @Override
    public Status deleteFromDestination(IDatasetLocation dataset)
    {
        File archiveFile = tryFindArchiveFile(dataset);
        if (archiveFile == null)
        {
            operationLog.warn("Archive file for data set '" + dataset.getDataSetCode() + "' no konger exists.");
            return Status.OK;
        }
        boolean success = archiveFile.delete();
        return success ? Status.OK : Status.createError("Couldn't delete archive file '" + archiveFile + "'.");
    }

    private File tryFindArchiveFile(IDatasetLocation datasetLocation)
    {
        File archiveFile = getArchiveFile(defaultFolder, datasetLocation, false);

        if (archiveFile.isFile())
        {
            return archiveFile;
        }

        if (defaultSmallDataSetsFolder != null)
        {
            archiveFile = getArchiveFile(defaultSmallDataSetsFolder, datasetLocation, false);

            if (archiveFile.isFile())
            {
                return archiveFile;
            }
        }

        Collection<File> folders = getArchiveFolderMapping().getAllFolders();
        for (File folder : folders)
        {
            archiveFile = getArchiveFile(folder, datasetLocation, false);
            if (archiveFile.isFile())
            {
                return archiveFile;
            }
        }
        return null;
    }

    @Override
    public Status markAsDeleted(IDatasetLocation dataset)
    {
        File archiveFile = tryFindArchiveFile(dataset);
        if (archiveFile == null)
        {
            operationLog.warn("Archive file for data set '" + dataset.getDataSetCode() + "' no konger exists.");
            return Status.OK;
        }
        String relPath = (withSharding ? dataset.getDataSetLocation() + "/" : "") + createPackageFileName(dataset);
        String path = archiveFile.getPath();
        int index = path.lastIndexOf(relPath);
        File archiveFolder = new File(path.substring(0, index));
        File folderOfMarkers = new File(archiveFolder, DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS);
        folderOfMarkers.mkdirs();
        File markerFile = new File(folderOfMarkers, dataset.getDataSetCode());
        try
        {
            if (markerFile.createNewFile() == false)
            {
                throw new IOException("Marker file already exists.");
            }
            return Status.OK;
        } catch (IOException ex)
        {
            String message = "Couldn't create marker file '" + markerFile + "': " + ex;
            operationLog.error(message, ex);
            return Status.createError(message);
        }
    }

    @Override
    public BooleanStatus isSynchronizedWithDestination(File originalData,
            DatasetDescription datasetDescription)
    {
        return isPresentInDestination(datasetDescription);
    }

    @Override
    public BooleanStatus isPresentInDestination(
            DatasetDescription datasetDescription)
    {

        if (ignoreExisting)
        {
            File file = getArchiveFile(datasetDescription);

            if (file.exists() && file.length() > 0)
            {
                operationLog
                        .info("Data set '"
                                + datasetDescription.getDataSetCode()
                                + "' will be ignored as it already exists in the archive.");
                return BooleanStatus.createTrue();
            } else
            {
                return BooleanStatus.createFalse();
            }
        } else
        {
            return BooleanStatus.createFalse();
        }
    }

    @Override
    public boolean isHosted()
    {
        return false;
    }

    @Override
    public IHierarchicalContent getAsHierarchicalContent(DatasetDescription dataset)
    {
        IHierarchicalContent content = packageManager.asHierarchialContent(getArchiveFile(dataset), 
                Arrays.asList(dataset), false);
        return new FilteredHierarchicalContent(content, FILTER);
    }

    private File getArchiveFile(DatasetDescription datasetDescription)
    {
        File folder = getArchiveFolderMapping().getArchiveFolder(datasetDescription, null);

        // there is no mapping, lets use default archive folders
        if (folder == null)
        {
            File[] folders = new File[] {
                    defaultFolder,
                    defaultSmallDataSetsFolder
            };
            ArchiveFolders archiveFolders = ArchiveFolders.create(folders, false, smallDataSetsSizeLimit);
            folder = archiveFolders.getFolder(datasetDescription);
        }

        return getArchiveFile(folder, datasetDescription, true);
    }

    private File getArchiveFile(File baseFolder, IDatasetLocation datasetLocation, boolean forWriting)
    {
        File folder = getArchiveFolder(baseFolder, datasetLocation, forWriting);
        return new File(folder, createPackageFileName(datasetLocation));
    }

    private String createPackageFileName(IDatasetLocation datasetLocation)
    {
        return packageManager.getName(datasetLocation.getDataSetCode());
    }

    private File getArchiveFolder(File baseFolder, IDatasetLocation datasetLocation, boolean forWriting)
    {
        File folder = baseFolder;
        if (withSharding)
        {
            folder = new File(folder, datasetLocation.getDataSetLocation());
            if (forWriting && folder.exists() == false)
            {
                folder.mkdirs();
            }
        }
        return folder;
    }

    private IdentifierAttributeMappingManager getArchiveFolderMapping()
    {
        if (archiveFolderMapping == null)
        {
            archiveFolderMapping = new IdentifierAttributeMappingManager(mappingFilePathOrNull, createArchives, smallDataSetsSizeLimit);
        }
        return archiveFolderMapping;
    }

}
