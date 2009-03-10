/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IPathHandler;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExtractableData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * The class that handles the incoming data set.
 * 
 * @author Bernd Rinn
 */
public final class TransferredDataSetHandler implements IPathHandler, ISelfTestable
{

    private static final String TARGET_NOT_RELATIVE_TO_STORE_ROOT =
            "Target path '%s' is not relative to store root directory '%s'.";

    @Private
    static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    @Private
    static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    @Private
    static final String SUCCESSFULLY_REGISTERED_TEMPLATE =
            "Successfully registered data set '%s' for sample '%s', data set type '%s', "
                    + "experiment '%s' with openBIS service.";

    @Private
    static final String EMAIL_SUBJECT_TEMPLATE = "Success: data set for experiment '%s";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, TransferredDataSetHandler.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TransferredDataSetHandler.class);

    private static final NamedDataStrategy ERROR_DATA_STRATEGY =
            new NamedDataStrategy(DataStoreStrategyKey.ERROR);

    private final IStoreRootDirectoryHolder storeRootDirectoryHolder;

    private final IEncapsulatedOpenBISService limsService;

    private final IDataStrategyStore dataStrategyStore;

    private final IDataSetInfoExtractor dataSetInfoExtractor;

    private final IFileOperations fileOperations;

    private final Lock registrationLock;

    private final IProcedureAndDataTypeExtractor typeExtractor;

    private final IStorageProcessor storageProcessor;

    private final IMailClient mailClient;

    private final String groupCode;

    private final boolean notifySuccessfulRegistration;

    private boolean stopped = false;

    private Map<String, IProcessorFactory> processorFactories =
            Collections.<String, IProcessorFactory> emptyMap();

    private DatabaseInstancePE homeDatabaseInstance;

    public TransferredDataSetHandler(final String groupCode, final IETLServerPlugin plugin,
            final IEncapsulatedOpenBISService limsService, final Properties mailProperties,
            final HighwaterMarkWatcher highwaterMarkWatcher,
            final boolean notifySuccessfulRegistration)

    {
        this(groupCode, plugin.getStorageProcessor(), plugin, limsService, new MailClient(
                mailProperties), notifySuccessfulRegistration);
    }

    TransferredDataSetHandler(final String groupCode,
            final IStoreRootDirectoryHolder storeRootDirectoryHolder,
            final IETLServerPlugin plugin, final IEncapsulatedOpenBISService limsService,
            final IMailClient mailClient, final boolean notifySuccessfulRegistration)

    {
        assert storeRootDirectoryHolder != null : "Given store root directory holder can not be null.";
        assert plugin != null : "IETLServerPlugin implementation can not be null.";
        assert limsService != null : "IEncapsulatedLimsService implementation can not be null.";
        assert mailClient != null : "IMailClient implementation can not be null.";

        this.groupCode = groupCode;
        this.storeRootDirectoryHolder = storeRootDirectoryHolder;
        this.dataSetInfoExtractor = plugin.getDataSetInfoExtractor();
        this.typeExtractor = plugin.getTypeExtractor();
        this.storageProcessor = plugin.getStorageProcessor();
        this.limsService = limsService;
        this.mailClient = mailClient;
        this.dataStrategyStore = new DataStrategyStore(this.limsService, mailClient);
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.registrationLock = new ReentrantLock();
        this.fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
    }

    public final void setProcessorFactories(final Map<String, IProcessorFactory> processorFactories)
    {
        assert processorFactories != null : "Unspecified processor factory map.";
        this.processorFactories = processorFactories;
    }

    /**
     * Returns the lock one needs to hold before one interrupts a data set registration.
     */
    public Lock getRegistrationLock()
    {
        return registrationLock;
    }

    //
    // IPathHandler
    //

    public final void handle(final File isFinishedFile)
    {
        if (stopped)
        {
            return;
        }
        final RegistrationHelper registrationHelper = new RegistrationHelper(isFinishedFile);
        registrationHelper.prepare();
        if (registrationHelper.hasDataSetBeenIdentified())
        {
            registrationHelper.registerDataSet();
        } else
        {
            registrationHelper.moveDataSet();
        }
    }

    public boolean isStopped()
    {
        return stopped;
    }

    //
    // ISelfTestable
    //

    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        final File storeRootDirectory = storeRootDirectoryHolder.getStoreRootDirectory();
        storeRootDirectory.mkdirs();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Checking store root directory '"
                    + storeRootDirectory.getAbsolutePath() + "'.");
        }
        final String errorMessage =
                fileOperations.checkDirectoryFullyAccessible(storeRootDirectory, "store root");
        if (errorMessage != null)
        {
            if (fileOperations.exists(storeRootDirectory) == false)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Store root directory '%s' does not exist.", storeRootDirectory
                                .getAbsolutePath());
            } else
            {
                throw new ConfigurationFailureException(errorMessage);
            }
        }
    }

    public boolean isRemote()
    {
        return true;
    }

    private DatabaseInstancePE getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = limsService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    //
    // Helper class
    //

    private final class RegistrationHelper
    {
        private final File isFinishedFile;

        private final File incomingDataSetFile;

        private final DataSetInformation dataSetInformation;

        private final IDataStoreStrategy dataStoreStrategy;

        private final DataSetType dataSetType;

        private final File storeRoot;

        private BaseDirectoryHolder baseDirectoryHolder;

        private String errorMessageTemplate;

        RegistrationHelper(final File isFinishedFile)
        {
            assert isFinishedFile != null : "Unspecified is-finished file.";
            final String name = isFinishedFile.getName();
            assert name.startsWith(IS_FINISHED_PREFIX) : "A finished file must starts with '"
                    + IS_FINISHED_PREFIX + "'.";
            errorMessageTemplate = DATA_SET_STORAGE_FAILURE_TEMPLATE;
            this.isFinishedFile = isFinishedFile;
            incomingDataSetFile = getIncomingDataSetPath(isFinishedFile);
            dataSetInformation = extractDataSetInformation(incomingDataSetFile);
            if (dataSetInformation.getDataSetCode() == null)
            {
                // Extractor didn't extract an externally generated data set code, so request one
                // from the openBIS server.
                dataSetInformation.setDataSetCode(limsService.createDataSetCode());
            }
            dataStoreStrategy =
                    dataStrategyStore.getDataStoreStrategy(dataSetInformation, incomingDataSetFile);
            dataSetType = typeExtractor.getDataSetType(incomingDataSetFile);
            storeRoot = storageProcessor.getStoreRootDirectory();
        }

        final void prepare()
        {
            final File baseDirectory =
                    createBaseDirectory(dataStoreStrategy, storeRoot, dataSetInformation);
            baseDirectoryHolder =
                    new BaseDirectoryHolder(dataStoreStrategy, baseDirectory, incomingDataSetFile);
        }

        final boolean hasDataSetBeenIdentified()
        {
            return dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED;
        }

        /**
         * This method is only ever called for identified data sets.
         */
        final void registerDataSet()
        {
            final ExperimentPE experiment = dataSetInformation.getExperiment();
            final String procedureTypeCode =
                    typeExtractor.getProcedureType(incomingDataSetFile).getCode();
            final IProcessor processorOrNull = tryCreateProcessor(procedureTypeCode);
            try
            {
                registerDataSetAndInitiateProcessing(experiment, procedureTypeCode, processorOrNull);
                logAndNotifySuccessfulRegistration(experiment.getRegistrator().getEmail());
                if (fileOperations.exists(incomingDataSetFile)
                        && fileOperations.removeRecursivelyQueueing(incomingDataSetFile) == false)
                {
                    operationLog.error("Cannot delete '" + incomingDataSetFile.getAbsolutePath()
                            + "'.");
                }
                deleteAndLogIsFinishedFile();
            } catch (final Throwable throwable)
            {
                rollback(throwable);
            }
        }

        private void rollback(final Throwable throwable) throws Error
        {
            stopped |= throwable instanceof StopException;
            if (stopped)
            {
                Thread.interrupted(); // Ensure the thread's interrupted state is cleared.
                operationLog.warn(String.format("Requested to stop registration of data set '%s'",
                        dataSetInformation));
            } else
            {
                notificationLog.error(String.format(errorMessageTemplate, dataSetInformation),
                        throwable);
            }
            // Errors which are not AssertionErrors leave the system in a state that we don't
            // know and can't trust. Thus we will not perform any operations any more in this
            // case.
            if (throwable instanceof Error && throwable instanceof AssertionError == false)
            {
                throw (Error) throwable;
            }
            storageProcessor.unstoreData(incomingDataSetFile, baseDirectoryHolder
                    .getBaseDirectory());
            if (stopped == false)
            {
                final File baseDirectory =
                        createBaseDirectory(ERROR_DATA_STRATEGY, storeRoot, dataSetInformation);
                baseDirectoryHolder =
                        new BaseDirectoryHolder(ERROR_DATA_STRATEGY, baseDirectory,
                                incomingDataSetFile);
                boolean moveInCaseOfErrorOk =
                        FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder
                                .getTargetFile());
                writeThrowable(throwable);
                if (moveInCaseOfErrorOk)
                {
                    deleteAndLogIsFinishedFile();
                }
            }
        }

        /**
         * Registers the data set and, if possible, initiates the processing.
         */
        private void registerDataSetAndInitiateProcessing(final ExperimentPE experiment,
                final String procedureTypeCode, final IProcessor processorOrNull)
        {
            final File markerFile = createProcessingMarkerFile();
            try
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Start storing data set for sample '"
                            + dataSetInformation.getSampleIdentifier() + "'.");
                }
                final StopWatch watch = new StopWatch();
                watch.start();
                File dataFile =
                        storageProcessor.storeData(experiment, dataSetInformation, typeExtractor,
                                mailClient, incomingDataSetFile, baseDirectoryHolder
                                        .getBaseDirectory());
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Finished storing data set for sample '"
                            + dataSetInformation.getSampleIdentifier() + "', took " + watch);
                }
                assert dataFile != null : "The folder that contains the stored data should not be null.";
                final String relativePath = FileUtilities.getRelativeFile(storeRoot, dataFile);
                assert relativePath != null : String.format(TARGET_NOT_RELATIVE_TO_STORE_ROOT,
                        dataFile.getAbsolutePath(), storeRoot.getAbsolutePath());
                final StorageFormat availableFormat = storageProcessor.getStorageFormat();
                final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();
                // Ensure that we either register the data set and initiate the processing copy or
                // do none of both.
                getRegistrationLock().lock();
                try
                {
                    errorMessageTemplate = DATA_SET_REGISTRATION_FAILURE_TEMPLATE;
                    plainRegisterDataSet(relativePath, procedureTypeCode, availableFormat,
                            isCompleteFlag);
                    deleteAndLogIsFinishedFile();
                    deleteAndLogIsFinishedFile();
                    if (processorOrNull == null)
                    {
                        return;
                    }
                    final StorageFormat requiredFormat =
                            processorOrNull.getRequiredInputDataFormat();
                    boolean canInitiateProcessing = requiredFormat.equals(availableFormat);
                    if (canInitiateProcessing == false
                            && availableFormatMayContainRequiredFormat(availableFormat,
                                    requiredFormat))
                    {
                        // Special case: Check whether we can actually get back the original data.
                        dataFile = storageProcessor.tryGetProprietaryData(dataFile);
                        if (dataFile != null)
                        {
                            canInitiateProcessing = true;
                        }
                    }
                    if (canInitiateProcessing == false)
                    {
                        operationLog.error(String.format(
                                "Configuration Error: mismatch in data set format for data set '%s' between storage "
                                        + "processor and processor (storage processor:"
                                        + " %s, processor: %s) -> No processing initiated.",
                                dataSetInformation, availableFormat, requiredFormat));
                        notificationLog.error(String.format(
                                "Configuration Error: no processing initiated for data set '%s'",
                                dataSetInformation));
                        return;
                    }
                    final ProcessingInstructionDTO processingInstructionOrNull =
                            tryToGetAppropriateProcessingInstruction(experiment
                                    .getProcessingInstructions(), procedureTypeCode);
                    if (processingInstructionOrNull != null)
                    {
                        try
                        {
                            processorOrNull.initiateProcessing(processingInstructionOrNull,
                                    dataSetInformation, dataFile);
                        } catch (final RuntimeException e)
                        {
                            operationLog.error(
                                    "Exception thrown when initiate processing for data set '"
                                            + dataSetInformation + "'.", e);
                            notificationLog
                                    .error("Couldn't initiate processing a data set for sample '"
                                            + dataSetInformation.getSampleIdentifier()
                                            + "' for some reason. For more details see log of the ETL Server.");
                        }
                    }
                } finally
                {
                    getRegistrationLock().unlock();
                }
            } finally
            {
                fileOperations.delete(markerFile);
            }
        }

        private final File createProcessingMarkerFile()
        {
            final File baseDirectory = baseDirectoryHolder.getBaseDirectory();
            final File baseParentDirectory = baseDirectory.getParentFile();
            final String processingDirName = baseDirectory.getName();
            final File markerFile =
                    new File(baseParentDirectory, Constants.PROCESSING_PREFIX + processingDirName);
            try
            {
                fileOperations.createNewFile(markerFile);
            } catch (final WrappedIOException ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Cannot create marker file '%s'.", markerFile.getPath());
            }
            return markerFile;
        }

        private boolean availableFormatMayContainRequiredFormat(
                final StorageFormat availableFormat, final StorageFormat requiredFormat)
        {
            return StorageFormat.PROPRIETARY.equals(requiredFormat)
                    && StorageFormat.BDS_DIRECTORY.equals(availableFormat);
        }

        /**
         * This method is only ever called for unidentified or invalid data sets.
         */
        final void moveDataSet()
        {
            final boolean ok =
                    FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder
                            .getTargetFile());
            if (ok)
            {
                deleteAndLogIsFinishedFile();
            }
        }

        private final void plainRegisterDataSet(final String relativePath,
                final String procedureTypeCode, final StorageFormat storageFormat,
                final BooleanOrUnknown isCompleteFlag)
        {
            final ExternalData data =
                    createExternalData(relativePath, storageFormat, isCompleteFlag);
            // Finally: register the data set in the database.
            limsService.registerDataSet(dataSetInformation, procedureTypeCode, data);
        }

        private void logAndNotifySuccessfulRegistration(final String email)
        {
            String msg = null;
            if (operationLog.isInfoEnabled())
            {
                msg = getSuccessRegistrationMessage();
                operationLog.info(msg);
            }
            if (notifySuccessfulRegistration)
            {
                if (msg == null)
                {
                    msg = getSuccessRegistrationMessage();
                }
                if (notificationLog.isInfoEnabled())
                {
                    notificationLog.info(msg);
                }
                if (StringUtils.isBlank(email) == false)
                {
                    mailClient.sendMessage(String.format(EMAIL_SUBJECT_TEMPLATE, dataSetInformation
                            .getExperimentIdentifier().getExperimentCode()), msg, null, email);
                }
            }
        }

        private final String getSuccessRegistrationMessage()
        {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(String.format(SUCCESSFULLY_REGISTERED_TEMPLATE, dataSetInformation
                    .getDataSetCode(), dataSetInformation.getSampleIdentifier(), dataSetType
                    .getCode(), dataSetInformation.getExperimentIdentifier()));
            buffer.append(OSUtilities.LINE_SEPARATOR);
            buffer.append(OSUtilities.LINE_SEPARATOR);
            appendNameAndObject(buffer, "Experiment Identifier", dataSetInformation
                    .getExperimentIdentifier());
            appendNameAndObject(buffer, "Producer Code", dataSetInformation.getProducerCode());
            appendNameAndObject(buffer, "Production Date", dataSetInformation.getProductionDate());
            appendNameAndObject(buffer, "Parent Data Set", StringUtils
                    .trimToNull(dataSetInformation.getParentDataSetCode()));
            appendNameAndObject(buffer, "Is complete", dataSetInformation.getIsCompleteFlag());
            return buffer.toString();
        }

        private final void appendNameAndObject(final StringBuilder buffer, final String name,
                final Object object)
        {
            if (object != null)
            {
                buffer.append(name).append(":\t").append(object);
                buffer.append(OSUtilities.LINE_SEPARATOR);
            }
        }

        /**
         * From given <var>isFinishedPath</var> gets the incoming data set path and checks it.
         * 
         * @return <code>null</code> if a problem has happened. Otherwise a useful and usable
         *         incoming data set path is returned.
         */
        private final File getIncomingDataSetPath(final File isFinishedPath)
        {
            final File incomingDataSetPath =
                    FileUtilities.removePrefixFromFileName(isFinishedPath, IS_FINISHED_PREFIX);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Getting incoming data set path '%s' from is-finished path '%s'",
                        incomingDataSetPath, isFinishedPath));
            }
            final String errorMsg =
                    fileOperations.checkPathFullyAccessible(incomingDataSetPath,
                            "incoming data set");
            if (errorMsg != null)
            {
                fileOperations.delete(isFinishedPath);
                throw EnvironmentFailureException.fromTemplate(String.format(
                        "Error moving path '%s' from '%s' to '%s': %s", incomingDataSetPath
                                .getName(), incomingDataSetPath.getParent(),
                        storeRootDirectoryHolder.getStoreRootDirectory(), errorMsg));
            }
            return incomingDataSetPath;
        }

        /**
         * From given <var>incomingDataSetPath</var> extracts a <code>DataSetInformation</code>.
         * 
         * @return never <code>null</code> but prefers to throw an exception.
         */
        private final DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
        {
            try
            {
                final DataSetInformation dataSetInfo =
                        dataSetInfoExtractor.getDataSetInformation(incomingDataSetPath);
                if (dataSetInfo.getSampleIdentifier() == null)
                {
                    final String extractorName = dataSetInfoExtractor.getClass().getSimpleName();
                    throw ConfigurationFailureException.fromTemplate(
                            "Data Set Information Extractor '%s' extracted no sample code "
                                    + "for incoming data set '%s' (extractor contract violation).",
                            extractorName, incomingDataSetPath);
                }
                dataSetInfo.setInstanceCode(getHomeDatabaseInstance().getCode());
                dataSetInfo.setInstanceUUID(getHomeDatabaseInstance().getUuid());
                if (dataSetInfo.getGroupCode() == null)
                {
                    dataSetInfo.setGroupCode(groupCode);
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Extracting data set information '%s' from incoming "
                                    + "data set path '%s'", dataSetInfo, incomingDataSetPath));
                }
                return dataSetInfo;
            } catch (final HighLevelException e)
            {
                throw e;
            } catch (final RuntimeException ex)
            {
                throw new EnvironmentFailureException("Error when trying to identify data set '"
                        + incomingDataSetPath.getAbsolutePath() + "'.", ex);
            }
        }

        private final File createBaseDirectory(final IDataStoreStrategy strategy,
                final File baseDir, final DataSetInformation dataSetInfo)
        {
            final File baseDirectory =
                    strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
            baseDirectory.mkdirs();
            if (fileOperations.isDirectory(baseDirectory) == false)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Creating data set base directory '%s' for data set '%s' failed.",
                        baseDirectory.getAbsolutePath(), incomingDataSetFile);
            }
            return baseDirectory;
        }

        private IProcessor tryCreateProcessor(final String procedureTypeCode)
        {
            final IProcessorFactory processorFactory = processorFactories.get(procedureTypeCode);
            if (processorFactory == null)
            {
                return null;
            }
            return processorFactory.createProcessor();
        }

        private ProcessingInstructionDTO tryToGetAppropriateProcessingInstruction(
                final ProcessingInstructionDTO[] processingInstructions,
                final String procedureTypeCode)
        {
            if (processingInstructions != null)
            {
                for (final ProcessingInstructionDTO instruction : processingInstructions)
                {
                    if (instruction.getProcedureTypeCode().equals(procedureTypeCode))
                    {
                        return instruction;
                    }
                }
            }
            return null;
        }

        private final ExternalData createExternalData(final String relativePath,
                final StorageFormat storageFormat, final BooleanOrUnknown isCompleteFlag)
        {
            final ExtractableData extractableData = dataSetInformation.getExtractableData();
            final ExternalData data = BeanUtils.createBean(ExternalData.class, extractableData);
            data.setLocation(relativePath);
            data.setLocatorType(typeExtractor.getLocatorType(incomingDataSetFile));
            data.setDataSetType(typeExtractor.getDataSetType(incomingDataSetFile));
            data.setFileFormatType(typeExtractor.getFileFormatType(incomingDataSetFile));
            data.setStorageFormat(storageFormat);
            data.setComplete(isCompleteFlag);
            return data;
        }

        private final void writeThrowable(final Throwable throwable)
        {
            final String fileName = incomingDataSetFile.getName() + ".exception";
            final File file =
                    new File(baseDirectoryHolder.getTargetFile().getParentFile(), fileName);
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                throwable.printStackTrace(new PrintWriter(writer));
            } catch (final IOException e)
            {
                operationLog.warn(String.format(
                        "Could not write out the exception '%s' in file '%s'.", fileName, file
                                .getAbsolutePath()), e);
            } finally
            {
                IOUtils.closeQuietly(writer);
            }
        }

        private boolean deleteAndLogIsFinishedFile()
        {
            if (fileOperations.exists(isFinishedFile) == false)
            {
                return false;
            }
            final boolean ok = fileOperations.delete(isFinishedFile);
            final String absolutePath = isFinishedFile.getAbsolutePath();
            if (ok == false)
            {
                notificationLog.error(String.format("Removing file '%s' failed.", absolutePath));
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("File '%s' has been removed.", absolutePath));
                }
            }
            return ok;
        }
    }

}
