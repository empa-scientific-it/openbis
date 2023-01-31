/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * A post-registration task replacing uncompressed datasets with new datasets which are HDF5 compressed.
 * 
 * <pre>
 * In order for a data set to be compressed the following prerequisites must be met 
 * 
 * 1) The data set is part of a container. This is required, because the contents of the new data set will 
 * overshadow the existing data set in the container.
 * 2) The data set type must match a configuration property
 * 3) The data set must contain a folder of a specified name which ends with ".h5ar" i.e. instead of 
 * "original" the root directory with contents must be named "original.h5ar". This is requirement 
 * enforces that no links in the UI are broken when the non-compressed data set gets deleted.
 * 
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public class Hdf5CompressingPostRegistrationTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{
    public static final String DATA_SET_TYPES = "data-set-types";

    private static final String HDF5_COMPRESSION_CLEANUP_MARKERS_DIRNAME = "hdf5-cleanup-markers";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            Hdf5CompressingPostRegistrationTask.class);

    /**
     * white list of the data set types to be compressed.
     */
    private final Set<String> processedDataSetTypes;

    public Hdf5CompressingPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
        processedDataSetTypes =
                new HashSet<String>(PropertyUtils.tryGetList(properties, DATA_SET_TYPES));
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new Executor(dataSetCode);
    }

    protected class Executor implements IPostRegistrationTaskExecutor
    {
        protected final String dataSetCode;

        protected Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        /**
         * Callback executed right after hdf5-compressed twin data set has been created and before the existing dataset set has been deleted.
         * <p>
         * Can be overriden by extending classes..
         */
        protected void notifyTwinDataSetCreated(String hdf5DataSetCode)
        {
            // empty by design
        }

        @Override
        public void execute()
        {

            AbstractExternalData externalData = tryGetDataSet(dataSetCode, service);
            if (false == shouldCompressToHdf5(externalData))
            {
                return;
            }

            IHierarchicalContent hierarchicalContent = tryGetHierarchicalContent(externalData);
            if (hierarchicalContent == null)
            {
                return;
            }

            try
            {
                if (false == hasFoldersForCompressing(hierarchicalContent))
                {
                    if (false == hasCompressedFiles(hierarchicalContent))
                    {
                        operationLog.info(String.format(
                                " Data set '%s' meets the criterion for HDF5 compression "
                                        + "in post registration, but it contains no folder named "
                                        + "'*.h5'. HDF5 compression will be skipped...",
                                dataSetCode));
                    } else
                    {
                        // ignore: this is one of the data sets created by the task itself
                    }
                    return;
                }
                String hdf5DataSetCode = service.createPermId();
                operationLog.info(String.format(
                        "The contents of data set '%s' will be hdf5-compressed "
                                + "into a new data set '%s'.", dataSetCode, hdf5DataSetCode));

                PhysicalDataSet dataSet = (PhysicalDataSet) externalData;
                File hdf5DataSetDir =
                        createNewDataSetDirectory(hierarchicalContent, hdf5DataSetCode);
                File cleanupMarker = createCleanupMarker(dataSet.getShareId(), hdf5DataSetDir);
                createCompressedDuplicate(hdf5DataSetDir, hierarchicalContent);
                registerTwinDataset(hdf5DataSetCode, dataSet);
                notifyTwinDataSetCreated(hdf5DataSetCode);
                removeOldDataSet(dataSetCode, "Replaced by '" + hdf5DataSetCode + "'");
                cleanupMarker.delete();

                operationLog.info(String.format(
                        "Data set '%s' is now replaced by its hdf5-compressed "
                                + "counterpart '%s'.", dataSetCode, hdf5DataSetCode));
            } finally
            {
                hierarchicalContent.close();
            }
        }

        private IHierarchicalContent tryGetHierarchicalContent(AbstractExternalData externalData)
        {
            try
            {
                return ServiceProvider.getHierarchicalContentProvider().asContent(externalData);
            } catch (IllegalArgumentException iae)
            {
                operationLog.error("Cannot get hierarchical content for external data "
                        + externalData, iae);
                return null;
            }

        }

        private File createCleanupMarker(String shareId, File hdf5DataSetDir)
        {
            File markerFile = getCleanupMarkerFile(dataSetCode, shareId);
            final File markerDir = markerFile.getParentFile();
            markerDir.mkdirs();
            if (false == markerDir.exists())
            {
                throw new EnvironmentFailureException(
                        "Cannot created HDF5 compression marker directory ");
            }

            FileUtilities.writeToFile(markerFile, hdf5DataSetDir.getAbsolutePath());
            return markerFile;
        }

        private void removeOldDataSet(String dataSetToDelete, String reason)
        {
            service.removeDataSetsPermanently(Collections.singletonList(dataSetToDelete), reason);
        }

        private void createCompressedDuplicate(File stagingDir,
                IHierarchicalContent hierarchicalContent)
        {
            IHierarchicalContentNode root = hierarchicalContent.getRootNode();
            for (IHierarchicalContentNode child : root.getChildNodes())
            {
                File fileOrFolder = child.getFile();
                if (shouldBeCompressed(fileOrFolder))
                {
                    File h5ContainerFile = new File(stagingDir, fileOrFolder.getName());
                    HDF5Container container = new HDF5Container(h5ContainerFile);
                    container.runWriterClient(true,
                            new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(
                                    fileOrFolder));
                } else
                {
                    copy(fileOrFolder, stagingDir);
                }
            }

        }

        private void copy(File fileOrFolder, File toDir)
        {
            try
            {
                if (fileOrFolder.isFile())
                {
                    FileUtils.copyFileToDirectory(fileOrFolder, toDir);
                } else
                {
                    FileUtils.copyDirectoryToDirectory(fileOrFolder, toDir);
                }
            } catch (IOException ioex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
            }
        }

        private File createNewDataSetDirectory(IHierarchicalContent hierarchicalContent,
                String hdf5DataSetCode)
        {
            File existingDataSetRoot = hierarchicalContent.getRootNode().getFile();
            File newDataSetRoot = new File(existingDataSetRoot.getParent(), hdf5DataSetCode);

            if (false == newDataSetRoot.mkdirs())
            {
                throw new EnvironmentFailureException(
                        "Cannot create folder for HDF5 compression data set - '" + newDataSetRoot
                                + "'");
            }
            return newDataSetRoot;
        }

        private boolean shouldBeCompressed(File file)
        {
            return file.isDirectory() && FileUtilities.hasHDF5ContainerSuffix(file);
        }

        private boolean hasFoldersForCompressing(IHierarchicalContent hierarchicalContent)
        {
            IHierarchicalContentNode root = hierarchicalContent.getRootNode();
            for (IHierarchicalContentNode child : root.getChildNodes())
            {
                if (shouldBeCompressed(child.getFile()))
                {
                    return true;
                }
            }
            return false;
        }

        private boolean hasCompressedFiles(IHierarchicalContent hierarchicalContent)
        {
            IHierarchicalContentNode root = hierarchicalContent.getRootNode();
            for (IHierarchicalContentNode child : root.getChildNodes())
            {
                if (FileUtilities.isHDF5ContainerFile(child.getFile()))
                {
                    return true;
                }
            }
            return false;
        }

        private boolean shouldCompressToHdf5(AbstractExternalData dataSet)
        {
            if (dataSet == null)
            {
                operationLog.warn("Data set '" + dataSetCode
                        + "' is no longer available in openBIS."
                        + "Archiving post-registration task will be skipped...");
                return false;
            }
            if (dataSet.tryGetContainer() == null)
            {
                operationLog.info("Data set '" + dataSetCode + "' is not part of a container."
                        + "Compression to HDF5 will be skipped...");
                return false;
            }

            final String dataSetTypeCode = dataSet.getDataSetType().getCode();
            if (false == processedDataSetTypes.contains(dataSetTypeCode))
            {
                operationLog.debug(String.format(
                        "Data set type '%s' is not configured for HDF5 compressing. Skipping "
                                + "compression for data set '%s'...", dataSetTypeCode,
                        dataSetCode));
                return false;
            }
            return true;
        }

        @Override
        public ICleanupTask createCleanupTask()
        {
            return new Hdf5CompressingCleanupTask(dataSetCode);
        }

        private void registerTwinDataset(String hdf5DataSetCode, PhysicalDataSet protoDataSet)
        {

            DataSetInformation dataSetInformation = createDataSetInformation(protoDataSet);
            NewExternalData twinExternalData = createTwin(hdf5DataSetCode, protoDataSet);

            service.registerDataSet(dataSetInformation, twinExternalData);
            ContainerDataSet container = protoDataSet.tryGetContainer();
            DataSetUpdatesDTO containerUpdate = addNewContainedDataSet(container, hdf5DataSetCode);
            service.updateDataSet(containerUpdate);
        }

        private DataSetInformation createDataSetInformation(PhysicalDataSet protoDataSet)
        {
            DataSetInformation result = new DataSetInformation();
            result.setExperimentIdentifier(extractExperimentIdentifier(protoDataSet));
            SampleIdentifier sampleIdentifier = extractSampleIdentifier(protoDataSet);
            if (sampleIdentifier != null)
            {
                result.setSampleIdentifier(sampleIdentifier);
            }
            return result;
        }

        private DataSetUpdatesDTO addNewContainedDataSet(ContainerDataSet container,
                String hdf5DataSetCode)
        {
            DataSetUpdatesDTO updatesDTO = new DataSetUpdatesDTO();
            updatesDTO.setVersion(container.getVersion());

            updatesDTO.setDatasetId(new TechId(container.getId()));
            updatesDTO.setProperties(container.getProperties());
            updatesDTO.setExperimentIdentifierOrNull(extractExperimentIdentifier(container));
            updatesDTO.setSampleIdentifierOrNull(extractSampleIdentifier(container));
            List<String> newContainedCodes = Code.extractCodes(container.getContainedDataSets());
            // the new data set will shadow the contents of the existing
            int hdf5DataSetIndex = Math.max(0, newContainedCodes.indexOf(dataSetCode));
            newContainedCodes.add(hdf5DataSetIndex, hdf5DataSetCode);
            updatesDTO.setModifiedContainedDatasetCodesOrNull(newContainedCodes
                    .toArray(new String[0]));
            return updatesDTO;

        }

        private NewExternalData createTwin(String hdf5DataSetCode, PhysicalDataSet protoDataSet)
        {
            NewExternalData externalData = new NewExternalData();
            externalData.setDataProducerCode(protoDataSet.getDataProducerCode());
            externalData.setDataSetProperties(extractProperties(protoDataSet));
            externalData.setDataSetType(protoDataSet.getDataSetType());
            externalData.setDataSetKind(protoDataSet.getDataSetKind());
            externalData.setDataStoreCode(protoDataSet.getDataStore().getCode());
            externalData.setExperimentIdentifierOrNull(extractExperimentIdentifier(protoDataSet));
            externalData.setMeasured(protoDataSet.isDerived() == false);
            externalData.setParentDataSetCodes(Code.extractCodes(protoDataSet.getParents()));
            externalData.setProductionDate(protoDataSet.getProductionDate());
            externalData.setRegistrationDate(protoDataSet.getRegistrationDate());
            externalData.setSampleIdentifierOrNull(extractSampleIdentifier(protoDataSet));
            externalData.setFileFormatType(protoDataSet.getFileFormatType());
            externalData.setLocation(protoDataSet.getLocation());
            externalData.setLocatorType(protoDataSet.getLocatorType());
            externalData.setShareId(protoDataSet.getShareId());
            externalData.setSpeedHint(protoDataSet.getSpeedHint());
            externalData.setStorageFormat(StorageFormat.PROPRIETARY);

            externalData.setCode(hdf5DataSetCode);
            final File protoDataSetLocation = new File(protoDataSet.getLocation());
            final String newDataSetLocation =
                    new File(protoDataSetLocation.getParentFile(), hdf5DataSetCode).getPath();
            externalData.setLocation(newDataSetLocation);

            return externalData;
        }

        private List<NewProperty> extractProperties(PhysicalDataSet protoDataSet)
        {
            ArrayList<NewProperty> newProperties = new ArrayList<NewProperty>();
            for (IEntityProperty prop : protoDataSet.getProperties())
            {
                NewProperty newProp =
                        new NewProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
                newProperties.add(newProp);
            }
            return newProperties;
        }
    }

    static AbstractExternalData tryGetDataSet(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        List<String> codeAsList = Collections.singletonList(dataSetCode);
        List<AbstractExternalData> dataList = service.listDataSetsByCode(codeAsList);
        if (dataList == null || dataList.isEmpty())
        {
            return null;
        }

        return dataList.get(0);
    }

    private ExperimentIdentifier extractExperimentIdentifier(AbstractExternalData data)
    {
        Experiment experiment = data.getExperiment();
        return experiment == null ? null : ExperimentIdentifierFactory.parse(experiment.getIdentifier());
    }

    private SampleIdentifier extractSampleIdentifier(AbstractExternalData data)
    {
        if (data.getSampleIdentifier() != null)
        {
            return SampleIdentifierFactory.parse(data.getSampleIdentifier());

        } else
        {
            return null;
        }
    }

    private static File getCleanupMarkerFile(String dataSetCode, String shareId)
    {
        File storeRoot = ServiceProvider.getConfigProvider().getStoreRoot();
        File shareRoot = new File(storeRoot, shareId);
        File hdf5CompressionMarkers = new File(shareRoot, HDF5_COMPRESSION_CLEANUP_MARKERS_DIRNAME);
        return new File(hdf5CompressionMarkers, dataSetCode);
    }

    private static class Hdf5CompressingCleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final String dataSetCode;

        Hdf5CompressingCleanupTask(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        @Override
        public void cleanup(ISimpleLogger logger)
        {
            PhysicalDataSet dataSet =
                    (PhysicalDataSet) tryGetDataSet(dataSetCode, ServiceProvider.getOpenBISService());
            if (dataSet != null)
            {
                File cleanupMarkerFile = getCleanupMarkerFile(dataSetCode, dataSet.getShareId());
                if (cleanupMarkerFile.exists())
                {
                    cleanup(dataSet, cleanupMarkerFile);
                    cleanupMarkerFile.delete();
                }
            }
        }

        private void cleanup(PhysicalDataSet dataSet, File cleanupMarkerFile)
        {
            String danglingHdf5DirName = FileUtilities.loadToString(cleanupMarkerFile).trim();
            File danglingDataSetDir = new File(danglingHdf5DirName);
            if (danglingDataSetDir.exists())
            {
                Collection<String> danglingDirContents =
                        getDanglingDirContentsAsSet(danglingDataSetDir);
                Collection<String> dataSetContents = getDataSetContents(dataSet);
                if (dataSetContents.containsAll(danglingDirContents))
                {
                    // dangling dir has known contents, so we can proceed with the deletion
                    operationLog.info("Deleting dangling HDF5 compression folder "
                            + danglingHdf5DirName);
                    FileUtilities.deleteRecursively(danglingDataSetDir);
                } else
                {
                    // marker file points to a directory which has files not present in the
                    // original data set. we'll skip deletion
                    operationLog
                            .error(String
                                    .format("Unexpected set of contents '%s' in dangling directory '%s'. "
                                            + "Only files with the following names '%s' are expected. Deletion of '%s' will be skipped.",
                                            danglingHdf5DirName, danglingDirContents,
                                            dataSetContents, danglingHdf5DirName));
                }
            }
        }

        private Collection<String> getDanglingDirContentsAsSet(File danglingDataSetDir)
        {
            List<File> files = FileUtilities.listFilesAndDirectories(danglingDataSetDir, false);
            List<String> result = new ArrayList<String>();
            for (File f : files)
            {
                result.add(f.getName());
            }
            return result;
        }

        private Collection<String> getDataSetContents(PhysicalDataSet dataSet)
        {
            IHierarchicalContent hierarchicalContent =
                    ServiceProvider.getHierarchicalContentProvider().asContent(dataSet.getCode());
            List<String> result = new ArrayList<String>();
            try
            {
                for (IHierarchicalContentNode node : hierarchicalContent.getRootNode()
                        .getChildNodes())
                {
                    result.add(node.getName());
                }
            } finally
            {
                hierarchicalContent.close();
            }

            return result;
        }

    }

}
