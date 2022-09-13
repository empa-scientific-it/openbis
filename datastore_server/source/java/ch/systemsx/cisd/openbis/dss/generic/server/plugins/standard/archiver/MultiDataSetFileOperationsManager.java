/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.IWaitingCondition;
import ch.systemsx.cisd.common.utilities.WaitingHelper;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.FilteredHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetFileOperationsManager extends AbstractDataSetFileOperationsManager implements IMultiDataSetFileOperationsManager,
        Serializable
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetFileOperationsManager.class);

    private static final long serialVersionUID = 1L;

    public static final String STAGING_DESTINATION_KEY = "staging-destination";

    public static final String FINAL_DESTINATION_KEY = "final-destination";

    public static final String REPLICATED_DESTINATION_KEY = "replicated-destination";

    public static final String WITH_SHARDING_KEY = "with-sharding";

    public static final String WAITING_FOR_FREE_SPACE_POLLING_TIME_KEY = "waiting-for-free-space-polling-time";

    public static final String WAITING_FOR_FREE_SPACE_TIME_OUT_KEY = "waiting-for-free-space-time-out";

    public static final String MINIMUM_FREE_SPACE_IN_MB_KEY = "minimum-free-space-in-MB";

    public static final String HDF5_FILES_IN_DATA_SET = "hdf5-files-in-data-set";

    private static final long DEFAULT_WAITING_FOR_FREE_SPACE_POLLING_TIME = DateUtils.MILLIS_PER_MINUTE;

    private static final long DEFAULT_WAITING_FOR_FREE_SPACE_TIME_OUT = 4 * DateUtils.MILLIS_PER_HOUR;

    private static final long DEFAULT_MINIMUM_FREE_SPACE_IN_MB = 1024;

    private transient ArchiveDestination stageArchive;

    private transient ArchiveDestination finalArchive;

    private transient ArchiveDestination finalReplicatedArchive;

    private final ArchiveDestinationFactory stageArchivefactory;

    private final ArchiveDestinationFactory finalArchivefactory;

    private final ArchiveDestinationFactory finalReplicatedArchivefactory;

    private final boolean withSharding;

    private final ITimeAndWaitingProvider timeProvider;

    private final IFreeSpaceProvider freeSpaceProviderOrNull;

    protected IMultiDataSetPackageManager packageManager;

    private long waitingForFreeSpacePollingTime;

    private long waitingForFreeSpaceTimeOut;

    private long minimumFreeSpace;

    private boolean hdf5FilesInDataSet;

    // TODO: some features existing in rsync archiver:
    // - ignore existing

    public MultiDataSetFileOperationsManager(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory, IFreeSpaceProvider freeSpaceProviderOrNull,
            ITimeAndWaitingProvider timeProvider)
    {
        this.freeSpaceProviderOrNull = freeSpaceProviderOrNull;
        this.timeProvider = timeProvider;
        this.packageManager = new MultiDataSetPackageManager(properties, new Log4jSimpleLogger(operationLog));

        this.withSharding = PropertyUtils.getBoolean(properties, WITH_SHARDING_KEY, false);
        hdf5FilesInDataSet = PropertyUtils.getBoolean(properties, HDF5_FILES_IN_DATA_SET, true);

        long timeoutInSeconds =
                PropertyUtils.getLong(properties, TIMEOUT_KEY, DEFAULT_TIMEOUT_SECONDS);
        long timeoutInMillis = timeoutInSeconds * DateUtils.MILLIS_PER_SECOND;

        finalArchivefactory = createArchiveFactory(FINAL_DESTINATION_KEY, "final destination",
                properties, pathCopierFactory, sshCommandExecutorFactory, timeoutInMillis);
        if (StringUtils.isNotBlank(properties.getProperty(STAGING_DESTINATION_KEY)))
        {
            stageArchivefactory = createArchiveFactory(STAGING_DESTINATION_KEY, "stage area",
                    properties, pathCopierFactory, sshCommandExecutorFactory, timeoutInMillis);
        } else
        {
            stageArchivefactory = finalArchivefactory;
        }
        if (StringUtils.isNotBlank(properties.getProperty(REPLICATED_DESTINATION_KEY)))
        {
            finalReplicatedArchivefactory = createArchiveFactory(REPLICATED_DESTINATION_KEY, "final cloned destination",
                    properties, pathCopierFactory, sshCommandExecutorFactory, timeoutInMillis);
        } else
        {
            finalReplicatedArchivefactory = finalArchivefactory;
        }

        waitingForFreeSpacePollingTime = DateTimeUtils.getDurationInMillis(properties,
                WAITING_FOR_FREE_SPACE_POLLING_TIME_KEY, DEFAULT_WAITING_FOR_FREE_SPACE_POLLING_TIME);
        waitingForFreeSpaceTimeOut = DateTimeUtils.getDurationInMillis(properties,
                WAITING_FOR_FREE_SPACE_TIME_OUT_KEY, DEFAULT_WAITING_FOR_FREE_SPACE_TIME_OUT);
        minimumFreeSpace = PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_IN_MB_KEY, DEFAULT_MINIMUM_FREE_SPACE_IN_MB)
                * FileUtils.ONE_MB;
    }

    private ArchiveDestinationFactory createArchiveFactory(String key, String name, Properties properties,
            IPathCopierFactory pathCopierFactory, ISshCommandExecutorFactory sshCommandExecutorFactory,
            long timeoutInMillis)
    {
        String finalHostFile = PropertyUtils.getMandatoryProperty(properties, key);
        if (false == new File(finalHostFile).isDirectory())
        {
            throw new ConfigurationFailureException("Archiving " + name + " '" + finalHostFile + "' is not an existing directory");
        }

        return new ArchiveDestinationFactory(properties, pathCopierFactory, sshCommandExecutorFactory, finalHostFile, timeoutInMillis);
    }

    private ArchiveDestination getStageArchive()
    {
        if (stageArchive == null)
        {
            stageArchive = stageArchivefactory.createArchiveDestination();
        }
        return stageArchive;
    }

    private ArchiveDestination getFinalArchive()
    {
        if (finalArchive == null)
        {
            finalArchive = finalArchivefactory.createArchiveDestination();
        }
        return finalArchive;
    }

    private ArchiveDestination getFinalReplicatedArchive()
    {
        if (finalReplicatedArchive == null)
        {
            finalReplicatedArchive = finalReplicatedArchivefactory.createArchiveDestination();
        }
        return finalReplicatedArchive;
    }

    @Override
    public Status deleteContainerFromStage(IMultiDataSetArchiveCleaner cleaner, String containerPath)
    {
        if (isStagingAreaDefined() == false)
        {
            return Status.OK;
        }
        File stageArchiveContainerFile = new File(getStageArchive().getDestination(), containerPath);

        if (false == stageArchiveContainerFile.isFile())
        {
            operationLog.warn("Archive container '" + containerPath + "' doesn't exist.");
            return Status.OK;
        }
        cleaner.delete(stageArchiveContainerFile);
        return Status.OK;
    }

    @Override
    public Status restoreDataSetsFromContainerInFinalDestination(String containerPath,
            List<? extends IDatasetLocation> dataSetLocations)
    {
        HashMap<String, File> dataSetToLocation = new HashMap<String, File>();
        for (IDatasetLocation dataSetLocation : dataSetLocations)
        {
            File location = getDirectoryProvider().getDataSetDirectory(dataSetLocation);
            dataSetToLocation.put(dataSetLocation.getDataSetCode(), location);
        }

        File stageArchiveContainerFile = new File(getFinalArchive().getDestination(), containerPath);
        return packageManager.extractMultiDataSets(stageArchiveContainerFile, dataSetToLocation);
    }

    @Override
    public Status createContainer(String containerPath, List<DatasetDescription> datasetDescriptions)
    {
        long totalSize = SegmentedStoreUtils.calculateTotalSize(datasetDescriptions);
        Status status;
        if (isStagingAreaDefined())
        {
            status = createContainerFile(containerPath, datasetDescriptions);
            if (status.isError())
            {
                return status;
            }
            waitUntilEnoughFreeSpace(totalSize);
            status = copyToFinalDestination(containerPath);
        } else
        {
            waitUntilEnoughFreeSpace(totalSize);
            status = createContainerFile(containerPath, datasetDescriptions);
        }
        return status;
    }

    private Status createContainerFile(String containerPath, List<DatasetDescription> datasetDescriptions)
    {
        File containerFile = new File(getStageArchive().getDestination(), containerPath);
        IShareIdManager shareIdManager = getDirectoryProvider().getShareIdManager();
        Status status = Status.OK;
        try
        {
            List<AbstractExternalData> dataSets = new LinkedList<AbstractExternalData>();
            for (DatasetDescription datasetDescription : datasetDescriptions)
            {
                AbstractExternalData dataSet = getDataSetWithAllMetaData(datasetDescription);
                dataSets.add(dataSet);
                shareIdManager.lock(dataSet.getCode());
                operationLog.info("Archive dataset " + dataSet.getCode() + " in " + containerFile);
            }

            boolean result = createFolderIfNotExists(stageArchive, containerFile.getParentFile());

            // TODO: react somehow?
            if (result)
            {
                operationLog.warn("File already exists in archive " + containerFile.getParentFile());
            }

            packageManager.create(containerFile, dataSets);
        } catch (Exception ex)
        {
            status = Status.createError(ex.toString());
            operationLog.error("Couldn't create package file: " + containerPath, ex);
        } finally
        {
            try
            {
                if (Status.OK.equals(status))
                {
                    Collection<VerificationError> errors = packageManager.verify(containerFile);

                    if (errors.size() > 0)
                    {
                        status = Status.createError(errors.toString());
                        throw new RuntimeException(errors.toString());
                    }
                }

                operationLog.info("Data sets archived: " + containerPath);
            } catch (Exception ex)
            {
                operationLog.error("Couldn't create package file: " + containerPath, ex);
            }
            for (DatasetDescription datasetDescription : datasetDescriptions)
            {
                shareIdManager.releaseLock(datasetDescription.getDataSetCode());
            }
        }
        return status;
    }

    /**
     * Returns container path local to the archive root.
     */
    @Override
    public String generateContainerPath(List<DatasetDescription> dataSets)
    {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String name = packageManager.getName(dataSets.get(0).getDataSetCode() + "-" + timestamp);

        if (withSharding)
        {
            return dataSets.get(0).getDataSetLocation() + "/" + name;
        }
        else
        {
            return name;
        }

    }

    @Override
    public boolean isStagingAreaDefined()
    {
        return finalArchivefactory != stageArchivefactory;
    }

    @Override
    public boolean isReplicatedArchiveDefined()
    {
        return finalArchivefactory != finalReplicatedArchivefactory;
    }

    @Override
    public String getOriginalArchiveFilePath(String containerPath)
    {
        return getFilePath(getFinalArchive(), containerPath);
    }

    @Override
    public String getReplicatedArchiveFilePath(String containerPath)
    {
        return getFilePath(getFinalReplicatedArchive(), containerPath);
    }

    private String getFilePath(ArchiveDestination archive, String containerPath)
    {
        return new File(archive.getDestination(), containerPath).getAbsolutePath();
    }

    private void waitUntilEnoughFreeSpace(final long totalSize)
    {
        ArchiveDestination finalDestination = getFinalArchive();
        final IDataSetFileOperationsExecutor operationsExecutor = finalDestination.getExecutor();
        final String destinationPath = new File(finalDestination.getDestination()).getAbsolutePath();
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        WaitingHelper waitingHelper = new WaitingHelper(waitingForFreeSpaceTimeOut, waitingForFreeSpacePollingTime,
                timeProvider, logger, true);
        boolean conditionFulfilled = waitingHelper.waitOn(new IWaitingCondition()
            {
                private long freeSpace;

                @Override
                public boolean conditionFulfilled()
                {
                    freeSpace = getFreeSpace(operationsExecutor, destinationPath);
                    return totalSize + minimumFreeSpace < freeSpace;
                }

                @Override
                public String toString()
                {
                    return "Free space: " + FileUtilities.byteCountToDisplaySize(freeSpace)
                            + ", needed space: " + FileUtilities.byteCountToDisplaySize(totalSize + minimumFreeSpace);
                }
            });
        if (conditionFulfilled == false)
        {
            long freeSpace = getFreeSpace(operationsExecutor, destinationPath);
            throw new EnvironmentFailureException("Still no free space on '" + destinationPath + "' after "
                    + DateTimeUtils.renderDuration(waitingForFreeSpaceTimeOut) + ". "
                    + FileUtils.byteCountToDisplaySize(totalSize) + " needed but only "
                    + FileUtilities.byteCountToDisplaySize(freeSpace) + " available.");
        }
    }

    private long getFreeSpace(final IDataSetFileOperationsExecutor operationsExecutor, final String destinationPath)
    {
        if (freeSpaceProviderOrNull != null)
        {
            HostAwareFile hostAwareFile = new HostAwareFile(null, destinationPath, null);
            try
            {
                return freeSpaceProviderOrNull.freeSpaceKb(hostAwareFile) * FileUtils.ONE_KB;
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        return operationsExecutor.freeSpaceKb(destinationPath) * FileUtils.ONE_KB;
    }

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the destination is defined by the original location of the
     * data set.
     */
    private Status copyToFinalDestination(String containerLocalPath)
    {
        ArchiveDestination stageDestination = getStageArchive();
        File containerFile = new File(stageDestination.getDestination(), containerLocalPath);

        ArchiveDestination finalDestination = getFinalArchive();
        try
        {
            File destinationFolder = new File(finalDestination.getDestination(), containerLocalPath);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            IDataSetFileOperationsExecutor operationsExecutor = finalDestination.getExecutor();
            File parentFolder = destinationFolder.getParentFile();
            if (createFolderIfNotExists(finalDestination, parentFolder)
                    || destinationExists(finalDestination, destinationFolder).isSuccess() == false)
            {
                operationLog.info("Copy archive container from '" + containerFile + "' to '" + parentFolder);
                operationsExecutor.copyDataSetToDestination(containerFile, parentFolder);
                operationLog.info("Copying archive container took " + stopWatch);
            } else
            {
                operationLog.info("Update archive container from '"
                        + containerFile + "' to '" + parentFolder);
                operationsExecutor.syncDataSetWithDestination(containerFile, parentFolder);
                operationLog.info("Updating archive container took " + stopWatch);
            }

            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    @Override
    public Status deleteContainerFromFinalDestination(IMultiDataSetArchiveCleaner cleaner, String containerLocalPath)
    {
        return deleteContainerFromDestination(cleaner, containerLocalPath, getFinalArchive());
    }

    @Override
    public Status deleteContainerFromFinalReplicatedDestination(IMultiDataSetArchiveCleaner cleaner, String containerLocalPath)
    {
        return deleteContainerFromDestination(cleaner, containerLocalPath, getFinalReplicatedArchive());
    }

    protected Status deleteContainerFromDestination(IMultiDataSetArchiveCleaner cleaner, String containerLocalPath, ArchiveDestination destination)
    {
        try
        {
            File containerInFinalDestination = new File(destination.getDestination(), containerLocalPath);
            cleaner.delete(containerInFinalDestination);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    private static final IHierarchicalContentNodeFilter METADATA_IN_CONTAINER_FILTER = new IHierarchicalContentNodeFilter()
        {
            @Override
            public boolean accept(IHierarchicalContentNode node)
            {
                return AbstractDataSetPackager.META_DATA_FILE_NAME.equals(node.getName()) == false;
            }
        };

    @Override
    public IHierarchicalContent getContainerAsHierarchicalContent(String containerPath, List<DatasetDescription> dataSets)
    {
        ArchiveDestination archiveDestination = getFinalArchive();
        return getAsHierarchicalContent(archiveDestination, containerPath, dataSets);
    }

    @Override
    public IHierarchicalContent getReplicaAsHierarchicalContent(String containerPath, List<DatasetDescription> dataSets)
    {
        ArchiveDestination archiveDestination = getFinalReplicatedArchive();
        return getAsHierarchicalContent(archiveDestination, containerPath, dataSets);
    }

    public IHierarchicalContent getAsHierarchicalContent(ArchiveDestination archiveDestination, String containerPath,
            List<DatasetDescription> dataSets)
    {
        String destinationRoot = archiveDestination.getDestination();
        File containerInDestination = new File(destinationRoot, containerPath);

        IHierarchicalContent containerMetaData = packageManager.asHierarchialContent(containerInDestination, dataSets, hdf5FilesInDataSet == false);
        return new FilteredHierarchicalContent(containerMetaData, METADATA_IN_CONTAINER_FILTER);
    }

    private boolean createFolderIfNotExists(ArchiveDestination archiveDestination, File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(archiveDestination, destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            archiveDestination.getExecutor().createFolder(destinationFolder);
            return true;
        }
        return false;
    }

    private BooleanStatus destinationExists(ArchiveDestination archiveDestination, File destinationFolder)
    {
        BooleanStatus destinationExists = archiveDestination.getExecutor().exists(destinationFolder);
        if (destinationExists.isError())
        {
            // operationLog.error("Could not check existence of '" + destinationFolder + "': "
            // + destinationExists.tryGetMessage());
            throw new ExceptionWithStatus(Status.createError("CHECK_EXISTENCE_FAILED"));
        }
        return destinationExists;
    }

}
