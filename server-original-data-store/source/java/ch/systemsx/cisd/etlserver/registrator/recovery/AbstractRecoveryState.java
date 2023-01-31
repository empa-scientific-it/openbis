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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import ch.systemsx.cisd.etlserver.DssRegistrationLogDirectoryHelper;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithm;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public abstract class AbstractRecoveryState<T extends DataSetInformation> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private File dssRegistrationLogFile;

    private File rollbackStackCommandsFile;

    private DataSetFile incomingDataSetFile;

    private DataSetRegistrationPersistentMap persistentMap;

    public AbstractRecoveryState()
    {
    }

    public AbstractRecoveryState(DssRegistrationLogger logger, IRollbackStack rollbackStack,
            DataSetFile incomingDataSetFile, DataSetRegistrationPersistentMap persistentMap)
    {
        dssRegistrationLogFile = logger.getFile();
        this.rollbackStackCommandsFile = ((RollbackStack) rollbackStack).getCommandsFile();

        this.incomingDataSetFile = incomingDataSetFile;

        this.persistentMap = persistentMap;
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public DssRegistrationLogger getRegistrationLogger(
            OmniscientTopLevelDataSetRegistratorState state)
    {
        DssRegistrationLogDirectoryHelper helper =
                new DssRegistrationLogDirectoryHelper(state.getGlobalState()
                        .getDssRegistrationLogDir());

        DssRegistrationLogger logger =
                new DssRegistrationLogger(dssRegistrationLogFile, helper, state.getFileOperations());
        return logger;
    }

    public abstract ArrayList<DataSetStorageAlgorithm<T>> getDataSetStorageAlgorithms(
            OmniscientTopLevelDataSetRegistratorState state);

    public RollbackStack getRollbackStack()
    {
        return new RollbackStack(rollbackStackCommandsFile);
    }

    public DataSetRegistrationPersistentMap getPersistentMap()
    {
        return persistentMap;
    }
}
