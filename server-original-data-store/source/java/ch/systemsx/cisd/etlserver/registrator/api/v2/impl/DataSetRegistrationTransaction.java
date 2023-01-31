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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.lemnik.eodsql.DynamicTransactionQuery;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.common.resource.Resources;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.IEntityOperationService;
import ch.systemsx.cisd.etlserver.registrator.IncomingFileDeletedBeforeRegistrationException;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDSSRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IExperimentUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IMetaproject;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISpace;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IVocabulary;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IVocabularyTerm;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AbstractTransactionState.CommitedTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AbstractTransactionState.LiveTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AbstractTransactionState.RecoveryPendingTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.AbstractTransactionState.RolledbackTransactionState;
import ch.systemsx.cisd.etlserver.registrator.recovery.IDataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IAttachmentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISpaceImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction<T extends DataSetInformation> implements
        IDataSetRegistrationTransactionV2, DataSetStorageAlgorithmRunner.IRollbackDelegate<T>,
        DataSetStorageAlgorithmRunner.IDataSetInApplicationServerRegistrator<T>,
        DataSetRegistrationContext.IHolder
{
    public static final String SUCCESS_MESSAGE = "Successfully committed transaction";

    private static final String ROLLBACK_QUEUE1_FILE_NAME_SUFFIX = "rollBackQueue";

    private final static String ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationTransaction.class);

    public static synchronized RollbackStack[] findRollbackStacks(File rollBackStackParentFolder)
    {
        File[] rollbackQueueFiles = rollBackStackParentFolder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(ROLLBACK_QUEUE1_FILE_NAME_SUFFIX);
                }
            });

        RollbackStack[] rollbackStacks = new RollbackStack[rollbackQueueFiles.length];

        for (int i = 0; i < rollbackQueueFiles.length; i++)
        {
            rollbackStacks[i] = new RollbackStack(rollbackQueueFiles[i]);
        }
        return rollbackStacks;
    }

    /**
     * Check if there are any uncompleted transactions and roll them back. To be called during startup of a thread.
     */
    public static synchronized void rollbackDeadTransactions(File rollBackStackParentFolder)
    {

        RollbackStack[] stacks = findRollbackStacks(rollBackStackParentFolder);
        for (RollbackStack stack : stacks)
        {
            if (stack.isLockedState())
            {
                operationLog.info("Found rollback stack in locked state: "
                        + stack.getCommandsFile() + ". Not Rolling back.");
            } else
            {
                operationLog.info("Found dead rollback stack: " + stack.getCommandsFile()
                        + ". Rolling back.");

                try
                {
                    stack.rollbackAll();
                } catch (Throwable ex)
                {
                    // This should ever happen since rollbackAll should handle execptions, but is
                    // here
                    // as a safeguard.
                    operationLog.error("Encountered error rolling back transaction:", ex);
                }
                stack.discard();
            }
        }
    }

    private static AtomicInteger ai = new AtomicInteger();

    /**
     * Create a new persistent rollback stack in the supplied folder.
     */
    private static RollbackStack createNewRollbackStack(File rollBackStackParentFolder)
    {
        // Add a unique number to the prefix to distinguish between rollback stacks created in the
        // same millisecond.
        String fileNamePrefix =
                DateFormatUtils.format(new Date(), ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN)
                        + "-" + ai.getAndIncrement() + "-";
        return new RollbackStack(new File(rollBackStackParentFolder, fileNamePrefix
                + ROLLBACK_QUEUE1_FILE_NAME_SUFFIX));
    }

    private AbstractTransactionState<T> state;

    private DataSetRegistrationContext registrationContext;

    // The registration service that owns this transaction
    private final DataSetRegistrationService<T> registrationService;

    // The interface to openBIS
    private final IEncapsulatedOpenBISService openBisService;

    private Resources resources = new Resources();

    public DataSetRegistrationTransaction(File rollBackStackParentFolder, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory, String userSessionToken)
    {
        this(createNewRollbackStack(rollBackStackParentFolder), workingDirectory, stagingDirectory,
                registrationService, registrationDetailsFactory, userSessionToken);
    }

    DataSetRegistrationTransaction(RollbackStack rollbackStack, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory, String userSessionToken)
    {
        state =
                new LiveTransactionState<T>(this, rollbackStack, workingDirectory,
                        stagingDirectory, registrationService, registrationDetailsFactory);
        this.registrationService = registrationService;
        this.openBisService =
                this.registrationService.getRegistratorContext().getGlobalState()
                        .getOpenBisService();
        this.registrationContext =
                new DataSetRegistrationContext(new DataSetRegistrationPersistentMap(),
                        this.registrationService.getRegistratorContext().getGlobalState(), userSessionToken);
        DssRegistrationLogger dssRegistrationLog = this.registrationService.getDssRegistrationLog();
        dssRegistrationLog.info(operationLog, "Start registration");
    }

    @Override
    public String getOpenBisServiceSessionToken()
    {
        return openBisService.getSessionToken();
    }

    @Override
    public IDSSRegistrationLogger getLogger()
    {
        return this.registrationService.getDssRegistrationLog();
    }

    @Override
    public String getUserId()
    {
        return getStateAsLiveState().getUserId();
    }

    @Override
    public void setUserId(String userIdOrNull)
    {
        getStateAsLiveState().setUserId(userIdOrNull);
    }

    @Override
    public IDataSet createNewDataSet()
    {
        return getStateAsLiveState().createNewDataSet();
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType)
    {
        return getStateAsLiveState().createNewDataSet(dataSetType);
    }

	@Override
	public IDataSet createNewDataSet(String dataSetType, DataSetKind datasetKindOrNull)
	{
        return getStateAsLiveState().createNewDataSet(dataSetType, datasetKindOrNull);
	}

	@Override
    public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
    {
        return getStateAsLiveState().createNewDataSet(dataSetType, dataSetCode);
    }

	@Override
	public IDataSet createNewDataSet(String dataSetType, String dataSetCode, DataSetKind datasetKindOrNull)
	{
        return getStateAsLiveState().createNewDataSet(dataSetType, dataSetCode, datasetKindOrNull);
	}

    public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
    {
        return getStateAsLiveState().createNewDataSet(registrationDetails);
    }

    public IDataSet createNewDataSet(IDataSetRegistrationDetailsFactory<T> factory,
            String dataSetType)
    {
        return getStateAsLiveState().createNewDataSet(factory, dataSetType, null);
    }

    @Override
    public IDataSetImmutable getDataSet(String dataSetCode)
    {
        return getSearchServiceUnfiltered().getDataSet(dataSetCode);
    }

    @Override
    public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
    {
        return getStateAsLiveState().getDataSetForUpdate(dataSetCode);
    }

    @Override
    public IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet)
    {
        return getStateAsLiveState().makeDataSetMutable(dataSet);
    }

    @Override
    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        return getSearchServiceUnfiltered().getSample(sampleIdentifierString);
    }

    @Override
    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return getStateAsLiveState().getSampleForUpdate(sampleIdentifierString);
    }

    @Override
    public ISample makeSampleMutable(ISampleImmutable sample)
    {
        return getStateAsLiveState().makeSampleMutable(sample);
    }

    @Override
    public IExperimentUpdatable getExperimentForUpdate(String experimentIdentifierString)
    {
        return getStateAsLiveState().getExperimentForUpdate(experimentIdentifierString);
    }

    @Override
    public IExperimentUpdatable makeExperimentMutable(IExperimentImmutable experiment)
    {
        return getStateAsLiveState().makeExperimentMutable(experiment);
    }

    @Override
    public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
    {
        return getStateAsLiveState().createNewSample(sampleIdentifierString, sampleTypeCode);
    }

    @Override
    public ISample createNewSampleWithGeneratedCode(String spaceCode, String sampleTypeCode)
    {
        return getStateAsLiveState().createNewSampleWithGeneratedCode(spaceCode, sampleTypeCode);
    }

    @Override
    public ISample createNewProjectSampleWithGeneratedCode(String projectIdentifier, String sampleTypeCode)
    {
        return getStateAsLiveState().createNewProjectSampleWithGeneratedCode(projectIdentifier, sampleTypeCode);
    }

    @Override
    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        return getSearchServiceUnfiltered().getExperiment(experimentIdentifierString);
    }

    @Override
    public IExperiment createNewExperiment(String experimentIdentifierString,
            String experimentTypeCode)
    {
        return getStateAsLiveState().createNewExperiment(experimentIdentifierString,
                experimentTypeCode);
    }

    @Override
    public IProject createNewProject(String projectIdentifier)
    {
        return getStateAsLiveState().createNewProject(projectIdentifier);
    }

    @Override
    public IProjectImmutable getProject(String projectIdentifier)
    {
        return getSearchServiceUnfiltered().getProject(projectIdentifier);
    }

    @Override
    public IProject getProjectForUpdate(String projectIdentifier)
    {
        return getStateAsLiveState().getProjectForUpdate(projectIdentifier);
    }

    @Override
    public IProject makeProjectMutable(IProjectImmutable project)
    {
        return getStateAsLiveState().makeProjectMutable(project);
    }

    @Override
    public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
    {
        return getStateAsLiveState().createNewSpace(spaceCode, spaceAdminUserIdOrNull);
    }

    @Override
    public ISpaceImmutable getSpace(String spaceCode)
    {
        return getSearchServiceUnfiltered().getSpace(spaceCode);
    }

    @Override
    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        return getSearchServiceUnfiltered().getMaterial(materialCode, materialType);
    }

    @Override
    public IMaterialImmutable getMaterial(String identifier)
    {
        return getSearchServiceUnfiltered().getMaterial(identifier);
    }

    @Override
    public IMaterial getMaterialForUpdate(String materialCode, String materialType)
    {
        return getStateAsLiveState().getMaterialForUpdate(materialCode, materialType);
    }

    @Override
    public IMaterial getMaterialForUpdate(String identifier)
    {
        MaterialIdentifier materialId = MaterialIdentifier.tryParseIdentifier(identifier);
        if (materialId == null)
        {
            throw new IllegalArgumentException("Incorrect material identifier format " + identifier
                    + ". Expected code (type)");
        }
        return getMaterialForUpdate(materialId.getCode(), materialId.getTypeCode());
    }

    @Override
    public IMaterial makeMaterialMutable(IMaterialImmutable material)
    {
        return getStateAsLiveState().makeMaterialMutable(material);
    }

    @Override
    public IMaterial createNewMaterial(String materialCode, String materialType)
    {
        return getStateAsLiveState().createNewMaterial(materialCode, materialType);
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(
            String externalDataManagementSystemCode)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem result =
                openBisService.tryGetExternalDataManagementSystem(externalDataManagementSystemCode);
        return (null == result) ? null : new ExternalDataManagementSystemImmutable(result);
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description)
    {
        if (getUserId() == null)
        {
            throw new IllegalArgumentException(
                    "Cannot create a new metaproject when user is not available nor specified. ");
        }
        return getStateAsLiveState().createNewMetaproject(name, description, getUserId());
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description, String ownerId)
    {
        if (getUserId() != null && false == getUserId().equals(ownerId))
        {
            throw new IllegalArgumentException(
                    "Cannot create metaproject for different user then the current one.");
        }
        return getStateAsLiveState().createNewMetaproject(name, description, ownerId);
    }

    @Override
    public IMetaproject getMetaproject(String name)
    {
        if (getUserId() == null)
        {
            throw new IllegalArgumentException(
                    "Cannot get a metaproject when user is not available nor specified. ");
        }
        return getStateAsLiveState().getMetaproject(name, getUserId());
    }

    @Override
    public IMetaproject getMetaproject(String name, String ownerId)
    {
        if (getUserId() != null && false == getUserId().equals(ownerId))
        {
            throw new IllegalArgumentException(
                    "Cannot get metaproject for different user then the current one.");
        }
        return getStateAsLiveState().getMetaproject(name, ownerId);
    }

    @Override
    public IVocabularyImmutable getVocabulary(String code)
    {
        return getSearchService().getVocabulary(code);
    }

    @Override
    public IVocabulary getVocabularyForUpdate(String code)
    {
        return getStateAsLiveState().getVocabularyForUpdate(code);
    }

    @Override
    public IVocabularyTerm createNewVocabularyTerm()
    {
        return new VocabularyTerm();
    }

    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return getStateAsLiveState().moveFile(src, dst);
    }

    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return getStateAsLiveState().moveFile(src, dst, dstInDataset);
    }

    @Override
    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return getStateAsLiveState().createNewDirectory(dst, dirName);
    }

    @Override
    public String createNewFile(IDataSet dst, String fileName)
    {
        return getStateAsLiveState().createNewFile(dst, fileName);
    }

    @Override
    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return getStateAsLiveState().createNewFile(dst, dstInDataset, fileName);
    }

    @Override
    public String createNewLink(IDataSet dst, String dstInDataset, String linkName, String linkTarget)
    {
        return getStateAsLiveState().createNewLink(dst, dstInDataset, linkName, linkTarget);
    }

    public void deleteFile(String src)
    {
        getStateAsLiveState().deleteFile(src);
    }

    @Override
    public DataSetRegistrationContext getRegistrationContext()
    {
        return registrationContext;
    }

    /**
     * Commit the transaction. Does not throw exceptions if the commit fails on some datasets!
     * 
     * @return true if any datasets have been commited, false otherwise.
     */
    public boolean commit()
    {
        // No need to commit again
        if (state.isCommitted())
        {
            return false;
        }
        LiveTransactionState<T> liveState = getStateAsLiveState();
        boolean commitSucceeded = liveState.commit();

        // The attempt to commit the live state could have changed the state to rolledback
        if (state.isRolledback() || state.isRecoveryPending())
        {
            return false;
        }

        // Advance to the committed state.
        state = new CommitedTransactionState<T>(liveState);
        invokeDidCommitTransaction();

        if (commitSucceeded)
        {
            operationLog.info(SUCCESS_MESSAGE);
        }
        return commitSucceeded;
    }

    private void invokeDidCommitTransaction()
    {
        try
        {
            registrationService.executePostCommit(this);
        } catch (Throwable t)
        {
            DssRegistrationLogger dssRegistrationLog = registrationService.getDssRegistrationLog();
            dssRegistrationLog.warn(operationLog, "Failed to invoke post transaction hook:" + t.getMessage(), t);
        }
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
     */
    public void rollback()
    {
        // No need to rollback again
        if (state.isRolledback() || state.isRecoveryPending())
        {
            return;
        }

        LiveTransactionState<T> liveState = getStateAsLiveState();
        liveState.rollback();

        state = new RolledbackTransactionState<T>(liveState);
    }

    /**
     * Delegate method called by the {@link DataSetStorageAlgorithmRunner}.
     */
    @Override
    public void didRollbackStorageAlgorithmRunner(DataSetStorageAlgorithmRunner<T> algorithm,
            Throwable ex, ErrorType errorType)
    {
        if (false == ex instanceof IncomingFileDeletedBeforeRegistrationException)
        {
            // Don't log if the file was deleted before registration, we already know.
            operationLog.error("The error ", ex);
        }
        IDataSetStorageRecoveryManager storageRecoveryManager =
                registrationService.getRegistratorContext().getGlobalState()
                        .getStorageRecoveryManager();

        storageRecoveryManager.removeCheckpoint(algorithm);

        rollback();
        registrationService.didRollbackTransaction(this, algorithm, ex, errorType);
    }

    @Override
    public void markReadyForRecovery(DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        registrationService.registerNonFatalError(ex);
        state = new RecoveryPendingTransactionState<T>(getStateAsLiveState());
    }

    /**
     * Delegate method called by the {@link DataSetStorageAlgorithmRunner}. This implementation asks the DataSetRegistrationService to register not
     * just the data sets, but perform any creation or updates of Experiments and Samples as well.
     */
    @Override
    public AtomicEntityOperationResult registerDataSetsInApplicationServer(TechId registrationId,
            List<DataSetRegistrationInformation<T>> dataSetRegistrations) throws Throwable
    {
        AtomicEntityOperationDetails<T> registrationDetails =
                getStateAsLiveState().createEntityOperationDetails(registrationId,
                        dataSetRegistrations);
        IEntityOperationService<T> entityRegistrationService =
                registrationService.getEntityRegistrationService();

        verifyOriginalFileIsStillAvailable();

        return entityRegistrationService.performOperationsInApplicationServer(registrationDetails);
    }

    /**
     * If we use prestaging, then we check that the original file has not been deleted.
     */
    private void verifyOriginalFileIsStillAvailable()
    {
        if (false == registrationService.shouldUsePrestaging())
        {
            return;
        }

        File realIncomingFile = getIncomingDataSetFile().getRealIncomingFile();
        if (realIncomingFile.exists())
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Incoming file [");
        sb.append(realIncomingFile.getAbsolutePath());
        sb.append("] ");
        sb.append(" was deleted before registration.");

        throw new IncomingFileDeletedBeforeRegistrationException(sb.toString());
    }

    @Override
    public EntityOperationsState didEntityOperationsSucceeded(TechId registrationId)
    {
        return openBisService.didEntityOperationsSucceed(registrationId);
    }

    public boolean isCommittedOrRolledback()
    {
        return isCommitted() || isRolledback();
    }

    public boolean isCommitted()
    {
        return state.isCommitted();
    }

    public boolean isRolledback()
    {
        return state.isRolledback();
    }

    public boolean isRecoveryPending()
    {
        return state.isRecoveryPending();
    }

    /**
     * Write something to the operation log.
     */
    public void logInfo(Object message)
    {
        operationLog.info(message);
    }

    /**
     * Return the state as live state. Throw an EnvironmentFailureException if this is not possible.
     */
    private LiveTransactionState<T> getStateAsLiveState()
    {
        try
        {
            LiveTransactionState<T> liveState = (LiveTransactionState<T>) state;
            return liveState;
        } catch (ClassCastException ex)
        {
            String message;
            if (state instanceof CommitedTransactionState)
            {
                message = "The transaction has already been committed";
            } else
            {
                message = "The transaction has already been rolledback";
            }
            throw new EnvironmentFailureException(message, ex);
        }
    }

    @Override
    public ISearchService getSearchService()
    {
        if (getUserId() == null)
            return getSearchServiceUnfiltered();
        else
            return new SearchService(
                    openBisService
                            .getBasicFilteredOpenBISService(getStateAsLiveState().getUserId()));
    }

    @Override
    public ISearchService getSearchServiceUnfiltered()
    {
        return new SearchService(openBisService);
    }

    @Override
    public ISearchService getSearchServiceFilteredForUser(String userId)
    {
        return new SearchService(openBisService.getBasicFilteredOpenBISService(userId));
    }

    @Override
    public IAuthorizationService getAuthorizationService()
    {
        return new AuthorizationService(openBisService);
    }

    @Override
    public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
            throws IllegalArgumentException
    {
        return getStateAsLiveState().getDatabaseQuery(dataSourceName);
    }

    void invokeDidEncounterSecondaryTransactionErrors(
            List<SecondaryTransactionFailure> encounteredErrors)
    {
        try
        {
            registrationService.didEncounterSecondaryTransactionErrors(this, encounteredErrors);
        } catch (Throwable t)
        {
            DataSetRegistrationTransaction.operationLog.warn(
                    "Failed to invoke secondary transaction error hook:" + t.getMessage(), t);
        }
    }

    public IDataSetStorageRecoveryManager getStorageRecoveryManager()
    {
        return registrationService.getRegistratorContext().getGlobalState()
                .getStorageRecoveryManager();
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return registrationService.getIncomingDataSetFile();
    }

    @Override
    public File getIncoming()
    {
        return getIncomingDataSetFile().getLogicalIncomingFile();
    }

    @Override
    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return registrationService.getRegistratorContext().getGlobalState();
    }

    @Override
    public Map<String, String> getServerInformation()
    {
        return getGlobalState().getOpenBisService().getServerInformation();
    }

    @Override
    public void assignRoleToSpace(RoleCode role, ISpaceImmutable space, List<String> userIds, List<String> groupCodes)
    {
        getStateAsLiveState().assignRoleToSpace(role, space, userIds, groupCodes);
    }

    @Override
    public void revokeRoleFromSpace(RoleCode role, ISpaceImmutable space, List<String> userIds, List<String> groupCodes)
    {
        getStateAsLiveState().revokeRoleFromSpace(role, space, userIds, groupCodes);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(IProjectImmutable project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null");
        }

        List<Attachment> attachments = openBisService.listAttachments(AttachmentHolderKind.PROJECT, ((ProjectImmutable) project).getId());
        return convert(attachments);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(IExperimentImmutable experiment)
    {
        if (experiment == null)
        {
            throw new IllegalArgumentException("Experiment cannot be null");
        }

        List<Attachment> attachments = openBisService.listAttachments(AttachmentHolderKind.EXPERIMENT, ((ExperimentImmutable) experiment).getId());
        return convert(attachments);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(ISampleImmutable sample)
    {
        if (sample == null)
        {
            throw new IllegalArgumentException("Sample cannot be null");
        }

        List<Attachment> attachments = openBisService.listAttachments(AttachmentHolderKind.SAMPLE, ((SampleImmutable) sample).getId());
        return convert(attachments);
    }

    @Override
    public InputStream getAttachmentContent(IProjectImmutable project, String fileName, Integer versionOrNull)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null");
        }

        InputStream stream =
                openBisService.getAttachmentContent(AttachmentHolderKind.PROJECT, ((ProjectImmutable) project).getId(), fileName, versionOrNull);
        if (stream != null)
        {
            resources.add(new ReleasableStream(stream));
        }
        return stream;
    }

    @Override
    public InputStream getAttachmentContent(IExperimentImmutable experiment, String fileName, Integer versionOrNull)
    {
        if (experiment == null)
        {
            throw new IllegalArgumentException("Experiment cannot be null");
        }

        InputStream stream =
                openBisService.getAttachmentContent(AttachmentHolderKind.EXPERIMENT, ((ExperimentImmutable) experiment).getId(), fileName,
                        versionOrNull);
        if (stream != null)
        {
            resources.add(new ReleasableStream(stream));
        }
        return stream;
    }

    @Override
    public InputStream getAttachmentContent(ISampleImmutable sample, String fileName, Integer versionOrNull)
    {
        if (sample == null)
        {
            throw new IllegalArgumentException("Sample cannot be null");
        }

        InputStream stream =
                openBisService.getAttachmentContent(AttachmentHolderKind.SAMPLE, ((SampleImmutable) sample).getId(), fileName, versionOrNull);
        if (stream != null)
        {
            resources.add(new ReleasableStream(stream));
        }
        return stream;
    }

    private List<IAttachmentImmutable> convert(List<Attachment> attachments)
    {
        List<IAttachmentImmutable> iattachments = new ArrayList<IAttachmentImmutable>();

        if (attachments != null)
        {
            for (Attachment attachment : attachments)
            {
                if (attachment != null)
                {
                    IAttachmentImmutable iattachment = new AttachmentImmutable(attachment);
                    iattachments.add(iattachment);
                }
            }
        }

        if (iattachments.isEmpty())
        {
            return null;
        } else
        {
            return iattachments;
        }
    }

    private static class ReleasableStream implements IReleasable
    {

        private InputStream stream;

        public ReleasableStream(InputStream stream)
        {
            this.stream = stream;
        }

        @Override
        public void release()
        {
            try
            {
                if (stream != null)
                {
                    stream.close();
                }
            } catch (IOException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
        }

    }

    public void close()
    {
        resources.release();
    }

}
