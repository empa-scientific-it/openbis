/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * This interface is very similar to {@link IServiceForDataStoreServer} but <code>sessionToken</code> has been removed from most methods.
 * 
 * @see IServiceForDataStoreServer
 * @author Christian Ribeaud
 */
public interface IEncapsulatedOpenBISService extends IEncapsulatedBasicOpenBISService
{

    /**
     * Get the basic version of this service, that will filter the results.
     * 
     * @param userName The user used for filtering.
     */
    @ManagedAuthentication
    public IEncapsulatedBasicOpenBISService getBasicFilteredOpenBISService(String userName);

    /**
     * Tries to get the data set location for the specified data set code, using the ETL server's session token.
     */
    @ManagedAuthentication
    public IDatasetLocationNode tryGetDataSetLocation(final String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to get the data set for the specified data set code and specified session.
     */
    @ManagedAuthentication
    public AbstractExternalData tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Checks if the current user has INSTANCE_ADMIN access rights.
     */
    @ManagedAuthentication
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks if the current user has PROJECT_POWER_USER access rights.
     */
    @ManagedAuthentication
    public void checkProjectPowerUserAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks if the current user has access rights to a dataset with the specified data set code.
     */
    @ManagedAuthentication
    public void checkDataSetAccess(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Check which of the list of of data sets the current user can access.
     * 
     * @param sessionToken The user's session token.
     * @param dataSetCodes The data set codes the user wants to access.
     */
    @ManagedAuthentication
    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes)
            throws UserFailureException;

    /**
     * Gets all sample in accordance to the specified criteria.
     */
    @ManagedAuthentication
    public List<Sample> listSamples(final ListSampleCriteria criteria) throws UserFailureException;

    /**
     * Tries to get the sample identifier for the sample with specified permanent ID.
     * 
     * @return <code>null</code> if nothing found.
     */
    @ManagedAuthentication
    public SampleIdentifier tryGetSampleIdentifier(String samplePermID) throws UserFailureException;

    /**
     * Tries to get the sample identifier for the sample with specified permanent ID.
     * 
     * @return <code>null</code> if nothing found.
     */
    @ManagedAuthentication
    public Map<String, SampleIdentifier> listSampleIdentifiers(List<String> samplePermID)
            throws UserFailureException;

    /**
     * For given (@code name} and {@code ownerId} returns the corresponding {@link Metaproject}
     */
    @ManagedAuthentication
    public Metaproject tryGetMetaproject(String name, String ownerId);

    /**
     * Gets the experiment type with assigned property types for the specified experiment type code.
     */
    @ManagedAuthentication
    public ExperimentType getExperimentType(String experimentTypeCode) throws UserFailureException;

    /**
     * Gets the sample type with assigned property types for the specified sample type code.
     */
    @ManagedAuthentication
    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException;

    /**
     * Lists all data sets of the specified experiment ID.
     */
    @ManagedAuthentication
    public List<AbstractExternalData> listDataSetsByExperimentID(long experimentID)
            throws UserFailureException;

    /**
     * Lists all data sets of the specified sample ID.
     * 
     * @param showOnlyDirectlyConnected If <code>true</code> only directly connected data sets are returned.
     */
    @ManagedAuthentication
    public List<AbstractExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * Returns all data sets found for specified data set codes.
     * 
     * @return plain data sets without properties, samples, and experiments.
     */
    @ManagedAuthentication
    public List<AbstractExternalData> listDataSetsByCode(List<String> dataSetCodes)
            throws UserFailureException;

    /**
     * Registers the specified experiment.
     * 
     * @return the technical ID of the new experiment
     */
    @ManagedAuthentication
    public long registerExperiment(final NewExperiment experiment) throws UserFailureException;

    /**
     * Registers the specified sample.
     * 
     * @return the technical ID of the new sample
     */
    @ManagedAuthentication
    public long registerSample(final NewSample newSample, String userIDOrNull)
            throws UserFailureException;

    /**
     * Registers samples in batches.
     */
    @ManagedAuthentication
    public void registerSamples(List<NewSamplesWithTypes> newSamples, String userIDOrNull)
            throws UserFailureException;

    /**
     * Updates sample specified by the argument.
     */
    @ManagedAuthentication
    public void updateSample(SampleUpdatesDTO sampleUpdate) throws UserFailureException;

    /**
     * Registers the specified data.
     * <p>
     * As side effect, sets <i>data set code</i> in {@link DataSetInformation#getExtractableData()}.
     * </p>
     */
    @ManagedAuthentication
    public void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data) throws UserFailureException;

    /**
     * Tries to return the properties of the top sample of the sample with given <var>sampleIdentifier</var>. The top sample is the root of the sample
     * relationship graph of this sample. If a sample has multiple parents or no parents at all, it is considered its own top sample.
     * 
     * @return <code>null</code> if no appropriated sample is found. Returns an empty array if a sample is found with no properties.
     */
    @ManagedAuthentication
    public IEntityProperty[] tryGetPropertiesOfTopSample(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Tries to return the properties of the sample with given <var>sampleIdentifier</var>.. If sample has no top sample, its own properties are
     * returned.
     * 
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a sample found with no properties.
     */
    @ManagedAuthentication
    public IEntityProperty[] tryGetPropertiesOfSample(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /** See {@link IServiceForDataStoreServer#listSamplesByCriteria(String, ListSamplesByPropertyCriteria)} */
    @ManagedAuthentication
    public List<Sample> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /** See {@link IServiceForDataStoreServer#listShareIds(String, String)} */
    @ManagedAuthentication
    public List<DataSetShareId> listDataSetShareIds() throws UserFailureException;

    /**
     * Returns informations about all physical data sets which belong to the calling data store server.
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listPhysicalDataSets() throws UserFailureException;

    /**
     * Returns informations about the <var>chunkSize</var> oldest physical data sets which belong to the calling data store server.
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(int chunkSize)
            throws UserFailureException;

    /**
     * Returns informations about the <var>chunkSize</var> oldest physical data sets newer than <var>newerThan</var> which belong to the calling data
     * store server.
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(Date youngerThan,
            int chunkSize) throws UserFailureException;

    /**
     * Returns informations about physical data sets with unknown size that belong to the calling data store server.
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsWithUnknownSize(int chunkSize, String dataSetCodeLowerLimit)
            throws UserFailureException;

    /**
     * Returns informations about physical data sets with certain archiving status
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsByArchivingStatus(DataSetArchivingStatus archivingStatus, Boolean presentInArchive)
            throws UserFailureException;

    /**
     * Updates sizes of the specified physical data sets (map key: data set code, map value: data set size).
     */
    @ManagedAuthentication
    public void updatePhysicalDataSetsSize(Map<String, Long> sizeMap) throws UserFailureException;

    /** @see IServiceForDataStoreServer#listDataSets(String, String, TrackingDataSetCriteria) */
    @ManagedAuthentication
    public List<AbstractExternalData> listNewerDataSets(TrackingDataSetCriteria criteria)
            throws UserFailureException;

    /**
     * Creates and returns a new permanent ID that can be used to identify experiments, samples or datasets.
     */
    @ManagedAuthentication
    public String createPermId();

    /**
     * Creates and returns a list new permanent IDs that can be used to identify experiments, samples or datasets.
     */
    @ManagedAuthentication
    public List<String> createPermIds(int n);

    /**
     * Creates a new unique ID which can be used to create codes which are guaranteed to be unique.
     */
    @ManagedAuthentication
    public long drawANewUniqueID();

    /**
     * Creates a list of specified size with new unique ID for the specified entity kind.
     */
    @ManagedAuthentication
    public List<String> generateCodes(String prefix, EntityKind entityKind, int size);

    /**
     * Returns the version of the service.
     */
    @ManagedAuthentication
    public int getVersion();

    /**
     * Returns the home database instance.
     */
    @ManagedAuthentication
    public DatabaseInstance getHomeDatabaseInstance();

    /**
     * List data sets deleted after the last seen deletion event. If event id is null all deleted datasets will be returned.
     * 
     * @param maxDeletionDateOrNull when specified only lists data sets that have been deleted before it.
     */
    @ManagedAuthentication
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDateOrNull);

    /**
     * Updates specified properties of given data set. It only adds new properties and don't update existing ones.
     */
    @ManagedAuthentication
    public void updateDataSet(String code, List<NewProperty> properties, SpaceIdentifier space)
            throws UserFailureException;

    /**
     * Updates share id and size of specified data set.
     */
    @ManagedAuthentication
    public void updateShareIdAndSize(String dataSetCode, String shareId, long size)
            throws UserFailureException;

    /**
     * Checks if the dataset is on the TrasCan or Deleted
     */
    @ManagedAuthentication
    public boolean isDataSetOnTrashCanOrDeleted(String dataSetCode);

    //
    // Archiving
    //

    /** See {@link IServiceForDataStoreServer#listAvailableDataSets(String, String, ArchiverDataSetCriteria)} */
    @ManagedAuthentication
    public List<AbstractExternalData> listAvailableDataSets(ArchiverDataSetCriteria criteria)
            throws UserFailureException;

    /** See {@link IServiceForDataStoreServer#archiveDatasets(String, List, boolean)} */
    @ManagedAuthentication
    public void archiveDataSets(List<String> dataSetCodes, boolean removeFromDataStore, Map<String, String> options)
            throws UserFailureException;

    /** See {@link IServiceForDataStoreServer#unarchiveDatasets(String, List)} */
    @ManagedAuthentication
    public void unarchiveDataSets(List<String> dataSetCodes) throws UserFailureException;

    /**
     * See {@link IServiceForDataStoreServer#updateDataSetStatuses(String, List, DataSetArchivingStatus, boolean)}
     */
    @ManagedAuthentication
    public void updateDataSetStatuses(List<String> codes, DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException;

    /**
     * See {@link IServiceForDataStoreServer#updateDataSetStatuses(String, List, DataSetArchivingStatus, boolean)}
     */
    @ManagedAuthentication
    public boolean compareAndSetDataSetStatus(String dataSetCode, DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
            throws UserFailureException;

    /** See {@link IServiceForDataStoreServer#checkSpaceAccess(String, SpaceIdentifier)} */
    @ManagedAuthentication
    public void checkSpaceAccess(String sToken, SpaceIdentifier spaceId);

    /** See {@link IServiceForDataStoreServer#checkExperimentAccess(String, String)} */
    @ManagedAuthentication
    public void checkExperimentAccess(String sToken, String experimentIdentifier);

    /** See {@link IServiceForDataStoreServer#checkSampleAccess(String, String)} */
    @ManagedAuthentication
    public void checkSampleAccess(String sToken, String sampleIdentifier);

    /**
     * See {@link IServiceForDataStoreServer#tryAuthenticate(String, String)}
     * 
     * @param user
     * @param password
     */
    @ManagedAuthentication
    public SessionContextDTO tryAuthenticate(String user, String password);

    /**
     * See {@link IServiceForDataStoreServer#tryGetSession(String)}
     * 
     * @param sessionToken
     */
    @ManagedAuthentication
    public SessionContextDTO tryGetSession(String sessionToken);

    /**
     * See @{link {@link IServiceForDataStoreServer#checkSession(String)}.
     */
    @ManagedAuthentication
    public void checkSession(String sessionToken);

    @ManagedAuthentication
    public Map<String, String> getServerInformation();

    /**
     * Return a list of users who could be considered administrators. See {@link IServiceForDataStoreServer#listAdministrators(String)}
     */
    @ManagedAuthentication
    public List<Person> listAdministrators();

    /**
     * Return the user that matches this username or email. See {@link IServiceForDataStoreServer#tryPersonWithUserIdOrEmail(String, String)}
     */
    @ManagedAuthentication
    public Person tryPersonWithUserIdOrEmail(String useridOrEmail);

    /**
     * Register a new sample and data set in one transaction. Return the sample.
     * {@link IServiceForDataStoreServer#registerSampleAndDataSet(String, NewSample, NewExternalData, String)}
     */
    @ManagedAuthentication
    public Sample registerSampleAndDataSet(NewSample newSample, NewExternalData externalData,
            String userIdOrNull);

    /**
     * Update a sample and register a data set in one transaction. Return the sample.
     * {@link IServiceForDataStoreServer#updateSampleAndRegisterDataSet(String, SampleUpdatesDTO, NewExternalData)}
     */
    @ManagedAuthentication
    public Sample updateSampleAndRegisterDataSet(SampleUpdatesDTO newSample,
            NewExternalData externalData);

    /**
     * {@link IServiceForDataStoreServer#performEntityOperations(String, AtomicEntityOperationDetails)}
     */
    @ManagedAuthentication
    public AtomicEntityOperationResult performEntityOperations(
            AtomicEntityOperationDetails operationDetails);

    /**
     * {@link IServiceForDataStoreServer#listProjects(String)}
     */
    @ManagedAuthentication
    public List<Project> listProjects();

    /**
     * {@link IServiceForDataStoreServer#removeDataSetsPermanently(String, List, String)}
     */
    @ManagedAuthentication
    public void removeDataSetsPermanently(List<String> dataSetCodes, String reason);

    /**
     * {@link IServiceForDataStoreServer#updateDataSet(String, DataSetUpdatesDTO)}
     */
    @ManagedAuthentication
    public void updateDataSet(DataSetUpdatesDTO dataSetUpdates);

    /**
     * {@link IServiceForDataStoreServer#getTrustedCrossOriginDomains(String)}
     */
    @ManagedAuthentication
    public List<String> getTrustedCrossOriginDomains();

    /**
     * {@link IServiceForDataStoreServer#setStorageConfirmed(String, List)}
     */
    @ManagedAuthentication
    public void setStorageConfirmed(List<String> dataSetCodes);

    /**
     * {@link IServiceForDataStoreServer#markSuccessfulPostRegistration(String, String)}
     */
    @ManagedAuthentication
    public void markSuccessfulPostRegistration(String dataSetCode);

    /**
     * {@link IServiceForDataStoreServer#notifyDatasetAccess(String, String)}
     */
    @ManagedAuthentication
    public void notifyDatasetAccess(String dataSetCode);

    /**
     * {@link IServiceForDataStoreServer#listDataSetsForPostRegistration(String, String)}
     */
    @ManagedAuthentication
    public List<AbstractExternalData> listDataSetsForPostRegistration();

    /**
     * {@link IServiceForDataStoreServer#didEntityOperationsSucceed(String, TechId)}
     */
    @ManagedAuthentication
    public EntityOperationsState didEntityOperationsSucceed(TechId registrationId);

    /**
     * {@link IServiceForDataStoreServer#heartbeat(String)}
     */
    @ManagedAuthentication
    public void heartbeat();

    /**
     * {@link IServiceForDataStoreServer#doesUserHaveRole(String, String, String, String)}
     */
    @ManagedAuthentication
    public boolean doesUserHaveRole(String user, String roleCode, String spaceOrNull);

    /**
     * {@link IServiceForDataStoreServer#filterToVisibleDataSets(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleDataSets(String user, List<String> dataSetCodes);

    /**
     * {@link IServiceForDataStoreServer#filterToVisibleExperiments(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleExperiments(String user, List<String> experimentIds);

    /**
     * {@link IServiceForDataStoreServer#filterToVisibleSamples(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleSamples(String user, List<String> sampleIdentifiers);

    /**
     * For given code returns the corresponding {@link ExternalDataManagementSystem}.
     */
    @ManagedAuthentication
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(
            String externalDataManagementSystemCode);

    /**
     * Return the authorization groups in openBIS
     */
    @ManagedAuthentication
    public List<AuthorizationGroup> listAuthorizationGroups();

    /**
     * Return the authorization groups in openBIS for a particular user
     */
    @ManagedAuthentication
    public List<AuthorizationGroup> listAuthorizationGroupsForUser(String userId);

    /**
     * Return the users in a particular authorization group
     */
    @ManagedAuthentication
    public List<Person> listUsersForAuthorizationGroup(TechId authorizationGroupId);

    /**
     * Return all role assignments in the database
     */
    @ManagedAuthentication
    public List<RoleAssignment> listRoleAssignments();

    /**
     * Returns a list of attachments with all versions.
     */
    @ManagedAuthentication
    public List<Attachment> listAttachments(AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId);

    /**
     * Returns an attachment content. If the version is not specified then the latest version of the attachment is returned.
     */
    @ManagedAuthentication
    public InputStream getAttachmentContent(AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId, String fileName,
            Integer versionOrNull);

    /**
     * Returns list of not archived datasets marked with a tag
     */
    @ManagedAuthentication
    public List<AbstractExternalData> listNotArchivedDatasetsWithMetaproject(IMetaprojectId metaprojectId);

    @ManagedAuthentication
    public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(List<? extends IPersonalAccessTokenId> personalAccessTokenIds,
            PersonalAccessTokenFetchOptions fetchOptions);

    @ManagedAuthentication
    public void updatePersonalAccessTokens(List<PersonalAccessTokenUpdate> personalAccessTokenUpdates);

    @ManagedAuthentication
    public SearchResult<SessionInformation> searchSessionInformation(SessionInformationSearchCriteria searchCriteria,
            SessionInformationFetchOptions fetchOptions);

}