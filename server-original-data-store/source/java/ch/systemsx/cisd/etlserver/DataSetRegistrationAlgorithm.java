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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.time.DateFormatThreadLocal;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * An algorithm that implements the logic for registration of a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationAlgorithm
{

    public static interface IRollbackDelegate
    {
        public void rollback(DataSetRegistrationAlgorithm algorithm, Throwable ex);
    }

    /**
     * Interface for code that is run to register a new data set.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IDataSetInApplicationServerRegistrator
    {
        public void registerDataSetInApplicationServer(NewExternalData data) throws Throwable;
    }

    /**
     * Object for holding the state necessary for registring a data set.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class DataSetRegistrationAlgorithmState
    {
        private final File incomingDataSetFile;

        private final IEncapsulatedOpenBISService openBisService;

        private final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction;

        private final IPreRegistrationAction preRegistrationAction;

        private final IPostRegistrationAction postRegistrationAction;

        private final DataSetInformation dataSetInformation;

        private final IDataStoreStrategy dataStoreStrategy;

        private final ITypeExtractor typeExtractor;

        private final IStorageProcessorTransactional storageProcessor;

        private IStorageProcessorTransaction transaction;

        private final IFileOperations fileOperations;

        private final IDataSetValidator dataSetValidator;

        private final IMailClient mailClient;

        private final boolean shouldDeleteUnidentified;

        private final String dataStoreCode;

        private final Lock registrationLock;

        private final boolean shouldNotifySuccessfulRegistration;

        private final DataSetType dataSetType;

        private final File storeRoot;

        private final String defaultErrorMessageTemplate;

        public DataSetRegistrationAlgorithmState(File incomingDataSetFile,
                IEncapsulatedOpenBISService openBisService,
                IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
                IPreRegistrationAction preRegistrationAction,
                IPostRegistrationAction postRegistrationAction,
                DataSetInformation dataSetInformation, IDataStoreStrategy dataStoreStrategy,
                ITypeExtractor typeExtractor, IStorageProcessorTransactional storageProcessor,
                IFileOperations fileOperations, IDataSetValidator dataSetValidator,
                IMailClient mailClient, boolean shouldDeleteUnidentified, Lock registrationLock,
                String dataStoreCode, boolean shouldNotifySuccessfulRegistration)
        {
            this.incomingDataSetFile = incomingDataSetFile;
            this.openBisService = openBisService;
            this.cleanAfterwardsAction = cleanAfterwardsAction;
            this.preRegistrationAction = preRegistrationAction;
            this.postRegistrationAction = postRegistrationAction;
            this.dataSetInformation = dataSetInformation;
            this.dataStoreStrategy = dataStoreStrategy;
            this.typeExtractor = typeExtractor;
            this.storageProcessor = storageProcessor;
            this.fileOperations = fileOperations;
            this.dataSetValidator = dataSetValidator;
            this.mailClient = mailClient;
            this.shouldDeleteUnidentified = shouldDeleteUnidentified;
            this.registrationLock = registrationLock;
            this.dataStoreCode = dataStoreCode;
            this.shouldNotifySuccessfulRegistration = shouldNotifySuccessfulRegistration;

            if (dataSetInformation.getDataSetCode() == null)
            {
                // Extractor didn't extract an externally generated data set code, so request one
                // from the openBIS server.
                dataSetInformation.setDataSetCode(openBisService.createPermId());
            }

            this.dataSetType = typeExtractor.getDataSetType(incomingDataSetFile);
            dataSetInformation.setDataSetType(dataSetType);
            dataSetInformation.setDataSetKind(DataSetKind.PHYSICAL);
            this.storeRoot = storageProcessor.getStoreRootDirectory();
            this.defaultErrorMessageTemplate = DATA_SET_STORAGE_FAILURE_TEMPLATE;
        }

        public File getIncomingDataSetFile()
        {
            return incomingDataSetFile;
        }
    }

    static private final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataSetRegistrationAlgorithm.class);

    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationAlgorithm.class);

    public static final String EMAIL_SUBJECT_TEMPLATE = "Success: data set for sample or experiment '%s";

    public static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    public static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    public static final String SUCCESSFULLY_REGISTERED = "Successfully registered data set: [";

    // Immutable State
    private final DataSetRegistrationAlgorithmState state;

    private final IDataSetInApplicationServerRegistrator applicationServerRegistrator;

    private final IRollbackDelegate rollbackDelegate;

    private final File incomingDataSetFile;

    private final DataSetInformation dataSetInformation;

    // State that changes during execution
    private BaseDirectoryHolder baseDirectoryHolder;

    private String errorMessageTemplate;

    public DataSetRegistrationAlgorithm(final DataSetRegistrationAlgorithmState state,
            IRollbackDelegate rollbackDelegate)
    {
        this(state, rollbackDelegate, new DefaultApplicationServerRegistrator(state));
    }

    public DataSetRegistrationAlgorithm(DataSetRegistrationAlgorithmState state,
            IRollbackDelegate rollbackDelegate,
            IDataSetInApplicationServerRegistrator applicationServerRegistrator)
    {
        this.state = state;
        this.rollbackDelegate = rollbackDelegate;
        incomingDataSetFile = state.incomingDataSetFile;
        dataSetInformation = state.dataSetInformation;
        errorMessageTemplate = state.defaultErrorMessageTemplate;
        this.applicationServerRegistrator = applicationServerRegistrator;
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        final File baseDirectory = createBaseDirectory(getDataStoreStrategy(), state.storeRoot);
        baseDirectoryHolder =
                new BaseDirectoryHolder(getDataStoreStrategy(), baseDirectory,
                        state.incomingDataSetFile);
    }

    public boolean hasDataSetBeenIdentified()
    {
        return state.dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED;
    }

    /**
     * Register the data set. This method is only ever called for identified data sets.
     */
    public final List<DataSetInformation> registerDataSet()
    {
        String processorID = getTypeExtractor().getProcessorType(incomingDataSetFile);
        try
        {
            getDataSetValidator().assertValidDataSet(state.dataSetType, incomingDataSetFile);
            registerDataSetAndInitiateProcessing(processorID);
            logAndNotifySuccessfulRegistration(getEmail());
            if (state.fileOperations.exists(incomingDataSetFile)
                    && removeAndLog("clean up failed") == false)
            {
                getOperationLog().error(
                        "Cannot delete '" + incomingDataSetFile.getAbsolutePath() + "'.");
            }

            clean();
            return Collections.singletonList(dataSetInformation);
        } catch (final HighLevelException ex)
        {
            final String userEmailOrNull = dataSetInformation.tryGetUploadingUserEmail();
            boolean deleted = false;
            if (userEmailOrNull != null)
            {
                final String errorMessage = "Error when trying to register data set '"
                        + incomingDataSetFile.getName() + "'.";
                state.mailClient.sendMessage(errorMessage, ex.getMessage(), null, null, userEmailOrNull);
                if (state.shouldDeleteUnidentified)
                {
                    deleted = removeAndLog(errorMessage + " [" + ex.getMessage() + "]");
                }
            }
            if (deleted == false)
            {
                rollback(ex);
            }
            return Collections.emptyList();
        } catch (final Throwable throwable)
        {
            rollback(throwable);
            return Collections.emptyList();
        }
    }

    /**
     * This method is only ever called for unidentified data sets.
     */
    public final void dealWithUnidentifiedDataSet()
    {
        final boolean ok =
                state.shouldDeleteUnidentified ? (removeAndLog(incomingDataSetFile.getName()
                        + " could not be identified.")) : FileRenamer.renameAndLog(
                        incomingDataSetFile, getBaseDirectoryHolder().getTargetFile());
        if (ok)
        {
            clean();
        }
    }

    public final File createBaseDirectory(final IDataStoreStrategy strategy, final File baseDir)
    {
        final File baseDirectory =
                strategy.getBaseDirectory(baseDir, dataSetInformation, state.dataSetType);
        baseDirectory.mkdirs();
        if (state.fileOperations.isDirectory(baseDirectory) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Creating data set base directory '%s' for data set '%s' failed.",
                    baseDirectory.getAbsolutePath(), state.incomingDataSetFile);
        }
        return baseDirectory;
    }

    public DataSetInformation getDataSetInformation()
    {
        return dataSetInformation;
    }

    public BaseDirectoryHolder getBaseDirectoryHolder()
    {
        return baseDirectoryHolder;
    }

    public void setBaseDirectoryHolder(BaseDirectoryHolder baseDirectoryHolder)
    {
        this.baseDirectoryHolder = baseDirectoryHolder;
    }

    public String getErrorMessageTemplate()
    {
        return errorMessageTemplate;
    }

    public File getStoreRoot()
    {
        return state.storeRoot;
    }

    /**
     * Ask the storage processor to rollback. Used by clients of the algorithm.
     */
    public UnstoreDataAction rollbackStorageProcessor(final Throwable throwable)
    {
        if (null == state.transaction)
        {
            return state.storageProcessor.getDefaultUnstoreDataAction(throwable);
        } else
        {
            return state.transaction.rollback(throwable);
        }
    }

    protected boolean clean()
    {
        return state.cleanAfterwardsAction.execute(true);
    }

    private static class DefaultApplicationServerRegistrator implements
            IDataSetInApplicationServerRegistrator
    {
        private final IEncapsulatedOpenBISService openBisService;

        private final DataSetInformation dataSetInformation;

        DefaultApplicationServerRegistrator(DataSetRegistrationAlgorithmState state)
        {
            openBisService = state.openBisService;
            dataSetInformation = state.dataSetInformation;
        }

        @Override
        public void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
        {
            openBisService.registerDataSet(dataSetInformation, data);
        }

    }

    /**
     * Use the applicationServerRegistrator to register the data set with the app server.
     * 
     * @throws Throwable
     */
    private void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
    {
        applicationServerRegistrator.registerDataSetInApplicationServer(data);
    }

    private void rollback(Throwable ex)
    {
        rollbackDelegate.rollback(this, ex);
    }

    /**
     * Registers the data set.
     */
    private void registerDataSetAndInitiateProcessing(final String procedureTypeCode)
            throws Throwable
    {
        final File markerFile = createProcessingMarkerFile();
        try
        {
            String entityDescription = createEntityDescription();
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info("Start storing data set for " + entityDescription + ".");
            }
            final StopWatch watch = new StopWatch();
            watch.start();
            NewExternalData data = createExternalData();
            state.preRegistrationAction.execute(data.getCode(),
                    incomingDataSetFile.getAbsolutePath());

            StorageProcessorTransactionParameters transactionParameters =
                    new StorageProcessorTransactionParameters(dataSetInformation,
                            incomingDataSetFile, baseDirectoryHolder.getBaseDirectory());
            state.transaction = getStorageProcessor().createTransaction(transactionParameters);
            state.transaction.storeData(getTypeExtractor(), state.mailClient, incomingDataSetFile);
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info(
                        "Finished storing data set for " + entityDescription + ", took " + watch);
            }
            File dataFolder = state.transaction.getStoredDataDirectory();
            assert dataFolder != null : "The folder that contains the stored data should not be null.";
            final String relativePath =
                    FileUtilities.getRelativeFilePath(state.storeRoot, dataFolder);
            String absolutePath = dataFolder.getAbsolutePath();
            assert relativePath != null : String.format(
                    TransferredDataSetHandler.TARGET_NOT_RELATIVE_TO_STORE_ROOT, absolutePath,
                    state.storeRoot.getAbsolutePath());
            final StorageFormat availableFormat = getStorageProcessor().getStorageFormat();
            final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();
            // Ensure that we either register the data set and initiate the processing copy or
            // do none of both.
            state.registrationLock.lock();
            try
            {
                errorMessageTemplate = DATA_SET_REGISTRATION_FAILURE_TEMPLATE;
                plainRegisterDataSet(data, relativePath, availableFormat, isCompleteFlag);
                state.postRegistrationAction.execute(data.getCode(), absolutePath);
                clean();
            } finally
            {
                state.registrationLock.unlock();
            }
            state.transaction.commit();
        } finally
        {
            try
            {
                getFileOperations().delete(markerFile);
            } catch (TimeoutExceptionUnchecked ex)
            {
                QueueingPathRemoverService.removeRecursively(markerFile);
            }
        }
    }

    private NewExternalData createExternalData()
    {
        final NewExternalData data = new NewExternalData();
        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setShareId(dataSetInformation.getShareId());
        data.setSpeedHint(dataSetInformation.getSpeedHint());
        data.setLocatorType(getTypeExtractor().getLocatorType(incomingDataSetFile));
        data.setDataSetType(getTypeExtractor().getDataSetType(incomingDataSetFile));
        data.setDataSetKind(dataSetInformation.getDataSetKind());
        data.setFileFormatType(getTypeExtractor().getFileFormatType(incomingDataSetFile));
        data.setMeasured(getTypeExtractor().isMeasuredData(incomingDataSetFile));
        data.setDataStoreCode(state.dataStoreCode);
        return data;
    }

    private void logAndNotifySuccessfulRegistration(final String email)
    {
        String msg = null;
        if (getOperationLog().isInfoEnabled())
        {
            msg = getSuccessRegistrationMessage();
            getOperationLog().info(msg);
        }
        if (state.shouldNotifySuccessfulRegistration)
        {
            if (msg == null)
            {
                msg = getSuccessRegistrationMessage();
            }
            if (getNotificationLog().isInfoEnabled())
            {
                getNotificationLog().info(msg);
            }
            if (StringUtils.isBlank(email) == false)
            {
                String code = dataSetInformation.getSampleCode();
                if (code == null)
                {
                    ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
                    if (experimentIdentifier != null)
                    {
                        code = experimentIdentifier.getExperimentCode();
                    }
                }
                state.mailClient.sendMessage(String.format(EMAIL_SUBJECT_TEMPLATE, code), msg,
                        null, null, email);
            }
        }
    }

    private final String getSuccessRegistrationMessage()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(SUCCESSFULLY_REGISTERED);
        String userID = dataSetInformation.getUploadingUserIdOrNull();
        String userEMail = dataSetInformation.tryGetUploadingUserEmail();
        if (userID != null || userEMail != null)
        {
            appendNameAndObject(buffer, "User", userID == null ? userEMail : userID);
        }
        appendNameAndObject(buffer, "Data Set Code", dataSetInformation.getDataSetCode());
        appendNameAndObject(buffer, "Data Set Type", state.dataSetType.getCode());
        appendNameAndObject(buffer, "Experiment Identifier",
                dataSetInformation.getExperimentIdentifier());
        appendNameAndObject(buffer, "Sample Identifier", dataSetInformation.getSampleIdentifier());
        appendNameAndObject(buffer, "Producer Code", dataSetInformation.getProducerCode());
        appendNameAndObject(buffer, "Production Date",
                formatDate(dataSetInformation.getProductionDate()));
        final List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
        if (parentDataSetCodes.isEmpty() == false)
        {
            appendNameAndObject(buffer, "Parent Data Sets",
                    StringUtils.join(parentDataSetCodes, ' '));
        }
        appendNameAndObject(buffer, "Is complete", dataSetInformation.getIsCompleteFlag());
        buffer.setLength(buffer.length() - 1);
        buffer.append(']');
        return buffer.toString();
    }

    private String formatDate(Date productionDate)
    {
        return productionDate == null ? "" : DateFormatThreadLocal.DATE_FORMAT.get().format(productionDate);
    }

    private final void appendNameAndObject(final StringBuilder buffer, final String name,
            final Object object)
    {
        if (object != null)
        {
            buffer.append(name).append("::").append(object).append(";");
        }
    }

    private String getEmail()
    {
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("Unknown experiment of data set " + dataSetInformation);
        }
        return experiment.getRegistrator().getEmail();
    }

    private boolean removeAndLog(String msg)
    {
        final boolean ok = getFileOperations().removeRecursivelyQueueing(incomingDataSetFile);
        if (getOperationLog().isInfoEnabled())
        {
            getOperationLog().info("Dataset deleted in registration: " + msg);
        }
        return ok;
    }

    private final void plainRegisterDataSet(NewExternalData data, final String relativePath,
            final StorageFormat storageFormat, final BooleanOrUnknown isCompleteFlag)
            throws Throwable
    {
        updateExternalData(data, relativePath, storageFormat, isCompleteFlag);
        // Finally: register the data set in the database.
        registerDataSetInApplicationServer(data);
        state.openBisService.setStorageConfirmed(Collections.singletonList(data.getCode()));
    }

    private final NewExternalData updateExternalData(NewExternalData data,
            final String relativePath, final StorageFormat storageFormat,
            final BooleanOrUnknown isCompleteFlag)
    {
        data.setComplete(isCompleteFlag);
        data.setLocation(relativePath.substring(data.getShareId().length() + 1));
        data.setStorageFormat(storageFormat);
        return data;
    }

    private final File createProcessingMarkerFile()
    {
        final File baseDirectory = baseDirectoryHolder.getBaseDirectory();
        final File baseParentDirectory = baseDirectory.getParentFile();
        final String processingDirName = baseDirectory.getName();
        final File markerFile =
                new File(baseParentDirectory, FileConstants.PROCESSING_PREFIX + processingDirName);
        try
        {
            getFileOperations().createNewFile(markerFile);
        } catch (final IOExceptionUnchecked ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Cannot create marker file '%s'.",
                    markerFile.getPath());
        }
        return markerFile;
    }

    private String createEntityDescription()
    {
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier != null)
        {
            return "sample '" + sampleIdentifier + "'";
        }
        return "experiment '" + dataSetInformation.getExperimentIdentifier() + "'";
    }

    private IFileOperations getFileOperations()
    {
        return state.fileOperations;
    }

    private IStorageProcessorTransactional getStorageProcessor()
    {
        return state.storageProcessor;
    }

    private IDataStoreStrategy getDataStoreStrategy()
    {
        return state.dataStoreStrategy;
    }

    private ITypeExtractor getTypeExtractor()
    {
        return state.typeExtractor;
    }

    private IDataSetValidator getDataSetValidator()
    {
        return state.dataSetValidator;
    }

    private Logger getNotificationLog()
    {
        return notificationLog;
    }

    private Logger getOperationLog()
    {
        return operationLog;
    }

    public File getIncomingDataSetFile()
    {
        return state.incomingDataSetFile;
    }
}
