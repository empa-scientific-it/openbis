/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.Hdf5AwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Archiver plugin which copies data sets to a destination folder using rsync (if it is remote). The destination can be
 * <ul>
 * <li>on the local file system,
 * <li>a mounted remote folder,
 * <li>a remote folder accessible via SSH,
 * <li>a remote folder accessible via an rsync server.
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class RsyncArchiver extends AbstractArchiverProcessingPlugin
{
    @Private
    static final String ONLY_MARK_AS_DELETED_KEY = "only-mark-as-deleted";

    @Private
    static final String STAGING_FOLDER = "archive-staging";

    @Private
    static final String VERIFY_CHECKSUMS_KEY = "verify-checksums";

    private static final long serialVersionUID = 1L;

    public enum ChecksumVerificationCondition
    {
        NO()
                {
                    @Override
                    boolean verifyChecksum(IHierarchicalContentNode node)
                    {
                        return false;
                    }
                },
        YES()
                {
                    @Override
                    boolean verifyChecksum(IHierarchicalContentNode node)
                    {
                        return true;
                    }
                },
        IF_AVAILABLE()
                {
                    @Override
                    boolean verifyChecksum(IHierarchicalContentNode node)
                    {
                        return node.isChecksumCRC32Precalculated();
                    }
                };

        abstract boolean verifyChecksum(IHierarchicalContentNode node);
    }

    private static final Comparator<IHierarchicalContentNode> NODE_COMPARATOR =
            new Comparator<IHierarchicalContentNode>()
                {
                    @Override
                    public int compare(IHierarchicalContentNode n1, IHierarchicalContentNode n2)
                    {
                        return n1.getName().compareTo(n2.getName());
                    }
                };

    public enum DeleteAction
    {
        DELETE(Operation.DELETE_FROM_ARCHIVE)
        {
            @Override
            public Status execute(IDataSetFileOperationsManager manager, IDatasetLocation dataSet)
            {
                return manager.deleteFromDestination(dataSet);
            }
        },
        MARK_AS_DELETED(Operation.MARK_AS_DELETED)
        {
            @Override
            public Status execute(IDataSetFileOperationsManager manager, IDatasetLocation dataSet)
            {
                return manager.markAsDeleted(dataSet);
            }
        };
        private final Operation operation;

        private DeleteAction(Operation operation)
        {
            this.operation = operation;
        }

        public Operation getOperation()
        {
            return operation;
        }

        public abstract Status execute(IDataSetFileOperationsManager manager,
                IDatasetLocation dataSet);
    }

    private transient IDataSetFileOperationsManager fileOperationsManager;

    private final IDataSetFileOperationsManagerFactory fileOperationsManagerFactory;

    public IDataSetFileOperationsManager getFileOperationsManager()
    {
        if (fileOperationsManager == null)
        {
            fileOperationsManager = fileOperationsManagerFactory.create();
        }
        return fileOperationsManager;
    }

    private final DeleteAction deleteAction;

    private final ChecksumVerificationCondition checksumVerificationCondition;

    public static class DataSetFileOperationsManagerFactory implements IDataSetFileOperationsManagerFactory
    {
        private static final long serialVersionUID = 1L;

        private final Properties properties;

        public DataSetFileOperationsManagerFactory(Properties properties)
        {
            this.properties = properties;
        }

        @Override
        public IDataSetFileOperationsManager create()
        {
            return new DataSetFileOperationsManager(properties,
                    new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory());
        }
    }

    public RsyncArchiver(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new DataSetFileOperationsManagerFactory(properties));
    }

    protected RsyncArchiver(Properties properties, File storeRoot,
            IDataSetFileOperationsManagerFactory fileOperationsManagerFactory)
    {
        this(properties, storeRoot, fileOperationsManagerFactory, PropertyUtils.getBoolean(properties,
                ONLY_MARK_AS_DELETED_KEY, true) ? DeleteAction.MARK_AS_DELETED
                : DeleteAction.DELETE, PropertyUtils.getBoolean(properties, VERIFY_CHECKSUMS_KEY,
                true) ? ChecksumVerificationCondition.YES : ChecksumVerificationCondition.NO);

    }

    public RsyncArchiver(Properties properties, File storeRoot,
            IDataSetFileOperationsManagerFactory fileOperationsManagerFactory, DeleteAction deleteAction,
            ChecksumVerificationCondition checksumVerificationCondition)
    {
        super(properties, storeRoot, null, null);
        this.fileOperationsManagerFactory = fileOperationsManagerFactory;
        this.deleteAction = deleteAction;
        this.checksumVerificationCondition = checksumVerificationCondition;
    }

    private File getTempRoot()
    {
        File tempFolderProperty = getTemporaryFolder();
        if (tempFolderProperty != null)
        {
            return tempFolderProperty;
        }
        else
        {
            return storeRoot;
        }
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context, boolean removeFromDataStore) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doArchive(dataset, originalData);
            String dataSetCode = dataset.getDataSetCode();
            if (status.isOK())
            {
                IHierarchicalContent content = null;
                File temp = null;
                IHierarchicalContent archivedContent = null;
                try
                {
                    content = context.getHierarchicalContentProvider().asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    temp = new File(getTempRoot(), STAGING_FOLDER + "/" + dataSetCode);
                    temp.mkdirs();
                    // We want to perform the check if the archived content is correct
                    // (filesizes/checksums)
                    // For this we want to have the archived content locally. If it is not available
                    // locally - we have to retrieve it from the archive first.
                    if (getFileOperationsManager().isHosted())
                    {
                        getFileOperationsManager().retrieveFromDestination(temp, dataset);
                        IHierarchicalContentFactory contentFactory =  new Hdf5AwareHierarchicalContentFactory(
                                dataset.isH5Folders(), dataset.isH5arFolders());
                        archivedContent = contentFactory.asHierarchicalContent(temp, null);
                    } else
                    {
                        archivedContent = getFileOperationsManager().getAsHierarchicalContent(dataset);
                    }

                    IHierarchicalContentNode root = content.getRootNode();
                    IHierarchicalContentNode archivedRoot = archivedContent.getRootNode();

                    status = checkHierarchySizeAndChecksums(root, "", archivedRoot, checksumVerificationCondition);
                } catch (Throwable t)
                {
                    status = Status.createError("Sanity check for data set " + dataSetCode + " failed: " + t);
                } finally
                {
                    if (content != null)
                    {
                        content.close();
                    }
                    if (archivedContent != null)
                    {
                        archivedContent.close();
                    }
                    FileUtils.deleteQuietly(temp);
                }
            }
            statuses.addResult(dataSetCode, status, Operation.ARCHIVE);
        }

        return statuses;
    }

    private static String pathCombine(String part1, String part2)
    {
        if (part1.equals(""))
        {
            return part2;
        }
        if (part2.equals(""))
        {
            return part1;
        }
        return part1 + File.separator + part2;
    }

    public static Status checkHierarchySizeAndChecksums(
            IHierarchicalContentNode node,
            String originalNodeContext,
            IHierarchicalContentNode retrievedNode, ChecksumVerificationCondition checksumVerificationCondition)
    {

        String relativePath = pathCombine(originalNodeContext, node.getRelativePath());
        String relativePathOfRetrieved = retrievedNode.getRelativePath();
        if (relativePath.equals(relativePathOfRetrieved) == false)
        {
            return Status.createError("Different paths: Path in the store is '" + relativePath
                    + "' and in the archive '" + relativePathOfRetrieved + "'.");
        }
        boolean directory = node.isDirectory();
        boolean directoryOfRetrieved = retrievedNode.isDirectory();
        if (directory != directoryOfRetrieved)
        {
            return Status.createError("The path '" + relativePath
                    + "' should be in store and archive either "
                    + "both directories or files but not mixed: In the store it is a "
                    + render(directory) + " but in the archive it is a "
                    + render(directoryOfRetrieved) + ".");
        }
        if (directory)
        {
            List<IHierarchicalContentNode> childNodes = getChildNodes(node);
            List<IHierarchicalContentNode> childNodesOfRetrieved = getChildNodes(retrievedNode);
            int size = childNodes.size();
            int sizeOfRetrieved = childNodesOfRetrieved.size();
            if (size != sizeOfRetrieved)
            {
                return Status.createError("The directory '" + relativePath + "' has in the store "
                        + size + " files but " + sizeOfRetrieved + " in the archive.");
            }
            for (int i = 0; i < size; i++)
            {
                Status status =
                        checkHierarchySizeAndChecksums(childNodes.get(i), originalNodeContext,
                                childNodesOfRetrieved.get(i), checksumVerificationCondition);
                if (status.isError())
                {
                    return status;
                }
            }
        } else
        {
            long fileLength = node.getFileLength();
            long fileLengthOfRetrieved = retrievedNode.getFileLength();
            if (fileLength != fileLengthOfRetrieved)
            {
                return Status.createError("The file '" + relativePath + "' has in the store "
                        + fileLength + " bytes but " + fileLengthOfRetrieved + " in the archive.");
            }
            if (checksumVerificationCondition.verifyChecksum(node))
            {
                long checksum = node.getChecksumCRC32();
                long checksumOfRetrieved = retrievedNode.getChecksumCRC32();
                if (checksum != checksumOfRetrieved)
                {
                    return Status.createError("The file '" + relativePath
                            + "' has in the store the checksum " + renderChecksum(checksum)
                            + " but " + renderChecksum(checksumOfRetrieved) + " in the archive.");
                }
            }
        }
        return Status.OK;
    }

    private static List<IHierarchicalContentNode> getChildNodes(IHierarchicalContentNode node)
    {
        List<IHierarchicalContentNode> childNodes = node.getChildNodes();
        Collections.sort(childNodes, NODE_COMPARATOR);
        return childNodes;
    }

    private static String render(boolean directory)
    {
        return directory ? "directory" : "file";
    }

    private static String renderChecksum(long checksum)
    {
        return String.format("%08X", checksum);
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            context.getUnarchivingPreparation().prepareForUnarchiving(Collections.singletonList(dataset));
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doUnarchive(dataset, originalData);
            statuses.addResult(dataset.getDataSetCode(), status, Operation.UNARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(
            List<? extends IDatasetLocation> datasets)
    {
        return delete(datasets, deleteAction);
    }

    @Override
    protected DatasetProcessingStatuses deletePermanentlyFromArchive(
            List<? extends IDatasetLocation> dataSets)
    {
        return delete(dataSets, DeleteAction.DELETE);
    }

    private DatasetProcessingStatuses delete(List<? extends IDatasetLocation> datasets,
            DeleteAction action)
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (IDatasetLocation dataset : datasets)
        {
            Status status = action.execute(getFileOperationsManager(), dataset);
            statuses.addResult(dataset.getDataSetCode(), status, action.getOperation());
        }
        return statuses;
    }

    @Override
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
            ArchiverTaskContext context)
    {
        File originalData = getDatasetDirectory(context, dataset);
        return getFileOperationsManager().isSynchronizedWithDestination(originalData, dataset);
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
    {
        return getFileOperationsManager().isPresentInDestination(dataset);
    }

    private Status doArchive(DatasetDescription dataset, File originalData)
    {
        return getFileOperationsManager().copyToDestination(originalData, dataset);
    }

    private Status doUnarchive(DatasetDescription dataset, File originalData)
    {
        return getFileOperationsManager().retrieveFromDestination(originalData, dataset);
    }

    private File getDatasetDirectory(ArchiverTaskContext context, DatasetDescription dataset)
    {
        return context.getDirectoryProvider().getDataSetDirectory(dataset);
    }

}
