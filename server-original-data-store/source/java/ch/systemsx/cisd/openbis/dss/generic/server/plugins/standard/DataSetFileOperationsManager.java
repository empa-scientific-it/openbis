/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.Hdf5AwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.AbstractDataSetFileOperationsManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.ArchiveDestination;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.ArchiveDestinationFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Code based on LocalAndRemoteCopier, able to copy dataset files both ways: to and from destination.
 * 
 * @author Piotr Buczek
 */
public class DataSetFileOperationsManager extends AbstractDataSetFileOperationsManager implements IDataSetFileOperationsManager
{
    private static interface IDeleteAction
    {
        String getName();

        void delete(File dataSetFolder, String dataSetCode);
    }

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetFileOperationsManager.class);

    private final ArchiveDestination archiveDestination;

    public DataSetFileOperationsManager(Properties properties,
            IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        long timeoutInSeconds =
                PropertyUtils.getLong(properties, TIMEOUT_KEY, DEFAULT_TIMEOUT_SECONDS);
        long timeoutInMillis = timeoutInSeconds * DateUtils.MILLIS_PER_SECOND;

        String hostFile = PropertyUtils.getMandatoryProperty(properties, DESTINATION_KEY);

        ArchiveDestinationFactory factory =
                new ArchiveDestinationFactory(properties, pathCopierFactory, sshCommandExecutorFactory, hostFile, timeoutInMillis);
        this.archiveDestination = factory.createArchiveDestination();
    }

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the destination is defined by the original location of the
     * data set.
     */
    @Override
    public Status copyToDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(archiveDestination.getDestination(), dataset.getDataSetLocation());
            if (createFolderIfNotExists(destinationFolder.getParentFile())
                    || destinationExists(destinationFolder).isSuccess() == false)
            {
                operationLog.info("Copy dataset '" + dataset.getDataSetCode() + "' from '"
                        + originalData.getPath() + "' to '" + destinationFolder.getParentFile());
                archiveDestination.getExecutor().copyDataSetToDestination(originalData, destinationFolder.getParentFile());
            } else
            {
                operationLog.info("Update dataset '" + dataset.getDataSetCode() + "' from '"
                        + originalData.getPath() + "' to '" + destinationFolder.getParentFile());
                archiveDestination.getExecutor().syncDataSetWithDestination(originalData, destinationFolder.getParentFile());
            }

            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Retrieves specified datases's data from the destination specified in constructor. The path at the destination is defined by original location
     * of the data set.
     */
    @Override
    public Status retrieveFromDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(archiveDestination.getDestination(), dataset.getDataSetLocation());
            checkDestinationExists(destinationFolder);
            File folder = originalData.getParentFile();
            operationLog.info("Retrieve data set '" + dataset.getDataSetCode() + "' from '"
                    + destinationFolder.getPath() + "' to '" + folder);
            folder.mkdirs();
            archiveDestination.getExecutor().retrieveDataSetFromDestination(folder, destinationFolder);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Deletes specified datases's data from the destination specified in constructor. The path at the destination is defined by original location of
     * the data set.
     */
    @Override
    public Status deleteFromDestination(IDatasetLocation dataset)
    {
        return delete(dataset, new IDeleteAction()
            {
                @Override
                public String getName()
                {
                    return "delete";
                }

                @Override
                public void delete(File dataSetFolder, String dataSetCode)
                {
                    archiveDestination.getExecutor().deleteFolder(dataSetFolder);
                }
            });
    }

    @Override
    public Status markAsDeleted(IDatasetLocation dataset)
    {
        return delete(dataset, new IDeleteAction()
            {
                @Override
                public String getName()
                {
                    return "mark as deleted";
                }

                @Override
                public void delete(File dataSetFolder, String dataSetCode)
                {
                    File deletedFolder =
                            new File(archiveDestination.getDestination(), FOLDER_OF_AS_DELETED_MARKED_DATA_SETS);
                    archiveDestination.getExecutor().createFolder(deletedFolder);
                    File markerFile = new File(deletedFolder, dataSetCode);
                    archiveDestination.getExecutor().createMarkerFile(markerFile);
                }
            });
    }

    private Status delete(IDatasetLocation dataset, IDeleteAction action)
    {
        try
        {
            File destinationFolder = new File(archiveDestination.getDestination(), dataset.getDataSetLocation());
            BooleanStatus destinationExists = destinationExists(destinationFolder);
            if (destinationExists.isSuccess())
            {
                action.delete(destinationFolder, dataset.getDataSetCode());
            } else
            {
                operationLog.info("Data of data set '" + dataset.getDataSetCode()
                        + "' don't exist in the destination '" + destinationFolder.getPath()
                        + "'. There is nothing to " + action.getName() + ".");
            }
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Checks if specified dataset's data are present and synchronized in the destination specified in constructor. The path at the destination is
     * defined by original location of the data set.
     */
    @Override
    public BooleanStatus isSynchronizedWithDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(archiveDestination.getDestination(), dataset.getDataSetLocation());
            BooleanStatus resultStatus = archiveDestination.getExecutor().checkSame(originalData, destinationFolder);
            String message = resultStatus.tryGetMessage();
            if (message != null) // if there is a message something went wrong
            {
                operationLog.error(message);
            }
            return resultStatus;
        } catch (ExceptionWithStatus ex)
        {
            return BooleanStatus.createError(ex.getStatus().toString());
        }
    }

    /**
     * Checks if specified dataset's data are present in the destination specified in constructor. The path at the destination is defined by original
     * location of the data set.
     */
    @Override
    public BooleanStatus isPresentInDestination(DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(archiveDestination.getDestination(), dataset.getDataSetLocation());
            BooleanStatus resultStatus = archiveDestination.getExecutor().exists(destinationFolder);
            String message = resultStatus.tryGetMessage();
            if (message != null) // if there is a message something went wrong
            {
                operationLog.error(message);
            }
            return resultStatus;
        } catch (ExceptionWithStatus ex)
        {
            return BooleanStatus.createError(ex.getStatus().toString());
        }
    }

    private void checkDestinationExists(File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            operationLog.error("Destination folder '" + destinationFolder + "' doesn't exist");
            throw new ExceptionWithStatus(Status.createError(DESTINATION_DOES_NOT_EXIST));
        }
    }

    private boolean createFolderIfNotExists(File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            archiveDestination.getExecutor().createFolder(destinationFolder);
            return true;
        }
        return false;
    }

    private BooleanStatus destinationExists(File destinationFolder)
    {
        BooleanStatus destinationExists = archiveDestination.getExecutor().exists(destinationFolder);
        if (destinationExists.isError())
        {
            operationLog.error("Could not check existence of '" + destinationFolder + "': "
                    + destinationExists.tryGetMessage());
            throw new ExceptionWithStatus(Status.createError(CHECK_EXISTENCE_FAILED));
        }
        return destinationExists;
    }

    @Override
    public boolean isHosted()
    {
        return archiveDestination.isHosted();
    }

    @Override
    public IHierarchicalContent getAsHierarchicalContent(DatasetDescription dataset)
    {
        return new Hdf5AwareHierarchicalContentFactory(dataset.isH5Folders(), dataset.isH5arFolders())
                .asHierarchicalContent(new File(archiveDestination.getDestination(), dataset.getDataSetLocation()), null);
    }

}