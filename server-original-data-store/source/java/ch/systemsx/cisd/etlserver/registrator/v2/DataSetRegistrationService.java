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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DssRegistrationLogDirectoryHelper;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.IdentifiedDataStrategy;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.etlserver.registrator.IEntityOperationService;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A service that registers many files as individual data sets in one transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationService<T extends DataSetInformation> implements
        DataSetStorageAlgorithmRunner.IPrePostRegistrationHook<T>
{
    private final IOmniscientEntityRegistrator<T> registrator;

    private final OmniscientTopLevelDataSetRegistratorState registratorContext;

    /**
     * Don't call explicitly, use executeCleanAction method instead. Should be called only once.
     */
    private final IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction;

    private boolean cleanActionExecuted = false;

    private final IDataSetRegistrationDetailsFactory<T> dataSetRegistrationDetailsFactory;

    private final File stagingDirectory;

    private final File precommitDirectory;

    private final DataSetFile incomingDataSetFile;

    public final DssRegistrationLogger dssRegistrationLog;

    private final DssRegistrationLogDirectoryHelper dssRegistrationLogHelper;

    private final ITopLevelDataSetRegistratorDelegate delegate;

    private final IDataStrategyStore dataStrategyStore;

    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationService.class);

    protected String userSessionToken;

    /**
     * Keep track of errors we encounter while processing. Clients may want this information.
     */
    public final ArrayList<Throwable> encounteredErrors = new ArrayList<Throwable>();

    /**
     * The single transaction created on this service.
     */
    protected DataSetRegistrationTransaction<T> transaction;

    /**
     * Create a new DataSetRegistrationService.
     * 
     * @param registrator The top level data set registrator
     * @param globalCleanAfterwardsAction An action to execute when the service has finished
     */
    public DataSetRegistrationService(IOmniscientEntityRegistrator<T> registrator,
            DataSetFile incomingDataSetFile,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        this.registrator = registrator;
        this.registratorContext = registrator.getRegistratorState();
        this.incomingDataSetFile = incomingDataSetFile;
        this.globalCleanAfterwardsAction = globalCleanAfterwardsAction;
        this.dataSetRegistrationDetailsFactory = registrationDetailsFactory;
        this.delegate = delegate;
        this.dataStrategyStore =
                registratorContext.getDataStrategyStore().getCachedDataStrategyStore();

        ThreadParameters threadParameters =
                registratorContext.getGlobalState().getThreadParameters();
        this.dssRegistrationLogHelper =
                new DssRegistrationLogDirectoryHelper(registratorContext.getGlobalState()
                        .getDssRegistrationLogDir());
        this.dssRegistrationLog =
                dssRegistrationLogHelper.createNewLogFile(incomingDataSetFile
                        .getLogicalIncomingFile().getName(), threadParameters.getThreadName(),
                        this.registratorContext.getFileOperations());
        this.stagingDirectory = registratorContext.getGlobalState().getStagingDir();
        this.precommitDirectory = registratorContext.getGlobalState().getPreCommitDir();
    }

    public void setUserSessionToken(String userSessionToken)
    {
        this.userSessionToken = userSessionToken;
    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorContext()
    {
        return registratorContext;
    }

    public DataSetRegistrationTransaction<T> transaction()
    {
        return transaction(incomingDataSetFile.getLogicalIncomingFile(),
                getDataSetRegistrationDetailsFactory());
    }

    /**
     * Create a new transaction that atomically performs file operations and registers entities.
     */
    public DataSetRegistrationTransaction<T> transaction(File dataSetFile,
            IDataSetRegistrationDetailsFactory<T> detailsFactory)
    {
        File workingDirectory = dataSetFile.getParentFile();

        if (transaction == null)
        {
            transaction =
                    createTransaction(registrator.getRollBackStackParentFolder(), workingDirectory,
                            stagingDirectory, detailsFactory);
        } else
        {
            throw new IllegalStateException(
                    "Failed to create transaction. Transaction has already been created before.");
        }

        return transaction;
    }

    protected DataSetRegistrationTransaction<T> createTransaction(File rollBackStackParentFolder,
            File workingDirectory, File stagingDir,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
    {
        return new DataSetRegistrationTransaction<T>(rollBackStackParentFolder, workingDirectory,
                stagingDir, this, registrationDetailsFactory, userSessionToken);

    }

    /**
     * Commit any scheduled changes.
     */
    public void commit()
    {
        // If a transaction is hanging around, commit it
        commitExtantTransactions();

        boolean transactionWasRolledback = transaction != null && transaction.isRolledback();

        logDssRegistrationResult();

        // Execute the clean afterwards action as successful only if no errors occurred and we
        // registered data sets
        executeGlobalCleanAfterwardsAction(false == (didErrorsArise() || transactionWasRolledback));
    }

    /**
     * Write to the dssRegistrationLog either success or the failure with error details.
     */
    protected void logDssRegistrationResult()
    {
        boolean stillExists = incomingDataSetFile.getRealIncomingFile().exists();
        if (false == stillExists)
        {
            dssRegistrationLog
                    .info(operationLog, "Incoming file ["
                            + incomingDataSetFile.getRealIncomingFile()
                            + "] was deleted outside of openBIS after processing started. The data had already been registered in the database.");

        }
        dssRegistrationLog.logDssRegistrationResult(encounteredErrors);
    }

    /**
     * Abort any scheduled changes.
     */
    public void abort(Throwable t)
    {
        encounteredErrors.add(t);
        rollbackExtantTransactions();
        executeGlobalCleanAfterwardsAction(false);
    }

    /**
     * Call this method at the end of registration to make sure the clean action was executed even if neither commit nor abort happened. This method
     * calls <code>globalCleanAfterwardsAction</code> action with <code>succeeded<code> parameter false.
     */
    public void cleanAfterRegistrationIfNecessary()
    {
        executeGlobalCleanAfterwardsAction(false);
    }

    protected void executeGlobalCleanAfterwardsAction(boolean success)
    {
        if (false == cleanActionExecuted)
        {
            if (transaction != null)
            {
                transaction.close();
            }
            globalCleanAfterwardsAction.execute(success);
            cleanActionExecuted = true;
        }
    }

    public File moveIncomingToError(String dataSetTypeCodeOrNull)
    {
        DataSetStorageRollbacker rollbacker =
                new DataSetStorageRollbacker(registratorContext, operationLog,
                        UnstoreDataAction.MOVE_TO_ERROR, incomingDataSetFile,
                        dataSetTypeCodeOrNull, null);
        return rollbacker.doRollback(dssRegistrationLog);
    }

    public void didRollbackTransaction(DataSetRegistrationTransaction<T> t,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex, ErrorType errorType)
    {
        encounteredErrors.add(ex);
        // Don't do the undo store action when this exception happens
        boolean stopped = ex instanceof InterruptedExceptionUnchecked;
        if (false == stopped)
        {
            UnstoreDataAction action =
                    registratorContext.getOnErrorActionDecision().computeUndoAction(errorType, ex);
            DataSetStorageRollbacker rollbacker =
                    new DataSetStorageRollbacker(registratorContext, operationLog, action,
                            incomingDataSetFile, null, ex, errorType);
            operationLog.info(rollbacker.getErrorMessageForLog());
            rollbacker.doRollback(dssRegistrationLog);
        }
        registrator.didRollbackTransaction(this, transaction, algorithm, ex);
    }

    public void executePostCommit(DataSetRegistrationTransaction<T> t)
    {
        registrator.didCommitTransaction(this, t);
    }

    @Override
    public void executePreRegistration(DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        registrator.didPreRegistration(this, registrationContextHolder);
    }

    @Override
    public void executePostRegistration(DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        registrator.didPostRegistration(this, registrationContextHolder);
    }

    public void didEncounterSecondaryTransactionErrors(DataSetRegistrationTransaction<T> t,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
        registrator.didEncounterSecondaryTransactionErrors(this, t, secondaryErrors);
    }

    /**
     * Create a storage algorithm for storing an individual data set. This is internally used by transactions. Other clients may find it useful as
     * well.
     */
    public DataSetStorageAlgorithm<T> createStorageAlgorithm(File dataSetFile,
            DataSetRegistrationDetails<? extends T> dataSetDetails)
    {
        IDataStoreStrategy strategy =
                dataStrategyStore.getDataStoreStrategy(dataSetDetails.getDataSetInformation(),
                        dataSetFile);
        return createStorageAlgorithmWithStrategy(dataSetFile, dataSetDetails, strategy);
    }

    /**
     * Create a storage algorithm for storing an individual data set, bypassing the detection of whether the data set's owner is in the db. This is
     * used if the owner will be registered in the same transaction. This is internally used by transactions. Other clients may find it useful as
     * well.
     */
    public DataSetStorageAlgorithm<T> createStorageAlgorithmWithIdentifiedStrategy(
            File dataSetFile, DataSetRegistrationDetails<? extends T> dataSetDetails)
    {
        IDataStoreStrategy strategy = new IdentifiedDataStrategy();
        return createStorageAlgorithmWithStrategy(dataSetFile, dataSetDetails, strategy);
    }

    /**
     * Helper method to create a storage algorithm for storing an individual data set.
     */
    protected DataSetStorageAlgorithm<T> createStorageAlgorithmWithStrategy(File dataSetFile,
            DataSetRegistrationDetails<? extends T> dataSetDetails, IDataStoreStrategy strategy)
    {
        // If the file is null and the registration details say it is a container, return a
        // different algorithm.
        TopLevelDataSetRegistratorGlobalState globalContext = registratorContext.getGlobalState();
        T dataSetInformation = dataSetDetails.getDataSetInformation();
        dataSetInformation.setShareId(globalContext.getShareId());

        DataSetStorageAlgorithm<T> algorithm;
        if (dataSetInformation.isNoFileDataSet())
        {
            // Return a different storage algorithm for non-regular data sets
            if (null != dataSetFile)
            {
                throw new IllegalArgumentException(
                        "A data set can contain either files or other data sets or link to an external system dataset. The data set specification is invalid: "
                                + dataSetInformation);
            }

            if (dataSetInformation.isContainerDataSet())
            {
                algorithm =
                        new ContainerDataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails,
                                strategy, registratorContext.getStorageProcessor(),
                                globalContext.getDataSetValidator(), globalContext.getDssCode(),
                                registratorContext.getFileOperations(),
                                globalContext.getMailClient(), stagingDirectory, precommitDirectory);
            } else if (dataSetInformation.isLinkDataSet())
            {
                algorithm =
                        new LinkDataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails, strategy,
                                registratorContext.getStorageProcessor(),
                                globalContext.getDataSetValidator(), globalContext.getDssCode(),
                                registratorContext.getFileOperations(),
                                globalContext.getMailClient(), stagingDirectory, precommitDirectory);
            } else
            {
                throw new IllegalStateException(
                        "No-file dataset has to be either a container dataset or a link dataset");
            }
        } else
        {

            algorithm =
                    new DataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails, strategy,
                            registratorContext.getStorageProcessor(),
                            globalContext.getDataSetValidator(), globalContext.getDssCode(),
                            registratorContext.getFileOperations(), globalContext.getMailClient(),
                            stagingDirectory, precommitDirectory);
        }
        return algorithm;
    }

    public IEntityOperationService<T> getEntityRegistrationService()
    {
        return new DefaultEntityOperationService<T>(registrator, delegate);
    }

    /**
     * Return true if errors happend while processing the service.
     */
    public boolean didErrorsArise()
    {
        return encounteredErrors.isEmpty() == false;
    }

    /**
     * Return the list of errors that were encountered. If didErrorsArise is false, this list will be empty, otherwise there will be at least one
     * element.
     */
    public List<Throwable> getEncounteredErrors()
    {
        return encounteredErrors;
    }

    public void registerNonFatalError(Throwable t)
    {
        encounteredErrors.add(t);
    }

    public IDataSetRegistrationDetailsFactory<T> getDataSetRegistrationDetailsFactory()
    {
        return dataSetRegistrationDetailsFactory;
    }

    /**
     * If a transaction is hanging around, commit it
     */
    private void commitExtantTransactions()
    {
        if (transaction != null && false == transaction.isCommittedOrRolledback())
        {
            // Commit the existing transaction
            transaction.commit();
        }
    }

    private void rollbackExtantTransactions()
    {
        if (transaction != null && false == transaction.isCommittedOrRolledback())
        {
            // Rollback the existing transaction
            transaction.rollback();
        }
    }

    protected IOmniscientEntityRegistrator<T> getRegistrator()
    {
        return registrator;
    }

    /**
     * @return the logger for this data set registration process
     */
    public DssRegistrationLogger getDssRegistrationLog()
    {
        return dssRegistrationLog;
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public boolean shouldUsePrestaging()
    {
        return delegate.getPrestagingBehavior() == DataSetRegistrationPreStagingBehavior.USE_PRESTAGING;
    }

}
