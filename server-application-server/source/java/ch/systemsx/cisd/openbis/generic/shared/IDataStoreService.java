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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Progress;
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
 * Service interface of Data Store Server.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataStoreService
{

    /**
     * Every time this interface and related DTO's are changed, we should increment this number.
     */
    public static final int VERSION = 10; // for S185

    /**
     * Returns the version of this service.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @return {@link #VERSION}
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    public int getVersion(String sessionToken) throws InvalidAuthenticationException;

    /**
     * Returns from the specified data sets those known by the Data Store Server.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @param ignoreNonExistingLocation specifies if non-existing locations should be ignored.
     * @return locations (as strings) of known data sets
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     * @deprecated
     */
    @Deprecated
    @Conversational(progress = Progress.MANUAL)
    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSets, boolean ignoreNonExistingLocation)
            throws InvalidAuthenticationException;

    /**
     * Uploads the specified data sets to CIFEX.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @param context Context data needed for uploading.
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    @Conversational(progress = Progress.AUTOMATIC)
    public void uploadDataSetsToCIFEX(String sessionToken, List<AbstractExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException;

    /**
     * Runs the reporting task with the specified id for provided datasets.
     * <p>
     * <i> Ensure that you call {@link #cleanupSession(String)} on closing of the user sesssion <var>userSessionToken</var> so that DSS gets the
     * chance to cleanup session files. </i>
     */
    @Conversational(progress = Progress.AUTOMATIC)
    public TableModel createReportFromDatasets(String sessionToken, String userSessionToken,
            String serviceKey, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull);

    /**
     * Schedules the processing task with the specified id for provided datasets and specified parameter bindings.
     * 
     * @param userSessionToken The session token of the user that initiated the processing.
     * @param parameterBindings Contains at least the parameter {@link Constants#USER_PARAMETER} with the ID of the user who initiated processing.
     * @param userId id of user who initiated the processing.
     * @param userEmailOrNull Email of user who initiated processing and will get a message after the processing is finished. It may be null if the
     *            user doesn't have email and no message will be send in such case.
     */
    public void processDatasets(String sessionToken, String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userId, String userEmailOrNull);

    /**
     * Schedules archiving of provided datasets.
     * 
     * @param userId id of user who initiated archiving.
     * @param userEmailOrNull Email of user who initiated archiving and will get a message after the task is finished. It may be null if the user
     *            doesn't have email and no message will be send in such case.
     * @param removeFromDataStore when set to <code>true</code> the data sets will be removed from the data store after a successful archiving
     *            operation.
     * @param options which might be used by particular archivers
     */
    public void archiveDatasets(String sessionToken, String userSessionToken,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull,
            boolean removeFromDataStore, Map<String, String> options);

    /**
     * Schedules unarchiving of provided datasets.
     * 
     * @param userId id of user who initiated unarchiving.
     * @param userEmailOrNull Email of user who initiated unarchiving and will get a message after the task is finished. It may be null if the user
     *            doesn't have email and no message will be send in such case.
     */
    public void unarchiveDatasets(String sessionToken, String userSessionToken,
            List<DatasetDescription> datasets, String userId, String userEmailOrNull);

    /**
     * Asks DSS whether archiving is currently possible or not.
     */
    public boolean isArchivingPossible(String sessionToken);

    /**
     * Asks arcvhiver to provide an extended list of data sets to unarchive.
     * 
     * @param userId id of user who initiated unarchiving.
     */
    public List<String> getDataSetCodesForUnarchiving(String sessionToken, String userSessionToken, List<String> datasets, String userId);

    /**
     * Gets the link from a service that supports the IReportingPluginTask#createLink method.
     * 
     * @param sessionToken The sessionToken
     * @param serviceKey The service that should compute the link
     * @param dataSet The data set we want the link for
     * @return A LinkModel that describes the link. The session Id needs to be filled in to retrieve the link.
     */
    public LinkModel retrieveLinkFromDataSet(String sessionToken, String serviceKey,
            DatasetDescription dataSet);

    /**
     * Gets the link from a service that supports the IReportingPluginTask#createLink method.
     * <p>
     * <i> Ensure that you call {@link #cleanupSession(String)} on closing of the user sesssion <var>userSessionToken</var> so that DSS gets the
     * chance to cleanup session files. </i>
     * 
     * @param sessionToken The sessionToken.
     * @param userSessionToken The session token of the user that initiated the processing.
     * @param serviceKey The service that produce the report.
     * @param parameters The parameters to the service.
     * @return A TableModel produced by the service.
     * @since 9
     */
    @Conversational(progress = Progress.AUTOMATIC)
    public TableModel createReportFromAggregationService(String sessionToken,
            String userSessionToken, String serviceKey, Map<String, Object> parameters,
            String userId, String userEmailOrNull);

    public String putDataSet(String sessionToken, String dropboxName,
            CustomImportFile customImportFile);

    /**
     * Cleans up the user session with given <var>userSessionToken</var>.
     * 
     * @since 8
     */
    public void cleanupSession(String userSessionToken);

    /**
     * Searches for the specified sequence snippet. If no preferred search domain is specified the first available one will be used. If the preferred
     * search domain doesn't exist or isn't available also the first one will be used.
     * 
     * @param preferredSearchDomainOrNull The key of the preferred search domain or <code>null</code>.
     * @param sequenceSnippet Snippet of nucleotid or aminoacid sequence.
     * @param optionalParametersOrNull Optional parameters. Can be <code>null</code>. The semantics depends on the type of the used sequence database.
     * @since 10
     */
    @Conversational(progress = Progress.AUTOMATIC)
    public List<SearchDomainSearchResult> searchForEntitiesWithSequences(String sessionToken,
            String preferredSearchDomainOrNull, String sequenceSnippet,
            Map<String, String> optionalParametersOrNull);

    /**
     * Lists all available search domains.
     * 
     * @since 10
     */
    @Conversational(progress = Progress.AUTOMATIC)
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken);

}
