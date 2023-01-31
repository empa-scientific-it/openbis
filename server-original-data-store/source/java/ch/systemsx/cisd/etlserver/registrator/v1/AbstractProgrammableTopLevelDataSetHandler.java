/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.io.File;
import java.util.List;

import org.python.core.PyException;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IJavaDataSetRegistrationDropboxV1;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler.ProgrammableDropboxObjectFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractProgrammableTopLevelDataSetHandler<T extends DataSetInformation>
        extends AbstractOmniscientTopLevelDataSetRegistrator<T>
{

    /**
     * @param globalState
     */
    protected AbstractProgrammableTopLevelDataSetHandler(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    @Override
    public abstract boolean shouldNotAddToFaultyPathsOrNull(File file);

    @Override
    abstract protected void handleDataSet(DataSetFile dataSetFile,
            DataSetRegistrationService<T> service) throws Throwable;

    protected void waitUntilApplicationIsReady(DataSetFile incomingDataSetFile)
    {
        while (DssRegistrationHealthMonitor.getInstance().checkHealthState(
                incomingDataSetFile.getRealIncomingFile().getParentFile()).isUnavailable())
        {
            waitTheRetryPeriod(10);
            // do nothing. just repeat until the application is ready
        }
    }

    protected void waitTheRetryPeriod(int retryPeriod)
    {
        ConcurrencyUtilities.sleep(retryPeriod * 1000); // in seconds
    }

    @Override
    public void didRollbackTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
    {
        try
        {
            getV2DropboxProgram(service).rollbackPreRegistration(
                    transaction.getRegistrationContext(), ex);
        } catch (NotImplementedException e)
        {
            IJavaDataSetRegistrationDropboxV1<T> v1Dropbox = getV1DropboxProgram();

            try
            {
                v1Dropbox.rollbackTransaction(service, transaction, algorithmRunner, ex);
            } catch (NotImplementedException exc)
            {
                try
                {
                    // No Rollback transaction function was called, see if the rollback service
                    // function
                    // was
                    // defined, and call it.
                    v1Dropbox.rollbackService(service, ex);
                } catch (NotImplementedException exception)
                {
                    // silently ignore if function is not implemented
                }
            }
        }

        super.didRollbackTransaction(service, transaction, algorithmRunner, ex);
    }

    @Override
    public void didCommitTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction)
    {
        super.didCommitTransaction(service, transaction);
        try
        {
            getV2DropboxProgram(service).postStorage(transaction.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            try
            {
                getV1DropboxProgram().commitTransaction(service, transaction);
            } catch (NotImplementedException ex)
            {
                // silently ignore if function is not implemented
            }
        }
    }

    @Override
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        super.didPreRegistration(service, registrationContextHolder);
        try
        {
            getV2DropboxProgram(service).preMetadataRegistration(
                    registrationContextHolder.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            // ignore
        }
    }

    @Override
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder)
    {
        super.didPostRegistration(service, registrationContextHolder);
        try
        {
            getV2DropboxProgram(service).postMetadataRegistration(
                    registrationContextHolder.getRegistrationContext());
        } catch (NotImplementedException e)
        {
            // ignore
        }
    }

    @Override
    public void didEncounterSecondaryTransactionErrors(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
        super.didEncounterSecondaryTransactionErrors(service, transaction, secondaryErrors);

        try
        {
            getV1DropboxProgram().didEncounterSecondaryTransactionErrors(service, transaction,
                    secondaryErrors);
        } catch (NotImplementedException e)
        {
            // silently ignore if function is not implemented
        }
    }

    abstract protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgram(
            DataSetRegistrationService<T> service);

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    @SuppressWarnings("unchecked")
    protected IDataSetRegistrationDetailsFactory<T> createObjectFactory(
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return (IDataSetRegistrationDetailsFactory<T>) new ProgrammableDropboxObjectFactory<DataSetInformation>(
                getRegistratorState(), userProvidedDataSetInformationOrNull)
            {
                @Override
                protected DataSetInformation createDataSetInformation()
                {
                    return new DataSetInformation();
                }
            };
    }

    /**
     * Create a registration service.
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationService(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);
    }

    /**
     * Create a registration service.
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationService(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new DataSetRegistrationService<T>(this, incomingDataSetFile,
                this.createObjectFactory(userProvidedDataSetInformationOrNull),
                cleanAfterwardsAction, delegate);
    }

    protected abstract IJavaDataSetRegistrationDropboxV1<T> getV1DropboxProgram();

    interface IRecoveryCleanupDelegate
    {
        void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount);
    }

    @Override
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof PyException)
        {
            Object value = ((PyException) throwable).value;
            return new RuntimeException((value == null ? "" : value + "\n") + throwable);
        }

        return super.asSerializableException(throwable);
    }
}
