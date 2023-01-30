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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.openbis.common.conversation.annotation.Progress;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Service interface for the Data Store server.
 * 
 * @author Christian Ribeaud
 */
public interface IServiceForDataStoreServer extends IServer, ISessionProvider
{
    /**
     * Returns the home database instance.
     */
    @Transactional(readOnly = true)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken);

    /**
     * Registers a Data Store Server for the specified info.
     */
    @Transactional
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo dataStoreServerInfo);

    /**
     * Returns the specified experiment or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     */
    @Transactional(readOnly = true)
    public Experiment tryGetExperiment(String sessionToken,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    public Material tryGetMaterial(String sessionToken, MaterialIdentifier materialIdentifier);

    /**
     * For given {@code name} and {@code ownerId} returns the corresponding {@link Metaproject}.
     */
    @Transactional(readOnly = true)
    public Metaproject tryGetMetaproject(String sessionToken, String name, String ownerId);

    /**
     * Tries to get the identifier of sample with specified permanent ID.
     * 
     * @return <code>null</code> if nothing found.
     */
    @Transactional(readOnly = true)
    public SampleIdentifier tryGetSampleIdentifier(String sessionToken, String samplePermID)
            throws UserFailureException;

    /**
     * Gets the list of identifiers of samples with given perm ids.
     * 
     * @return <code>null</code> if nothing found.
     */
    @Transactional(readOnly = true)
    public Map<String, SampleIdentifier> listSamplesByPermId(final String sessionToken,
            List<String> samplePermIds);

    /**
     * Returns the ExperimentType together with assigned property types for specified experiment type code.
     */
    @Transactional(readOnly = true)
    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the experiment with properties.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no sample attached to an experiment could be found for given <var>sampleIdentifier</var>.
     */
    @Transactional(readOnly = true)
    public Sample tryGetSampleWithExperiment(final String sessionToken,
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Returns a list of terms belonging to given vocabulary.
     */
    @Transactional(readOnly = true)
    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabulary)
            throws UserFailureException;

    /**
     * Returns a vocabulary with given code
     */
    @Transactional(readOnly = true)
    public Vocabulary tryGetVocabulary(String sessionToken, String code);

    /**
     * Returns the SampleType together with assigned property types for specified sample type code.
     */
    @Transactional(readOnly = true)
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException;

    /**
     * Returns the data set type together with assigned property types for specified data set type code.
     */
    @Transactional(readOnly = true)
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
            throws UserFailureException;

    /**
     * For given experiment {@link TechId} returns the corresponding list of {@link AbstractExternalData}.
     * 
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listDataSetsByExperimentID(final String sessionToken,
            final TechId experimentID) throws UserFailureException;

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link AbstractExternalData}.
     * 
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listDataSetsBySampleID(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
            throws UserFailureException;

    /**
     * Returns all data sets found for specified data set codes.
     * 
     * @return plain data sets without properties, samples, and experiments.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listDataSetsByCode(String sessionToken,
            List<String> dataSetCodes)
            throws UserFailureException;

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    public List<Sample> listSamples(final String sessionToken, final ListSampleCriteria criteria);

    /**
     * Tries to return the properties of the top sample of the sample with given <var>sampleIdentifier</var>. The top sample is the root of the sample
     * relationship graph of this sample. If a sample has multiple parents or no parents at all, it is considered its own top sample.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a sample found with no properties.
     */
    @Transactional(readOnly = true)
    public IEntityProperty[] tryGetPropertiesOfTopSample(final String sessionToken,
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Tries to return the properties of the sample with given <var>sampleIdentifier</var>.. If sample has no top sample, its own properties are
     * returned.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a sample found with no properties.
     */
    @Transactional(readOnly = true)
    public IEntityProperty[] tryGetPropertiesOfSample(final String sessionToken,
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Registers/updates various entities in one transaction.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException;

    /**
     * Registers experiment.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public long registerExperiment(String sessionToken, NewExperiment experiment)
            throws UserFailureException;

    /**
     * Registers samples in batches.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void registerSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType, String userIdOrNull)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public long registerSample(final String sessionToken, final NewSample newSample,
            String userIDOrNull) throws UserFailureException;

    /**
     * Saves changed sample.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSample(String sessionToken, SampleUpdatesDTO updates);

    /**
     * Registers the specified data connected to a sample.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @param externalData Data set to be registered. It is assumed that the attributes <code>location</code>, <code>fileFormatType</code>,
     *            <code>dataSetType</code>, and <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence layer.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(final String sessionToken, final SampleIdentifier sampleIdentifier,
            final NewExternalData externalData) throws UserFailureException;

    /**
     * Registers the specified data connected to an experiment.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     * @param externalData Data set to be registered. It is assumed that the attributes <code>location</code>, <code>fileFormatType</code>,
     *            <code>dataSetType</code>, and <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence layer.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(final String sessionToken,
            final ExperimentIdentifier experimentIdentifier, final NewExternalData externalData)
            throws UserFailureException;

    /**
     * Checks that the user of specified session has INSTANCE_ADMIN access rights.
     */
    @Transactional(readOnly = true)
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks that the user of specified session has PROJECT_POWER_USER access rights.
     */
    @Transactional(readOnly = true)
    public void checkProjectPowerUserAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Does nothing besides checking that the current user has rights to access the content of the dataset.
     */
    @Transactional(readOnly = true)
    public void checkDataSetAccess(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Check if the current user can access all the data sets in the list
     * 
     * @param sessionToken The user's session token.
     * @param dataSetCodes The data set codes the user wants to access.
     */
    @Transactional(readOnly = true)
    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes);

    /**
     * Tries to return a location of the data set specified by its code.
     */
    @Transactional(readOnly = true)
    public IDatasetLocationNode tryGetDataSetLocation(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to return the data set specified by its code.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to return the data set specified by its code, but without any fetch options.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData tryGetThinDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to return the data set specified by its code, but only if it belongs to the data store of the caller.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData tryGetLocalDataSet(String sessionToken, String dataSetCode,
            String dataStore) throws UserFailureException;

    /**
     * Create and return a new permanent id that can be used to identify samples, experiments and datasets.
     */
    @Transactional
    public String createPermId(final String sessionToken) throws UserFailureException;

    /**
     * Create and return a list of new permanent ids that can be used to identify samples, experiments and datasets.
     */
    @Transactional
    public List<String> createPermIds(final String sessionToken, int n) throws UserFailureException;

    /**
     * Draw a new unique ID. The returned value is guaranteed to be unique.
     */
    @Transactional
    public long drawANewUniqueID(String sessionToken) throws UserFailureException;

    /**
     * Lists samples codes filtered by specified criteria, see {@link ListSamplesByPropertyCriteria} to see the details.
     */
    @Transactional(readOnly = true)
    public List<Sample> listSamplesByCriteria(final String sessionToken,
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException;

    /**
     * Lists share ids of all data sets belonging to chosen data store (even the ones in trash!).
     */
    @Transactional(readOnly = true)
    public List<DataSetShareId> listShareIds(final String sessionToken, String dataStore)
            throws UserFailureException;

    /**
     * Lists file-content data sets belonging to specified data store.
     */
    @Transactional(readOnly = true)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSets(final String sessionToken,
            String dataStore) throws UserFailureException;

    /**
     * Lists the <var>limit</var> oldest physical data sets belonging to specified data store.
     * <p>
     * The result is ordered by registration date in ascending order.
     */
    @Transactional(readOnly = true)
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(final String sessionToken,
            String dataStore, int limit) throws UserFailureException;

    /**
     * Returns informations about physical data sets with unknown size that belong to the specified data store server.
     */
    @Transactional(readOnly = true)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsByArchivingStatus(String sessionToken, String dataStoreCode,
            DataSetArchivingStatus archivingStatus, Boolean presentInArchive);

    /**
     * Lists the <var>limit</var> oldest physical data sets younger than <var>youngerThan</var> belonging to specified data store.
     * <p>
     * The result is ordered by registration date in ascending order.
     */
    @Transactional(readOnly = true)
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(final String sessionToken,
            String dataStore, Date youngerThan, int limit) throws UserFailureException;

    /**
     * Returns informations about physical data sets with unknown size that belong to the specified data store server.
     */
    @Transactional(readOnly = true)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsWithUnknownSize(String sessionToken, String dataStoreCode, int chunkSize,
            String dataSetCodeLowerLimit);

    /**
     * Updates sizes of the specified physical data sets (map key: data set code, map value: data set size).
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updatePhysicalDataSetsSize(String sessionToken, Map<String, Long> sizeMap);

    /**
     * List data sets deleted after specified date.
     */
    @Transactional(readOnly = true)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDataOrNull);

    /**
     * List 'AVAILABLE' data sets (not locked) that match given criteria.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listAvailableDataSets(String sessionToken,
            String dataStoreCode,
            ArchiverDataSetCriteria criteria);

    /**
     * List data sets from specified store which are younger then the specified one.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria);

    /**
     * List all experiments for a given project identifier.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(String sessionToken, ProjectIdentifier projectIdentifier);

    /**
     * List experiments for a given list of experiment identifiers.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(String sessionToken,
            List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

    /**
     * List experiments for a given list of project identifiers.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperimentsForProjects(String sessionToken,
            List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

    /**
     * List all projects that the user can see.
     */
    @Transactional(readOnly = true)
    public List<Project> listProjects(String sessionToken);

    /**
     * Lists materials using given criteria.
     * 
     * @return a sorted list of {@link Material}.
     */
    @Transactional(readOnly = true)
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties);

    /**
     * Adds specified properties of given data set. Properties defined before will not be updated.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, final SpaceIdentifier identifier) throws UserFailureException;

    /**
     * Checks if the dataset is on the TrasCan or Deleted
     */
    @Transactional(readOnly = true)
    public boolean isDataSetOnTrashCanOrDeleted(String sessionToken, String dataSetCode);

    /**
     * Updates share id and size of specified data set.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateShareIdAndSize(String sessionToken, String dataSetCode, String shareId,
            long size) throws UserFailureException;

    /**
     * Updates status of given data sets.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSetStatuses(String sessionToken, List<String> dataSetCodes,
            final DataSetArchivingStatus newStatus, boolean presentInArchive)
            throws UserFailureException;

    /**
     * Set the status for a given dataset to the given new status value if the current status equals an expected value.
     * 
     * @return true if the update is successful, false if the current status is different than <code>oldStatus</code>.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public boolean compareAndSetDataSetStatus(String token, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException;

    /**
     * Schedules archiving of specified data sets.
     * 
     * @param removeFromDataStore when set to <code>true</code> the data sets will be removed from the data store after a successful archiving
     *            operation.
     * @return number of data sets scheduled for archiving.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int archiveDatasets(String sessionToken, List<String> datasetCodes,
            boolean removeFromDataStore, Map<String, String> options);

    /**
     * Schedules unarchiving of specified data sets.
     * 
     * @return number of data sets scheduled for unarchiving.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int unarchiveDatasets(String sessionToken, List<String> datasetCodes);

    /**
     * Check if the user has USER access on the space
     * 
     * @param sessionToken The user's session token.
     * @param spaceId The id for the space the user wants to access
     */
    @Transactional(readOnly = true)
    public void checkSpaceAccess(String sessionToken, SpaceIdentifier spaceId);

    /**
     * Check if the user has USER access on the experiment
     * 
     * @param sessionToken The user's session token.
     * @param experimentIdentifier The identifier for the experiment the user wants to access
     */
    @Transactional(readOnly = true)
    public void checkExperimentAccess(String sessionToken, String experimentIdentifier);

    /**
     * Check if the user has USER access on the sample
     * 
     * @param sessionToken The user's session token.
     * @param sampleIdentifier The identifier for the sample the user wants to access
     */
    @Transactional(readOnly = true)
    public void checkSampleAccess(String sessionToken, String sampleIdentifier);

    /**
     * Returns a list of unique codes for the specified entity kind.
     */
    @Transactional
    public List<String> generateCodes(String sessionToken, String prefix, EntityKind entityKind,
            int number);

    /**
     * Returns a list users who could be considered administrators.
     */
    @Transactional(readOnly = true)
    public List<Person> listAdministrators(String sessionToken);

    /**
     * Search for the person that matches the given userId or email. The search first tries to find a userId match; if none was found, it searches for
     * an Email match.
     */
    @Transactional(readOnly = true)
    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail);

    /**
     * Registers a sample and data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param newSample The new sample to register.
     * @param externalData Data set to be registered. It is assumed that the attributes <code>location</code>, <code>fileFormatType</code>,
     *            <code>dataSetType</code>, and <code>locatorType</code> are not-<code>null</code>.
     * @param userIdOrNull The user id on whose behalf we are registering the sample
     * @throws UserFailureException if given data set code could not be found in the persistence layer.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public Sample registerSampleAndDataSet(final String sessionToken, final NewSample newSample,
            final NewExternalData externalData, String userIdOrNull) throws UserFailureException;

    /**
     * Updates a sample and registers a data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param updates The sample updates to apply
     * @param externalData Data set to be registered. It is assumed that the attributes <code>location</code>, <code>fileFormatType</code>,
     *            <code>dataSetType</code>, and <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence layer.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public Sample updateSampleAndRegisterDataSet(String sessionToken, SampleUpdatesDTO updates,
            NewExternalData externalData);

    /**
     * Updates a sample and registers a data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param operationDetails A DTO containing information about the entities to change / register.
     * @throws UserFailureException if given data set code could not be found in the persistence layer.
     */
    @Transactional
    @Conversational(progress = Progress.MANUAL)
    @DatabaseUpdateModification(value = { ObjectKind.SAMPLE, ObjectKind.EXPERIMENT, ObjectKind.DATA_SET })
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SPACE, ObjectKind.PROJECT, ObjectKind.SAMPLE, ObjectKind.EXPERIMENT,
            ObjectKind.DATA_SET })
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            AtomicEntityOperationDetails operationDetails);

    /**
     * Tries to return the space specified by its identifier.
     */
    @Transactional(readOnly = true)
    public Space tryGetSpace(String sessionToken, SpaceIdentifier spaceIdentifier);

    /**
     * Tries to return the project specified by its identifier.
     */
    @Transactional(readOnly = true)
    public Project tryGetProject(String sessionToken, ProjectIdentifier projectIdentifier);

    /**
     * Search for samples matching the provided criteria.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param searchCriteria The criteria for samples.
     * @return A collection of samples matching the search criteria.
     */
    @Transactional(readOnly = true)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Search for experiments matching the provided criteria.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param searchCriteria The criteria for experiments.
     * @return A collection of experiments matching the search criteria.
     */
    @Transactional(readOnly = true)
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Search for data sets matching the provided criteria.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param searchCriteria The criteria for data sets.
     * @return A collection of data sets matching the search criteria.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            SearchCriteria searchCriteria);

    /**
     * permanently deletes a list of data sets.
     */
    @Transactional
    @DatabaseUpdateModification(value = { ObjectKind.SAMPLE, ObjectKind.EXPERIMENT })
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET })
    public void removeDataSetsPermanently(String sessionToken, List<String> dataSetCodes,
            String reason);

    /**
     * updates a data set.
     */
    @Transactional
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void updateDataSet(String sessionToken, DataSetUpdatesDTO dataSetUpdates);

    /**
     * Returns a list of configured trusted domains which can host external shared web resources. Typically these are lightweight webapps that
     * integrate with openBIS via JSON-RPC services.
     * <p>
     * Can return empty list.
     */
    @Transactional
    public List<String> getTrustedCrossOriginDomains(String sessionToken);

    /**
     * Marks the storage of datasets as confirmed. Adds the given dataset to post-registration queue
     */
    @Transactional
    @DatabaseUpdateModification(value = { ObjectKind.DATA_SET })
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.POSTREGISTRATION_QUEUE })
    public void setStorageConfirmed(String sessionToken, List<String> dataSetCodes);

    /**
     * Informs that the post-registration task for a given dataset was performed, and it should be removed from the post-registration queue.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.POSTREGISTRATION_QUEUE })
    public void markSuccessfulPostRegistration(String token, String dataSetCode);

    /**
     * Informs that the data set has been accessed
     */
    @Transactional
    public void notifyDatasetAccess(String token, String dataSetCode);

    /**
     * Gets the list of all datasets, which are in the post-registration queue.
     */
    @Transactional
    public List<AbstractExternalData> listDataSetsForPostRegistration(String token,
            String dataStoreCode);

    /**
     * Return true if the log indicates that the performEntityOperations invocation for the given registrationId succeeded.
     */
    @Transactional(readOnly = true)
    public EntityOperationsState didEntityOperationsSucceed(String token, TechId registrationId);

    /**
     * Method that does nothing. Use it to check if the connection to the server is working.
     */
    @Transactional(readOnly = true)
    public void heartbeat(String token);

    /**
     * Check whether the specified user has the given role
     */
    @Transactional(readOnly = true)
    public boolean doesUserHaveRole(String token, String user, String roleCode, String spaceOrNull);

    /**
     * Filter list of datasets to only those visible by the given user
     */
    @Transactional(readOnly = true)
    public List<String> filterToVisibleDataSets(String token, String user, List<String> dataSetCodes);

    /**
     * Filter list of experiments to only those visible by the given user
     */
    @Transactional(readOnly = true)
    public List<String> filterToVisibleExperiments(String token, String user,
            List<String> experimentIds);

    /**
     * Filter list of samples to only those visible by the given user
     */
    @Transactional(readOnly = true)
    public List<String> filterToVisibleSamples(String token, String user,
            List<String> samplesIndentifiers);

    /**
     * For given code returns the corresponding {@link ExternalDataManagementSystem}.
     */
    @Transactional(readOnly = true)
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(String token,
            String externalDataManagementSystemCode);

    /**
     * For given entity type list all assigned property definitions.
     */
    @Transactional(readOnly = true)
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForType(
            String sessionToken, String code,
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind);

    /**
     * List metaprojects for given user
     */
    @Transactional(readOnly = true)
    public List<Metaproject> listMetaprojects(String sessionToken, String userId);

    /**
     * List metaproject assignments for given metaproject name and owner
     */
    @Transactional(readOnly = true)
    public MetaprojectAssignments getMetaprojectAssignments(String systemSessionToken, String name,
            String userName, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions);

    /**
     * List metaprojects for the given entity
     */
    @Transactional(readOnly = true)
    public List<Metaproject> listMetaprojectsForEntity(String systemSessionToken, String userId,
            IObjectId entityId);

    /**
     * List metaprojects for the given entities
     */
    @Transactional(readOnly = true)
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(String systemSessionToken, String userId,
            Collection<? extends IObjectId> entityIds);

    /**
     * Return the authorization groups in openBIS
     */
    @Transactional(readOnly = true)
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken);

    /**
     * Return the authorization groups in openBIS for a particular user
     */
    @Transactional(readOnly = true)
    public List<AuthorizationGroup> listAuthorizationGroupsForUser(String sessionToken, String userId);

    /**
     * Return the users in a particular authorization group
     */
    @Transactional(readOnly = true)
    public List<Person> listUsersForAuthorizationGroup(String sessionToken, TechId authorizationGroupId);

    /**
     * Return all role assignments in the database
     */
    @Transactional(readOnly = true)
    public List<RoleAssignment> listRoleAssignments(String sessionToken);

    /**
     * Returns a list of all attachments for the given attachment holder.
     */
    @Transactional(readOnly = true)
    public List<Attachment> listAttachments(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId);

    /**
     * Returns a content of the given attachment. If the version is not specified then the latest version of the attachment is returned.
     */
    @Transactional(readOnly = true)
    public AttachmentWithContent getAttachment(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId,
            String fileName,
            Integer versionOrNull);

    /**
     * Returns list of not archived data sets marked with a tag
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listNotArchivedDatasetsWithMetaproject(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Returns the specified experiment or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param permId a perm id which uniquely identifies the experiment.
     */
    @Transactional(readOnly = true)
    public Experiment tryGetExperimentByPermId(String sessionToken,
            PermId permId) throws UserFailureException;

    /**
     * Returns the specified project or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param permId a perm id which uniquely identifies the project.
     */
    @Transactional(readOnly = true)
    public Project tryGetProjectByPermId(String sessionToken,
            PermId permId) throws UserFailureException;

    /**
     * Returns the specified sample or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param permId a perm id which uniquely identifies the sample.
     */
    @Transactional(readOnly = true)
    public Sample tryGetSampleByPermId(String sessionToken,
            PermId permId) throws UserFailureException;

}
