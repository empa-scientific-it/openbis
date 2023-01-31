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

package ch.systemsx.cisd.etlserver.registrator.recovery;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class DataSetStorageStorageRecoveryState<T extends DataSetInformation> extends
        AbstractRecoveryState<T>
{
    private static final long serialVersionUID = 1L;

    private final List<DataSetStorageStoredRecoveryAlgorithm<T>> dataSetRecoveryStorageAlgorithms;

    public DataSetStorageStorageRecoveryState(
            List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            DssRegistrationLogger logger, IRollbackStack rollbackStack,
            DataSetFile incomingDataSetFile, DataSetRegistrationPersistentMap persistentMap)
    {
        super(logger, rollbackStack, incomingDataSetFile, persistentMap);
        this.dataSetRecoveryStorageAlgorithms =
                new ArrayList<DataSetStorageStoredRecoveryAlgorithm<T>>();
        for (DataSetStorageAlgorithm<T> algorithm : dataSetStorageAlgorithms)
        {
            this.dataSetRecoveryStorageAlgorithms.add(algorithm.getStoredRecoveryAlgorithm());
        }
    }

    @Override
    public ArrayList<DataSetStorageAlgorithm<T>> getDataSetStorageAlgorithms(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        ArrayList<DataSetStorageAlgorithm<T>> algorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>();

        for (DataSetStorageStoredRecoveryAlgorithm<T> recoveryAlgorithm : this.dataSetRecoveryStorageAlgorithms)
        {
            algorithms.add(recoveryAlgorithm.recoverDataSetStorageAlgorithm(state));
        }
        return algorithms;
    }

}
