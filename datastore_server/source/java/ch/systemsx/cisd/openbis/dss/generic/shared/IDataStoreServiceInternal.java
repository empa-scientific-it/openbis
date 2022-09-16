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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.resource.IInitializable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * {@link IDataStoreService} for internal(invisible for openBIS) usage.
 * 
 * @author Kaloyan Enimanev
 */
public interface IDataStoreServiceInternal extends IInitializable, IDataStoreService
{
    /**
     * Return an {@link IDataSetDeleter} instance.
     */
    IDataSetDeleter getDataSetDeleter();

    /**
     * Return an {@link IArchiverPlugin} or null if none is configured.
     */
    IArchiverPlugin getArchiverPlugin();

    /**
     * Return the archiver {@link Properties} or null if the archiver is not configured.
     */
    Properties getArchiverProperties();

    /**
     * Returns the data set directory provider.
     */
    IDataSetDirectoryProvider getDataSetDirectoryProvider();

    /**
     * Returns the mail client.
     */
    IMailClient createEMailClient();

    /**
     * Returns the session workspace provider.
     */
    ISessionWorkspaceProvider getSessionWorkspaceProvider(String userSessionToken);

    /**
     * An internal version of {@link IDataStoreService#createReportFromDatasets(String, String, String, List, String, String)} .
     */
    public TableModel internalCreateReportFromDatasets(String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull);

    /**
     * An internal version of {@link IDataStoreService#createReportFromAggregationService(String, String, String, Map, String, String)}
     */
    public TableModel internalCreateReportFromAggregationService(String userSessionToken,
            String serviceKey, Map<String, Object> parameters, String userId, String userEmailOrNull);

    public void scheduleTask(String taskKey, IProcessingPluginTask task, Map<String, String> parameterBindings,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull, String userSessionToken);

    /**
     * Returns the put data set service.
     */
    IPutDataSetService getPutDataSetService();

    /**
     * Adds an action for the specified sessionToken which will be invoked when the session is expired.
     */
    void addCleanupAction(String userSessionToken, IDelegatedAction action);

}
