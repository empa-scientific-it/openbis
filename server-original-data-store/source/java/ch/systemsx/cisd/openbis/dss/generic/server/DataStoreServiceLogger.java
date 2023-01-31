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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.resource.IInitializable;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
class DataStoreServiceLogger implements IDataStoreService, IInitializable
{
    private static final String RESULT_SUCCESS = "";

    private static final String RESULT_FAILURE = " ...FAILED";

    private final Logger operationLog;

    private final IInvocationLoggerContext loggerContext;

    DataStoreServiceLogger(Logger operationLog, IInvocationLoggerContext context)
    {
        this.operationLog = operationLog;
        this.loggerContext = context;
    }

    private final void log(final String commandName, final String parameterDisplayFormat,
            final Object... parameters)
    {
        for (int i = 0; i < parameters.length; i++)
        {
            final Object parameter = parameters[i];
            if (parameter == null)
            {
                parameters[i] = "<UNDEFINED>";
            } else
            {
                parameters[i] = "'" + parameter + "'";
            }
        }
        final String message = String.format(parameterDisplayFormat, parameters);
        final String invocationStatusMessage =
                loggerContext.invocationWasSuccessful() ? RESULT_SUCCESS : RESULT_FAILURE;
        // We put on purpose 2 spaces between the command and the message derived from the
        // parameters.
        operationLog.info(String.format("%s  %s%s (%s ms)", commandName, message,
                invocationStatusMessage, loggerContext.getElapsedTime()));
    }

    @Override
    public void initialize()
    {
        log("initialize", "");
    }

    @Override
    public int getVersion(String sessionToken)
    {
        log("getVersion", "SESSION(%s)", sessionToken);
        return 0;
    }

    public void send(ServiceMessage message)
    {
        log("send", "", message);
    }

    @Override
    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSetLocations, boolean ignoreNonExistingLocation)
            throws InvalidAuthenticationException
    {
        log("getKnownDataSets", "DATA_SETS(%s) IGNORE_NON_EXISTING_LOCATION(%s)", dataSetLocations,
                ignoreNonExistingLocation);
        return null;
    }

    public void deleteDataSets(String sessionToken, List<? extends IDatasetLocation> dataSets)
            throws InvalidAuthenticationException
    {
        log("deleteDataSets", "DATA_SETS(%s)", dataSets);
    }

    @Override
    public void uploadDataSetsToCIFEX(String sessionToken, List<AbstractExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        StringBuilder builder = new StringBuilder();
        for (AbstractExternalData externalDataPE : dataSets)
        {
            builder.append(' ').append(externalDataPE.getCode());
        }
        log("uploadDataSetsToCIFEX", "USER(%s) DATASETS(%s)", context.getUserID(), builder
                .toString().trim());
    }

    @Override
    public TableModel createReportFromDatasets(String sessionToken, String userSessionToken,
            String serviceKey, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull)
    {
        log("createReportFromDatasets",
                "USER_SESSION(%s) TASK_ID(%s) NO_OF_DATASETS(%s) USER_ID (%s) EMAIL(%s)",
                userSessionToken, serviceKey, datasets.size(), userId, userEmailOrNull);
        return null;
    }

    @Override
    public void processDatasets(String sessionToken, String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userId, String userEmailOrNull)
    {
        log("processDatasets",
                "USER_SESSION(%s) TASK_ID(%s) NO_OF_DATASETS(%s) PARAMETERS(%s) USER_ID (%s)  USER_EMAIL(%s)",
                userSessionToken, serviceKey, datasets.size(), parameterBindings.keySet(), userId,
                userEmailOrNull);
    }

    @Override
    public void unarchiveDatasets(String sessionToken, String userSessionToken, List<DatasetDescription> datasets,
            String userId, String userEmailOrNull)
    {
        log("activateDatasets", "NO_OF_DATASETS(%s) USER_ID (%s) USER_EMAIL(%s)", datasets.size(),
                userId, userEmailOrNull);
    }

    @Override
    public void archiveDatasets(String sessionToken, String userSessionToken, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull, boolean removeFromDataStore, Map<String, String> options)
    {
        log("archiveDatasets",
                "NO_OF_DATASETS(%s) USER_ID (%s) USER_EMAIL(%s) REMOVE_FROM_DATA_STORE(%s)",
                datasets.size(), userId, userEmailOrNull, removeFromDataStore);
    }

    @Override
    public boolean isArchivingPossible(String sessionToken)
    {
        log("isArchivingPossible", "");
        return false;
    }

    @Override
    public List<String> getDataSetCodesForUnarchiving(String sessionToken, String userSessionToken, List<String> datasets, String userId)
    {
        log("getDataSetCodesForUnarchiving", "NO_OF_DATASETS(%s)", datasets.size());
        return null;
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String sessionToken, String serviceKey,
            DatasetDescription dataSet)
    {
        log("retrieveLinkFromDataSet", "TASK_ID(%s) DATA_SET(%s)", serviceKey, dataSet);
        return null;
    }

    @Override
    public TableModel createReportFromAggregationService(String sessionToken,
            String userSessionToken, String serviceKey, Map<String, Object> parameters,
            String userId, String userEmailOrNull)
    {
        log("createReportFromAggregationService",
                "USER_SESSION(%s) SERVICE(%s) PARAMETERS(%s) USER_ID(%s) EMAIL(%s)",
                userSessionToken, serviceKey, parameters.keySet(), userId, userEmailOrNull);
        return null;
    }

    @Override
    public String putDataSet(String sessionToken, String dropboxName,
            CustomImportFile customImportFile)
    {
        log("putDataSet", "DROPBOX_NAME(%s), CUSTOM_IMPORT_FILE(%s)", dropboxName, customImportFile);
        return null;
    }

    @Override
    public void cleanupSession(String userSessionToken)
    {
        log("cleanupSession", "USER_SESSION(%s)", userSessionToken);
    }

    @Override
    public List<SearchDomainSearchResult> searchForEntitiesWithSequences(String sessionToken,
            String preferredSearchDomainOrNull, String sequenceSnippet,
            Map<String, String> optionalParametersOrNull)
    {
        log("searchForEntitiesWithSequences", "SEARCH_DOMAIN(%s) SEQUENCE_SNIPPET(%s)",
                preferredSearchDomainOrNull, sequenceSnippet);
        return null;
    }

    @Override
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        log("listAvailableSequenceDatabases", "");
        return null;
    }
}
