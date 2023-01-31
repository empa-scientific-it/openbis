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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Interface for classes that know control registration of entities.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IOmniscientEntityRegistrator<T extends DataSetInformation>
{
    File getRollBackStackParentFolder();

    TopLevelDataSetRegistratorGlobalState getGlobalState();

    OmniscientTopLevelDataSetRegistratorState getRegistratorState();

    /**
     * Rollback a failure when trying to commit a transaction.
     */

    public void didRollbackTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex);

    /**
     * A method called after a successful commit of a transaction.
     */
    public void didCommitTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction);

    /**
     * A method called just before the registration of datasets in application server.
     */
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder);

    /**
     * A method called just after the successful registration of datasets in application server.
     */
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationContext.IHolder registrationContextHolder);

    /**
     * A method called when there is an error in one of the secondary transactions.
     */
    public void didEncounterSecondaryTransactionErrors(
            DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors);

}
