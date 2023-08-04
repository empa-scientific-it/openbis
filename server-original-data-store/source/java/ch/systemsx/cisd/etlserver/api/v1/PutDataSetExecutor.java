/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.api.v1;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.action.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithmRunner;
import ch.systemsx.cisd.etlserver.DataSetRegistrationHelper;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.IPostRegistrationAction;
import ch.systemsx.cisd.etlserver.IPreRegistrationAction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * A helper class for carrying out the put command for creating data sets.
 * <p>
 * It is a data set handler that allows overriding information obtained from the extractors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetExecutor implements IDataSetHandlerRpc
{
    // General State
    private final PutDataSetService service;

    // Command Context State
    private final IETLServerPlugin plugin;

    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File temporaryIncomingDir;

    private final File dataSetDir;

    private final OverridingTypeExtractor overridingTypeExtractor;

    private final IDataSetHandler handler;

    private final IImmutableCopier copier;

    private DataSetInformation overrideOrNull;

    PutDataSetExecutor(PutDataSetService service, IETLServerPlugin plugin, String sessionToken,
            NewDataSetDTO newDataSet, InputStream inputStream)
    {
        this.service = service;
        this.plugin = plugin;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.copier = FastRecursiveHardLinkMaker.tryCreate(RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        this.temporaryIncomingDir = service.createTemporaryIncomingDir(newDataSet.tryDataSetType());
        this.dataSetDir = new File(temporaryIncomingDir, newDataSet.getDataSetFolderName());
        if (dataSetDir.exists())
        {
            deleteDataSetDir();
        }
        if (false == this.dataSetDir.mkdir())
        {
            throw new EnvironmentFailureException("Could not create directory for data set "
                    + newDataSet.getDataSetFolderName());
        }

        overridingTypeExtractor = new OverridingTypeExtractor();
        handler = plugin.getDataSetHandler(this, service.getOpenBisService());
    }

    PutDataSetExecutor(PutDataSetService service, IETLServerPlugin plugin, String sessionToken,
            NewDataSetDTO newDataSet, File temporaryIncomingDir, File dataSet)
    {
        this.service = service;
        this.plugin = plugin;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = null;
        this.copier = FastRecursiveHardLinkMaker.tryCreate(RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        this.temporaryIncomingDir = temporaryIncomingDir;
        this.dataSetDir = dataSet;
        overridingTypeExtractor = new OverridingTypeExtractor();
        handler = plugin.getDataSetHandler(this, service.getOpenBisService());
    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients of the executor are expected to close the input stream when appropriate.
     * 
     * @param noOwnerAllowed <code>true</code> if missing data set owner is allowed. This is the case for custom import.
     * @throws IOException
     */
    public List<DataSetInformation> execute(boolean noOwnerAllowed) throws UserFailureException, IOException
    {
        PutDataSetUtil.checkAccess(sessionToken, getOpenBisService(), newDataSet, noOwnerAllowed);

        writeDataSetToTempDirectory();
        overrideOrNull = null;

        // Register the data set
        try
        {
            return handler.handleDataSet(dataSetDir);
        } finally
        {
            deleteDataSetDir();
        }
    }

    /**
     * Run the put command; this method assumes the data set is already in the rpc-icoming folder in the share.
     */
    public List<DataSetInformation> executeWithoutWriting() throws UserFailureException
    {
        PutDataSetUtil.checkAccess(sessionToken, getOpenBisService(), newDataSet, false);

        overrideOrNull = null;

        // Register the data set
        try
        {
            return handler.handleDataSet(dataSetDir);
        } finally
        {
            deleteDataSetDir();
        }
    }

    @Override
    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        return handleDataSet(dataSet, null);
    }

    @Override
    public List<DataSetInformation> handleDataSet(final File dataSet,
            final DataSetInformation newOverride)
    {
        // Remember the old override, replace it with the override for the execution, then restore
        // it
        DataSetInformation oldOverride = overrideOrNull;
        if (newOverride != null)
        {
            overrideOrNull = newOverride;
        }

        String dataSetTypeCodeOrNull = tryGetDataSetTypeCode(newOverride);
        String shareId = service.getShareId(dataSetTypeCodeOrNull);
        RegistrationHelper helper = new RegistrationHelper(service, shareId, plugin, dataSet);

        new DataSetRegistrationAlgorithmRunner(
                helper,
                new DataSetRegistrationAlgorithmRunner.IDataSetRegistrationAlgorithmRunnerDelegate()
                    {

                        @Override
                        public void didNotIdentifyDataSet()
                        {
                            throw new UserFailureException("Could not find owner:\n\t"
                                    + newOverride + "\nfor data set:\n\t" + dataSet);
                        }
                    }).runAlgorithm();

        overrideOrNull = oldOverride;

        return Collections.singletonList(helper.getDataSetInformation());
    }

    public String tryGetDataSetTypeCode(final DataSetInformation newOverride)
    {
        String dataSetTypeCodeOrNull =
                (newOverride != null && newOverride.getDataSetType() != null) ? newOverride
                        .getDataSetType().getCode() : null;
        return dataSetTypeCodeOrNull;
    }

    @Override
    public List<DataSetInformation> linkAndHandleDataSet(File dataSetComponent,
            DataSetInformation newOverride)
    {
        File incomingDir = service.getIncomingDir(tryGetDataSetTypeCode(newOverride));
        // Make a hard link to the file within the data set
        final Status status = copier.copyImmutably(dataSetComponent, incomingDir, null);
        if (status.isError())
        {
            throw new EnvironmentFailureException("Couldn't create a hard-link copy of '"
                    + dataSetComponent.getAbsolutePath() + "' in folder '"
                    + incomingDir.getAbsolutePath() + "'. [" + status.isError() + "]");
        }

        File linkedFile = new File(incomingDir, dataSetComponent.getName());

        // Register the component of the data set
        try
        {
            return handleDataSet(linkedFile, newOverride);
        } finally
        {
            if (linkedFile.isDirectory())
            {
                deleteDir(linkedFile);
            } else if (linkedFile.exists())
            {
                // this should have been moved already -- cleanup
                deleteFile(linkedFile);
            }

        }
    }

    @Override
    public SessionContextDTO getSessionContext()
    {
        return getOpenBisService().tryGetSession(sessionToken);
    }

    @Override
    public File getFileForDataSet(PhysicalDataSet dataSet, String shareId)
    {
        File share = new File(service.getStoreRootDirectory(), shareId);
        File dataSetFile = new File(share, dataSet.getLocation());
        return DefaultStorageProcessor.getOriginalDirectory(dataSetFile);
    }

    public String getDataStoreCode()
    {
        return service.getDataStoreCode();
    }

    @Override
    public DataSetOwner getDataSetOwner()
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        return owner;
    }

    @Override
    public DataSetInformation getCallerDataSetInformation()
    {
        DataSetInformation dataSetInfo = new DataSetInformation();
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
                dataSetInfo.setExperimentIdentifier(tryExperimentIdentifier());
                break;
            case SAMPLE:
                SampleIdentifier sampleId = trySampleIdentifier();
                dataSetInfo.setSampleIdentifier(sampleId);
                break;
            case DATA_SET:
                String dataSetCode = tryGetDataSetCode();

                AbstractExternalData parentDataSet = getOpenBisService().tryGetDataSet(dataSetCode);
                if (parentDataSet != null)
                {
                    if (parentDataSet.getExperiment() != null)
                    {
                        dataSetInfo.setExperiment(parentDataSet.getExperiment());
                    }
                    if (parentDataSet.getSample() != null)
                    {
                        dataSetInfo.setSample(parentDataSet.getSample());
                    }
                    ArrayList<String> parentDataSetCodes = new ArrayList<String>();
                    // Add this parent as the first parent
                    parentDataSetCodes.add(parentDataSet.getCode());
                    parentDataSetCodes.addAll(dataSetInfo.getParentDataSetCodes());
                    dataSetInfo.setParentDataSetCodes(parentDataSetCodes);
                    break;
                }
                break;
        }
        String typeCode = newDataSet.tryDataSetType();
        if (null != typeCode)
        {
            dataSetInfo.setDataSetType(new DataSetType(typeCode));
        }
        dataSetInfo.setDataSetKind(DataSetKind.PHYSICAL);

        Map<String, Serializable> primitiveProps = newDataSet.getProperties();
        if (false == primitiveProps.isEmpty())
        {
            ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
            for (String key : primitiveProps.keySet())
            {
                properties.add(new NewProperty(key, primitiveProps.get(key)));
            }
            dataSetInfo.setDataSetProperties(properties);
        }

        // Add any parents to the end of the list of parents
        ArrayList<String> parentDataSetCodes = new ArrayList<String>();
        parentDataSetCodes.addAll(dataSetInfo.getParentDataSetCodes());
        parentDataSetCodes.addAll(newDataSet.getParentDataSetCodes());
        dataSetInfo.setParentDataSetCodes(parentDataSetCodes);

        return dataSetInfo;
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            if (fileInfo.isDirectory())
            {
                // Just make the directory
                File file = new File(dataSetDir, fileInfo.getPathInDataSet());
                file.mkdir();
            } else
            {
                // Download the file -- the directory should have already been made
                OutputStream output = getOutputStream(fileInfo);
                imagesWriter.writeNextBlock(output);
                output.flush();
                output.close();
            }
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());

        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex)
        {
            // This should not happen since we just created the directory to store the new file in
            throw new EnvironmentFailureException("Could not save file " + file, ex);
        }
        return new BufferedOutputStream(fos);
    }

    private void deleteDataSetDir()
    {
        deleteDir(temporaryIncomingDir);
    }

    private void deleteDir(File dirToDelete)
    {
        try
        {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException ex)
        {
            getOperationLog().error("Could not delete data set directory " + dirToDelete, ex);
            ex.printStackTrace();
        }
    }

    private void deleteFile(File fileToDelete)
    {
        FileUtils.deleteQuietly(fileToDelete);
    }

    private ExperimentIdentifier tryExperimentIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
                return new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            default:
                return null;
        }
    }

    private SampleIdentifier trySampleIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case SAMPLE:
                return new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();
            default:
                return null;
        }
    }

    private String tryGetDataSetCode()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case DATA_SET:
                return owner.getIdentifier();
            default:
                return null;
        }
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        return service.getOpenBisService();
    }

    private Logger getOperationLog()
    {
        return service.getOperationLog();
    }

    public OverridingTypeExtractor getOverridingTypeExtractor()
    {
        return overridingTypeExtractor;
    }

    private static class PostRegistrationAction implements IPostRegistrationAction
    {

        @Override
        public boolean execute(String dataSetCode, String dataSetAbsolutePathInStore)
        {
            return true;// do nothing
        }
    }

    private static class PreRegistrationAction implements IPreRegistrationAction
    {

        @Override
        public boolean execute(String dataSetCode, String dataSetAbsolutePathInStore)
        {
            return true;// do nothing
        }
    }

    private static class CleanAfterwardsAction extends AbstractDelegatedActionWithResult<Boolean>
    {
        private CleanAfterwardsAction()
        {
            super(true);
        }
    }

    /**
     * Implementation of ITypeExtractor that overrides the plugin's type extractor only if the caller has provided an override in the
     * {@link NewDataSetDTO}.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class OverridingTypeExtractor implements ITypeExtractor
    {
        private final ITypeExtractor pluginTypeExtractor;

        OverridingTypeExtractor()
        {
            pluginTypeExtractor = plugin.getTypeExtractor();
        }

        @Override
        public DataSetType getDataSetType(File incomingDataSetPath)
        {
            if (null != overrideOrNull)
            {
                DataSetType dataSetType = overrideOrNull.getDataSetType();
                if (null != dataSetType)
                {
                    return dataSetType;
                }
            }
            return pluginTypeExtractor.getDataSetType(incomingDataSetPath);
        }

        @Override
        public FileFormatType getFileFormatType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getFileFormatType(incomingDataSetPath);
        }

        @Override
        public LocatorType getLocatorType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getLocatorType(incomingDataSetPath);
        }

        @Override
        public String getProcessorType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getProcessorType(incomingDataSetPath);
        }

        @Override
        public boolean isMeasuredData(File incomingDataSetPath)
        {
            return pluginTypeExtractor.isMeasuredData(incomingDataSetPath);
        }

    }

    /**
     * An implementation of the registration algorithm that gets its state from the executor.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class RegistrationHelper extends DataSetRegistrationHelper implements
            IDataSetInfoExtractor
    {
        /**
         * @param service The provider of global state for the data set registration algorithm
         * @param plugin The provider of the storage processor
         * @param incomingDataSetFile The data set to register
         */
        public RegistrationHelper(PutDataSetService service, String shareId,
                IETLServerPlugin plugin, File incomingDataSetFile)
        {
            super(incomingDataSetFile, shareId, new CleanAfterwardsAction(),
                    new PreRegistrationAction(), new PostRegistrationAction());
        }

        @Override
        protected IDataSetInfoExtractor getDataSetInfoExtractor()
        {
            return this;
        }

        @Override
        protected IDataSetValidator getDataSetValidator()
        {
            return service.getDataSetValidator();
        }

        @Override
        protected String getDataStoreCode()
        {
            return service.getDataStoreCode();
        }

        @Override
        protected IDataStrategyStore getDataStrategyStore()
        {
            return service.getDataStrategyStore();
        }

        @Override
        protected IFileOperations getFileOperations()
        {
            return FileOperations.getMonitoredInstanceForCurrentThread();
        }

        @Override
        protected DatabaseInstance getHomeDatabaseInstance()
        {
            return service.getHomeDatabaseInstance();
        }

        @Override
        protected IMailClient getMailClient()
        {
            return service.getMailClient();
        }

        @Override
        protected Logger getNotificationLog()
        {
            return service.getOperationLog();
        }

        @Override
        protected IEncapsulatedOpenBISService getOpenBisService()
        {
            return service.getOpenBisService();
        }

        @Override
        protected Logger getOperationLog()
        {
            return service.getOperationLog();
        }

        @Override
        protected Lock getRegistrationLock()
        {
            return service.getRegistrationLock();
        }

        @Override
        protected IStorageProcessorTransactional getStorageProcessor()
        {
            return plugin.getStorageProcessor();
        }

        @Override
        protected ITypeExtractor getTypeExtractor()
        {
            return getOverridingTypeExtractor();
        }

        @Override
        protected void rollback(Throwable ex)
        {
            registrationAlgorithm.rollbackStorageProcessor(ex);
            if (ex instanceof HighLevelException)
            {
                throw (HighLevelException) ex;
            }
            throw new EnvironmentFailureException("Could not register data set " + newDataSet, ex);
        }

        @Override
        protected boolean shouldDeleteUnidentified()
        {
            return true;
        }

        @Override
        protected boolean shouldNotifySuccessfulRegistration()
        {
            return false;
        }

        @Override
        protected DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
        {
            DataSetInformation dataSetInfo = super.extractDataSetInformation(incomingDataSetPath);
            if (null == dataSetInfo)
            {
                return null;
            }

            final SessionContextDTO session =
                    service.getOpenBisService().tryGetSession(sessionToken);
            dataSetInfo.setUploadingUserId(session.getUserName());
            return dataSetInfo;
        }

        @Override
        public DataSetInformation getDataSetInformation(File incomingDataSetPath,
                IEncapsulatedOpenBISService openbisService) throws UserFailureException,
                EnvironmentFailureException
        {
            if (null != overrideOrNull)
            {
                return overrideOrNull;
            }

            DataSetInformation dataSetInfo =
                    plugin.getDataSetInfoExtractor().getDataSetInformation(incomingDataSetPath,
                            openbisService);
            DataSetOwner owner = getDataSetOwner();
            if (owner != null)
            {
                switch (owner.getType())
                {
                    case EXPERIMENT:
                        dataSetInfo.setExperimentIdentifier(tryExperimentIdentifier());
                        break;
                    case SAMPLE:
                        SampleIdentifier sampleId = trySampleIdentifier();
                        dataSetInfo.setSampleIdentifier(sampleId);
                        break;
                    case DATA_SET:
                        String dataSetCode = tryGetDataSetCode();

                        AbstractExternalData parentDataSet = openbisService.tryGetDataSet(dataSetCode);
                        if (parentDataSet != null)
                        {
                            if (parentDataSet.getExperiment() != null)
                            {
                                dataSetInfo.setExperiment(parentDataSet.getExperiment());
                            }
                            if (parentDataSet.getSample() != null)
                            {
                                dataSetInfo.setSample(parentDataSet.getSample());
                            }
                            ArrayList<String> parentDataSetCodes = new ArrayList<String>();
                            parentDataSetCodes.add(parentDataSet.getCode());
                            parentDataSetCodes.addAll(dataSetInfo.getParentDataSetCodes());
                            dataSetInfo.setParentDataSetCodes(parentDataSetCodes);
                            break;
                        }
                }
            }
            String typeCode = newDataSet.tryDataSetType();
            if (null != typeCode)
            {
                dataSetInfo.setDataSetType(new DataSetType(typeCode));
            }
            dataSetInfo.setDataSetKind(DataSetKind.PHYSICAL);

            Map<String, Serializable> primitiveProps = newDataSet.getProperties();
            if (false == primitiveProps.isEmpty())
            {
                ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
                for (String key : primitiveProps.keySet())
                {
                    properties.add(new NewProperty(key, primitiveProps.get(key)));
                }
                dataSetInfo.setDataSetProperties(properties);
            }
            return dataSetInfo;
        }
    }
}
