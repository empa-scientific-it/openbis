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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.lemnik.eodsql.DynamicTransactionQuery;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMetaproject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IVocabulary;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IVocabularyTerm;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.CommitedTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.LiveTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState.RolledbackTransactionState;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The implementation of a transaction. This class is designed to be used in one thread.
 * <p>
 * A transaction tracks commands that are invoked on it so they can be reverted (rolledback) if necessary.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransaction<T extends DataSetInformation> implements
        IDataSetRegistrationTransaction, DataSetStorageAlgorithmRunner.IRollbackDelegate<T>,
        DataSetStorageAlgorithmRunner.IDataSetInApplicationServerRegistrator<T>,
        DataSetRegistrationContext.IHolder
{
    public static final String SUCCESS_MESSAGE = "Successfully committed transaction";

    private static final String ROLLBACK_QUEUE_FILE_NAME_SUFFIX = "rollBackQueue";

    private final static String ROLLBACK_STACK_FILE_NAME_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationTransaction.class);

    public static synchronized RollbackStack[] findRollbackStacks(File rollBackStackParentFolder)
    {
        File[] rollbackQueueFiles = rollBackStackParentFolder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(ROLLBACK_QUEUE_FILE_NAME_SUFFIX);
                }
            });

        RollbackStack[] rollbackStacks = new RollbackStack[rollbackQueueFiles.length];

        for (int i = 0; i < rollbackQueueFiles.length; i++)
        {
            rollbackStacks[i] = new RollbackStack(rollbackQueueFiles[i], operationLog);
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
                + ROLLBACK_QUEUE_FILE_NAME_SUFFIX), operationLog);
    }

    private AbstractTransactionState<T> state;

    private DataSetRegistrationContext registrationContext;

    // The registration service that owns this transaction
    private final DataSetRegistrationService<T> registrationService;

    // The interface to openBIS
    private final IEncapsulatedOpenBISService openBisService;

    public DataSetRegistrationTransaction(File rollBackStackParentFolder, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        this(createNewRollbackStack(rollBackStackParentFolder), workingDirectory, stagingDirectory,
                registrationService, registrationDetailsFactory);
    }

    DataSetRegistrationTransaction(RollbackStack rollbackStack, File workingDirectory,
            File stagingDirectory, DataSetRegistrationService<T> registrationService,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
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
                        this.registrationService.getRegistratorContext().getGlobalState(), null);
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
        return getStateAsLiveState().getDataSet(dataSetCode);
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
        SampleIdentifier sampleIdentifier =
                new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleOrNull =
                openBisService.tryGetSampleWithExperiment(sampleIdentifier);
        return (null == sampleOrNull) ? null : new SampleImmutable(sampleOrNull);
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
    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experimentOrNull =
                openBisService.tryGetExperiment(experimentIdentifier);
        return (null == experimentOrNull) ? null : new ExperimentImmutable(experimentOrNull);
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
    public IProjectImmutable getProject(String projectIdentifierString)
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdentifierString).createIdentifier();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project projectOrNull =
                openBisService.tryGetProject(projectIdentifier);
        return (null == projectOrNull) ? null : new ProjectImmutable(projectOrNull);
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
        SpaceIdentifier spaceIdentifier = new SpaceIdentifier(spaceCode);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space spaceOrNull =
                openBisService.tryGetSpace(spaceIdentifier);
        return (null == spaceOrNull) ? null : new SpaceImmutable(spaceOrNull);
    }

    @Override
    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        MaterialIdentifier materialIdentifier = new MaterialIdentifier(materialCode, materialType);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material materialOrNull =
                openBisService.tryGetMaterial(materialIdentifier);
        return (null == materialOrNull) ? null : new MaterialImmutable(materialOrNull);
    }

    @Override
    public IMaterialImmutable getMaterial(String identifier)
    {
        MaterialIdentifier materialId = MaterialIdentifier.tryParseIdentifier(identifier);
        if (materialId == null)
        {
            throw new IllegalArgumentException("Incorrect material identifier format " + identifier
                    + ". Expected code (type)");
        }
        return getMaterial(materialId.getCode(), materialId.getTypeCode());
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
        if (state.isRolledback())
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
        if (state.isRolledback())
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

        rollback();
        registrationService.didRollbackTransaction(this, algorithm, ex, errorType);
    }

    /**
     * Delegate method called by the {@link DataSetStorageAlgorithmRunner}. This implementation asks the DataSetRegistrationService to register not
     * just the data sets, but perform any creation or updates of Experiments and Samples as well.
     */
    @Override
    public void registerDataSetsInApplicationServer(TechId registrationId,
            List<DataSetRegistrationInformation<T>> dataSetRegistrations) throws Throwable
    {
        AtomicEntityOperationDetails<T> registrationDetails =
                getStateAsLiveState().createEntityOperationDetails(registrationId,
                        dataSetRegistrations);
        IEntityOperationService<T> entityRegistrationService =
                registrationService.getEntityRegistrationService();

        verifyOriginalFileIsStillAvailable();

        entityRegistrationService.performOperationsInApplicationServer(registrationDetails);
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

    @Override
    public OmniscientTopLevelDataSetRegistratorState getRegistratorContext()
    {
        return registrationService.getRegistratorContext();
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
        return getRegistratorContext().getGlobalState();
    }
}
