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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;

/**
 * Info provider for plugin tasks.
 *
 * @author Bernd Rinn
 */
public interface IPluginTaskInfoProvider
{

    /**
     * Returns the root directory of the data store.
     */
    public File getStoreRoot();

    /**
     * Returns the root directory of session workspaces.
     */
    public File getSessionWorkspaceRootDir();

    public PluginTaskProvider<IReportingPluginTask> getReportingPluginsProvider();

    public PluginTaskProvider<IProcessingPluginTask> getProcessingPluginsProvider();

    public PluginTaskProvider<ISearchDomainService> getSearchDomainServiceProvider();

    public ArchiverPluginFactory getArchiverPluginFactory();

    public void logConfigurations();

    public DatastoreServiceDescriptions getPluginTaskDescriptions();

}