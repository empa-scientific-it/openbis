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

package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.util.concurrent.locks.Lock;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.AbstractTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.ETLServerPluginFactory;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Not a real top-level data set registrator. It is just a placeholder to make it possible to extract the IETLServerPlugin.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class PutDataSetServerPluginHolder extends AbstractTopLevelDataSetRegistrator
{
    private final IETLServerPlugin plugin;

    /**
     * The designated constructor.
     * 
     * @param globalState
     */
    public PutDataSetServerPluginHolder(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        plugin = ETLServerPluginFactory.getPluginForThread(globalState.getThreadParameters());
    }

    public IETLServerPlugin getPlugin()
    {
        return plugin;
    }

    @Override
    public Lock getRegistrationLock()
    {
        throw new NotImplementedException();
    }

    @Override
    public void handle(File file, String userSessionToken,
            DataSetInformation callerDataSetInformation, ITopLevelDataSetRegistratorDelegate delegate)
    {
        throw new NotImplementedException();
    }

    @Override
    public void handle(File path)
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean isStopped()
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRemote()
    {
        throw new NotImplementedException();
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean shouldNotAddToFaultyPathsOrNull(File storeItem)
    {
        throw new NotImplementedException();
    }

}
