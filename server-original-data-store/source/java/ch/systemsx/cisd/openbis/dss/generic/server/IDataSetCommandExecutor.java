/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Executor of commands operating on data sets in a data store. Commands are expected to be executed asynchronously in the order they had been
 * scheduled.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetCommandExecutor extends IDataSetDeleter
{
    /**
     * Starts up executor.
     */
    void start();

    /**
     * Returns the codes of the DataSets currently in the queue.
     */
    Set<String> getDataSetCodesFromCommandQueue();

    /**
     * Schedules uploading of all data sets to CIFEX using the specified upload context.
     * 
     * @param cifexServiceFactory Factory for creating CIFEX upload service.
     * @param mailClientParameters Parameters needed for sending an e-mail to the user if uploading failed.
     * @param cifexAdminUserOrNull The name of the CIFEX admin user, or <code>null</code>, if no connection as admin should be used.
     * @param cifexAdminPasswordOrNull The password of the CIFEX admin user, or <code>null</code>, if no connection as admin should be used.
     */
    void scheduleUploadingDataSetsToCIFEX(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<AbstractExternalData> dataSets,
            DataSetUploadContext uploadContext, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull);

    /**
     * Schedules the specified processing task with specified parameter bindings for provided datasets.
     * 
     * @param parameterBindings Contains at least the parameter {@link Constants#USER_PARAMETER} with the ID of the user who initiated processing.
     * @param userId id of user who initiated the processing.
     * @param userEmailOrNull Email of user who initiated processing and will get a message after the processing is finished. It may be null if the
     *            user doesn't have email and no message will be send in such case.
     * @param sessionTokenOrNull The session token of the user that requested the processing.
     */
    void scheduleProcessDatasets(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userId, String userEmailOrNull,
            String sessionTokenOrNull, DatastoreServiceDescription serviceDescription,
            MailClientParameters mailClientParameters);
}