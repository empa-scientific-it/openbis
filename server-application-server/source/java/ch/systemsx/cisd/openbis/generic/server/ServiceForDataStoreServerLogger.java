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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ServiceForDataStoreServerLogger extends AbstractServerLogger implements
        IServiceForDataStoreServer
{

    public ServiceForDataStoreServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public String createPermId(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "createPermId", "");
        return null;
    }

    @Override
    public List<String> createPermIds(String sessionToken, int n) throws UserFailureException
    {
        logTracking(sessionToken, "createPermIds", "NUMBER(%s)", n);
        return null;
    }

    @Override
    public long drawANewUniqueID(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "drawANewUniqueID", "");
        return 0;
    }

    @Override
    public DatabaseInstance getHomeDatabaseInstance(String sessionToken)
    {
        return null;
    }

    @Override
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        String code = info.getDataStoreCode();
        String downloadUrl = info.getDownloadUrl();
        int port = info.getPort();
        String dssSessionToken = info.getSessionToken();
        DatastoreServiceDescriptions services = info.getServicesDescriptions();
        logTracking(
                sessionToken,
                "registerDataStoreServer",
                "CODE(%s) DOWNLOAD-URL(%s) PORT(%s) DSS-TOKEN(%s) REPORTING_PLUGINS(%s), PROCESSING_PLUGINS(%s)",
                code, downloadUrl, port, dssSessionToken,
                services.getReportingServiceDescriptions(),
                services.getProcessingServiceDescriptions());
    }

    @Override
    public long registerSample(String sessionToken, NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%S) USER(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), userIDOrNull);
        return 0;
    }

    @Override
    public void updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "updateSample", "SAMPLE(%S)", updates.getSampleIdentifier());
    }

    @Override
    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException
    {
        List<NewExperiment> newExperiments = collection.getNewExperiments();
        List<NewExternalData> newDataSets = collection.getNewDataSets();
        logTracking(sessionToken, "registerEntities", "NEW_EXPERIMENTS(%s) NEW_DATA_SETS(%s)",
                newExperiments.size(), newDataSets.size());
    }

    @Override
    public long registerExperiment(String sessionToken, NewExperiment experiment)
            throws UserFailureException
    {
        logTracking(sessionToken, "registerExperiment", "EXPERIMENT_TYPE(%s) EXPERIMENT(%S)",
                experiment.getExperimentTypeCode(), experiment.getIdentifier());
        return 0;
    }

    @Override
    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "registerDataSet", "SAMPLE(%s) DATA_SET(%s)", sampleIdentifier,
                externalData);
    }

    @Override
    public void registerDataSet(String sessionToken, ExperimentIdentifier experimentIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "registerDataSet", "EXPERIMENT(%s) DATA_SET(%s)",
                experimentIdentifier, externalData);
    }

    public void deleteDataSet(String sessionToken, String dataSetCode, String reason)
            throws UserFailureException
    {
        logTracking(sessionToken, "deleteDataSet", "DATA_SET(%s) REASON(%s)", dataSetCode, reason);
    }

    @Override
    public Experiment tryGetExperiment(String sessionToken,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetExperiment", "EXPERIMENT(%s)", experimentIdentifier);
        return null;
    }

    @Override
    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        logAccess(sessionToken, "listSamples", "CRITERIA(%s)", criteria);
        return null;
    }

    @Override
    public Sample tryGetSampleWithExperiment(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryGetSampleWithExperiment", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    @Override
    public SampleIdentifier tryGetSampleIdentifier(String sessionToken, String samplePermID)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetSampleIdentifier", "SAMPLE(%s)", samplePermID);
        return null;
    }

    @Override
    public Map<String, SampleIdentifier> listSamplesByPermId(String sessionToken,
            List<String> samplePermIds)
    {
        logAccess(sessionToken, "listSamplesByPermId", "SAMPLES(%s)", samplePermIds);
        return null;
    }

    @Override
    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "getExperimentType", "EXPERIMENT_TYPE(%s)", experimentTypeCode);
        return null;
    }

    @Override
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "getSampleType", "SAMPLE_TYPE(%s)", sampleTypeCode);
        return null;
    }

    @Override
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
    {
        logAccess(sessionToken, "getDataSetType", "DATA_SET_TYPE(%s)", dataSetTypeCode);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSetsByExperimentID(String sessionToken,
            TechId experimentID)
            throws UserFailureException
    {
        logAccess(sessionToken, "listDataSetsByExperimentID", "EXPERIMENT_ID(%s)", experimentID);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSetsBySampleID(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "listDataSetsBySampleID", "SAMPLE_ID(%s)", sampleId);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSetsByCode(String sessionToken,
            List<String> dataSetCodes)
            throws UserFailureException
    {
        logAccess(sessionToken, "listDataSetsByCode", "DATA_SETS(%s)", dataSetCodes);
        return null;
    }

    @Override
    public IEntityProperty[] tryGetPropertiesOfTopSample(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetPropertiesOfTopSample", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    @Override
    public IEntityProperty[] tryGetPropertiesOfSample(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetPropertiesOfSample", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    @Override
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException
    {
        logAccess(sessionToken, "checkInstanceAdminAuthorization");
    }

    @Override
    public void checkProjectPowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        logAccess(sessionToken, "checkProjectPowerUserAuthorization");
    }

    @Override
    public void checkDataSetAccess(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "checkDataSetAccess", "DATA_SET(%s)", dataSetCode);
    }

    @Override
    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes)
    {
        logTracking(sessionToken, "checkDataSetCollectionAccess", "DATA_SET_CODES(%s)",
                dataSetCodes);
    }

    @Override
    public void checkSpaceAccess(String sessionToken, SpaceIdentifier spaceId)
            throws UserFailureException
    {
        logAccess(sessionToken, "checkSpaceAccess", "SPACE(%s)", spaceId);
    }

    @Override
    public void checkExperimentAccess(String sessionToken, String experimentIdentifier)
    {
        logAccess(sessionToken, "checkExperimentAccess", "EXPERIMENT(%s)", experimentIdentifier);
    }

    @Override
    public void checkSampleAccess(String sessionToken, String sampleIdentifier)
    {
        logAccess(sessionToken, "checkSampleAccess", "SAMPLE(%s)", sampleIdentifier);
    }

    @Override
    public IDatasetLocationNode tryGetDataSetLocation(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryGetDataSetLocation", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    @Override
    public AbstractExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryGetDataSet", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    @Override
    public AbstractExternalData tryGetThinDataSet(String sessionToken, String dataSetCode) throws UserFailureException
    {
        logAccess(sessionToken, "tryGetThinDataSet", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    @Override
    public AbstractExternalData tryGetLocalDataSet(String sessionToken, String dataSetCode,
            String dataStore) throws UserFailureException
    {
        logAccess(sessionToken, "tryGetLocalDataSet", "DATA_SET(%s) DATA_STORE_CODE(%s)",
                dataSetCode, dataStore);
        return null;
    }

    @Override
    public List<Sample> listSamplesByCriteria(String sessionToken,
            ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        logAccess(sessionToken, "listSamplesByCriteria", "criteria(%s)", criteria);
        return null;
    }

    @Override
    public List<DataSetShareId> listShareIds(String sessionToken, String dataStore)
            throws UserFailureException
    {
        logAccess(sessionToken, "listShareIds", "DATA_STORE(%s)", dataStore);
        return null;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSets(String sessionToken,
            String dataStore)
            throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "listFileDataSets", "DATA_STORE(%s)", dataStore);
        return null;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(String sessionToken,
            String dataStore, int limit) throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "listFileDataSets", "DATA_STORE(%s), LIMIT(%s)",
                dataStore, limit);
        return null;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(String sessionToken,
            String dataStore, Date youngerThan, int limit) throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "listFileDataSets",
                "DATA_STORE(%s), YOUNGER_THAN(%s), LIMIT(%s)", dataStore, youngerThan, limit);
        return null;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsByArchivingStatus(String sessionToken, String dataStoreCode,
            DataSetArchivingStatus archivingStatus, Boolean presentInArchive) throws UserFailureException
    {
        logAccess(sessionToken, "listPhysicalDataSetsByArchivingStatus", "DATA_STORE(%s) STATUS(%s) PRESENT(%s)", dataStoreCode, archivingStatus,
                presentInArchive);
        return null;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsWithUnknownSize(String sessionToken, String dataStoreCode, int chunkSize,
            String dataSetCodeLowerLimit)
    {
        logAccess(Level.DEBUG, sessionToken, "listPhysicalDataSetsWithUnknownSize", "DATA_STORE(%s) CHUNK_SIZE(%s) DATA_SET_CODE_LOWER_LIMIT(%s)",
                dataStoreCode, chunkSize, dataSetCodeLowerLimit);
        return null;
    }

    @Override
    public void updatePhysicalDataSetsSize(String sessionToken, Map<String, Long> sizeMap)
    {
        logAccess(Level.DEBUG, sessionToken, "updatePhysicalDataSetsSize", "CODES(%s)",
                sizeMap != null ? CollectionUtils.abbreviate(sizeMap.keySet(), 10) : Collections.emptySet());
    }

    @Override
    public List<AbstractExternalData> listAvailableDataSets(String sessionToken,
            String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        logAccess(sessionToken, "listAvailableDataSets", "DATA_STORE(%s) CRITERIA(%s)",
                dataStoreCode, criteria);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria)
    {
        logAccess(Level.DEBUG, sessionToken, "listDataSets", "DATA_STORE(%s) CRITERIA(%s)",
                dataStoreCode, criteria);
        return null;
    }

    public SamplePE getSampleWithProperty(String sessionToken, String propertyTypeCode,
            SpaceIdentifier groupIdentifier, String propertyValue)
    {
        logAccess(sessionToken, "getSampleWithProperty",
                "PROPERTY_TYPE(%s) SPACE(%s) PROPERTY_VALUE(%s)", propertyTypeCode,
                groupIdentifier, propertyValue);
        return null;
    }

    @Override
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDateOrNull)
    {
        logAccess(Level.DEBUG, sessionToken, "listDeletedDataSets", "LAST_SEEN_EVENT(%s)",
                (lastSeenDeletionEventIdOrNull == null ? "all" : "id > "
                        + lastSeenDeletionEventIdOrNull),
                (maxDeletionDateOrNull == null ? "all"
                        : "maxDeletionDate > " + maxDeletionDateOrNull));
        return null;
    }

    @Override
    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, SpaceIdentifier space) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSet", "DATA_SET_CODE(%s) PROPERTIES(%s)", dataSetCode,
                properties.size());
    }

    @Override
    public boolean isDataSetOnTrashCanOrDeleted(String sessionToken, String dataSetCode)
    {
        logAccess(Level.DEBUG, sessionToken, "isDataSetOnTrashCanOrDeleted", "DATA_SET_CODE(%s)", dataSetCode);
        return false;
    }

    @Override
    public void updateShareIdAndSize(String sessionToken, String dataSetCode, String shareId,
            long size) throws UserFailureException
    {
        logTracking(sessionToken, "updateShareIdAndSize",
                "DATA_SET_CODE(%s) SHARE_ID(%s) SIZE(%s)", dataSetCode, shareId, size);
    }

    @Override
    public void updateDataSetStatuses(String sessionToken, List<String> dataSetCodes,
            DataSetArchivingStatus newStatus, boolean presentInArchive) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSetStatus",
                "NO_OF_DATASETS(%s) STATUS(%s) PRESENT_IN_ARCHIVE(%s)", dataSetCodes.size(),
                newStatus, presentInArchive);
    }

    @Override
    public boolean compareAndSetDataSetStatus(String token, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException
    {
        logTracking(token, "compareAndSetDataSetStatus",
                "DATASET_COE(%s) OLD_STATUS(%s) NEW_STATUS(%s) NEW_PRESENT_IN_ARCHIVE(%s)",
                dataSetCode, oldStatus, newStatus, newPresentInArchive);
        return false;
    }

    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "getDefaultPutDataStoreBaseURL");
        return null;
    }

    @Override
    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabulary)
            throws UserFailureException
    {
        logAccess(sessionToken, "listVocabularyTerms", "VOCABULARY(%s)", vocabulary);
        return null;
    }

    @Override
    public void registerSamples(String sessionToken, List<NewSamplesWithTypes> newSamplesWithType,
            String userIdOrNull) throws UserFailureException
    {

        logTracking(sessionToken, "registerSamples", "NO_OF_SAMPLES(%s) USER(%s)",
                print(newSamplesWithType), userIdOrNull);

    }

    private String print(List<NewSamplesWithTypes> newSamplesWithType)
    {
        StringBuilder sb = new StringBuilder();
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            if (sb.length() != 0)
            {
                sb.append(", ");
            }
            sb.append(samples.getEntityType().getCode());
            sb.append(":").append(samples.getNewEntities().size());
        }
        return sb.toString();
    }

    @Override
    public List<String> generateCodes(String sessionToken, String prefix, EntityKind entityKind,
            int number)
    {
        logAccess(sessionToken, "generateCodes", "PREFIX(%s) ENTITY_KIND(%s) NUMBER(%s)", prefix,
                entityKind, number);
        return null;
    }

    @Override
    public List<Person> listAdministrators(String sessionToken)
    {
        logAccess(sessionToken, "listAdministrators");
        return null;
    }

    @Override
    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail)
    {
        logAccess(sessionToken, "tryPersonWithUserIdOrEmail", "USERID_OR_EMAIL(%s)", useridOrEmail);
        return null;
    }

    @Override
    public Sample registerSampleAndDataSet(String sessionToken, NewSample newSample,
            NewExternalData externalData, String userIdOrNull) throws UserFailureException
    {
        logAccess(sessionToken, "registerSampleAndDataSet",
                "SAMPLE_TYPE(%s) SAMPLE(%S) DATA_SET(%s) USER(%s)", newSample.getSampleType(),
                newSample.getIdentifier(), externalData, userIdOrNull);
        return null;
    }

    @Override
    public Sample updateSampleAndRegisterDataSet(String sessionToken, SampleUpdatesDTO updates,
            NewExternalData externalData)
    {
        logAccess(sessionToken, "updateSampleAndRegisterDataSet", "SAMPLE(%S) DATA_SET(%s)",
                updates.getSampleIdentifier(), externalData);
        return null;
    }

    @Override
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        logAccess(sessionToken, "performEntityOperations", "%s", operationDetails);
        return null;
    }

    @Override
    public Space tryGetSpace(String sessionToken, SpaceIdentifier spaceIdentifier)
    {
        logAccess(sessionToken, "tryGetSpace", "%s", spaceIdentifier);
        return null;
    }

    @Override
    public Project tryGetProject(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        logAccess(sessionToken, "tryGetProject", "%s", projectIdentifier);
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        logAccess(sessionToken, "listExperiments", "%s", projectIdentifier);
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken,
            List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        logAccess(sessionToken, "listExperiments",
                "EXPERIMENT_IDENTIFIERS(%s), EXPERIMENT_FETCH_OPTIONS(%s)", experimentIdentifiers,
                experimentFetchOptions);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsForProjects(String sessionToken,
            List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        logAccess(sessionToken, "listExperimentsForProjects",
                "PROJECT_IDENTIFIERS(%s), EXPERIMENT_FETCH_OPTIONS(%s)", projectIdentifiers,
                experimentFetchOptions);
        return null;
    }

    @Override
    public List<Project> listProjects(String sessionToken)
    {
        logAccess(sessionToken, "listProjects");
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "searchForSamples", "%s", searchCriteria);
        return null;
    }

    @Override
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "searchForExperiments", "%s", searchCriteria);
        return null;
    }

    @Override
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "searchForDataSets", "%s", searchCriteria);
        return null;
    }

    @Override
    public Material tryGetMaterial(String sessionToken, MaterialIdentifier materialIdentifier)
    {
        logAccess(sessionToken, "tryGetMaterial", "%s", materialIdentifier);
        return null;
    }

    @Override
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        logAccess(sessionToken, "listMaterials", "CRITERIA(%s), WITH_PROPERTIES(%s)", criteria,
                withProperties);
        return null;
    }

    @Override
    public Metaproject tryGetMetaproject(String sessionToken, String name, String ownerId)
    {
        logAccess(sessionToken, "tryGetMetaproject", "NAME(%s), OWNER_ID(%s)", name, ownerId);
        return null;
    }

    @Override
    public void removeDataSetsPermanently(String sessionToken, List<String> dataSetCodes,
            String reason)
    {
        logAccess(sessionToken, "removeDataSetsPermanently", "DATA_SET_CODES(%s) REASON(%s)",
                CollectionUtils.abbreviate(dataSetCodes, 5), reason);
    }

    @Override
    public void updateDataSet(String sessionToken, DataSetUpdatesDTO dataSetUpdates)
    {
        logAccess(sessionToken, "updateDataSet", "DATA_SET_UPDATES(%s)", dataSetUpdates);
    }

    @Override
    public List<String> getTrustedCrossOriginDomains(String sessionToken)
    {
        logAccess(sessionToken, "getTrustedCrossOriginDomains");
        return null;
    }

    @Override
    public void setStorageConfirmed(String sessionToken, List<String> dataSetCodes)
    {
        logAccess(sessionToken, "setStorageConfirmed", "DATA_SET_CODE(%s)", dataSetCodes);
    }

    @Override
    public void markSuccessfulPostRegistration(String sessionToken, String dataSetCode)
    {
        logAccess(sessionToken, "markSuccessfulPostRegistration", "DATA_SET_CODE(%s)", dataSetCode);
    }

    @Override
    public void notifyDatasetAccess(String sessionToken, String dataSetCode)
    {
        logAccess(sessionToken, "notifyDatasetAccess", "DATA_SET_CODE(%s)", dataSetCode);
    }

    @Override
    public List<AbstractExternalData> listDataSetsForPostRegistration(String sessionToken,
            String dataStoreCode)
    {
        logAccess(Level.DEBUG, sessionToken, "listDataSetsForPostRegistration", "DATA_STORE(%s)",
                dataStoreCode);
        return null;
    }

    @Override
    public EntityOperationsState didEntityOperationsSucceed(String token, TechId registrationId)
    {
        logAccess(Level.DEBUG, token, "didEntityOperationsSucceed", "REGISTRATION_ID(%s)",
                registrationId);
        return null;
    }

    @Override
    public void heartbeat(String token)
    {
        // do / log nothing
    }

    @Override
    public boolean doesUserHaveRole(String token, String user, String roleCode, String spaceOrNull)
    {
        logAccess(Level.DEBUG, token, "doesUserHaveRole", "USER(%s) ROLE(%s) SPACE(%s)", user,
                roleCode, spaceOrNull);
        return false;
    }

    @Override
    public List<String> filterToVisibleDataSets(String token, String user, List<String> dataSetCodes)
    {
        logAccess(Level.DEBUG, token, "filterToVisibleDataSets", "USER(%s), DATA_SET_CODES(%s)",
                user, dataSetCodes);
        return null;
    }

    @Override
    public List<String> filterToVisibleExperiments(String token, String user,
            List<String> experimentIds)
    {
        logAccess(Level.DEBUG, token, "filterToVisibleExperiments", "USER(%s), EXPERIMENT_IDS(%s)",
                user, experimentIds);
        return null;
    }

    @Override
    public List<String> filterToVisibleSamples(String token, String user, List<String> samplesAll)
    {
        logAccess(Level.DEBUG, token, "filterToVisibleSamples", "USER(%s), SAMPLE_PERMIDS(%s)",
                user, samplesAll);
        return null;
    }

    @Override
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(String token,
            String externalDataManagementSystemCode)
    {
        logAccess(Level.DEBUG, token, "tryGetExternalDataManagementSystem", "CODE(%s)",
                externalDataManagementSystemCode);
        return null;
    }

    @Override
    public Vocabulary tryGetVocabulary(String token, String code)
    {
        logAccess(Level.DEBUG, token, "tryGetVocabulary", "CODE(%s)", code);
        return null;
    }

    @Override
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForType(
            String sessionToken, String code,
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind)
    {
        logAccess(Level.DEBUG, sessionToken, "listPropertyDefinitionsForType",
                "CODE(%s) ENTITY_KIND(%s)", code, entityKind);
        return null;
    }

    @Override
    public List<Metaproject> listMetaprojects(String sessionToken, String userId)
    {
        logAccess(Level.DEBUG, sessionToken, "listMetaprojects", "USER_ID(%s)", userId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaprojectAssignments(String systemSessionToken, String name,
            String userName, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions)
    {
        logAccess(Level.DEBUG, systemSessionToken, "getMetaprojectAssignments",
                "NAME(%s) USER_ID(%s)", name, userName);
        return null;
    }

    @Override
    public List<Metaproject> listMetaprojectsForEntity(String systemSessionToken, String userId,
            IObjectId entityId)
    {
        logAccess(Level.DEBUG, systemSessionToken, "listMetaprojects", "USER_ID(%s) ENTITY_ID(%s)",
                userId, entityId.toString());
        return null;
    }

    @Override
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(String systemSessionToken, String userId,
            Collection<? extends IObjectId> entityIds)
    {
        logAccess(Level.DEBUG, systemSessionToken, "listMetaprojects", "USER_ID(%s) ENTITY_IDS(%s)",
                userId, abbreviate(entityIds));
        return null;
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        logAccess(Level.DEBUG, sessionToken, "listAuthorizationGroups", "");
        return null;
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroupsForUser(String sessionToken, String userId)
    {
        logAccess(Level.DEBUG, sessionToken, "listAuthorizationGroupsForUser", "USER_ID(%s)",
                userId);
        return null;
    }

    @Override
    public List<Person> listUsersForAuthorizationGroup(String sessionToken, TechId authorizationGroupId)
    {
        logAccess(Level.DEBUG, sessionToken, "listUsersForAuthorizationGroup", "AUTHORIZATION_GROUP_ID(%s)",
                authorizationGroupId);
        return null;
    }

    @Override
    public List<RoleAssignment> listRoleAssignments(String sessionToken)
    {
        logAccess(Level.DEBUG, sessionToken, "listRoleAssignments", "");
        return null;
    }

    @Override
    public AttachmentWithContent getAttachment(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId,
            String fileName, Integer versionOrNull)
    {
        logAccess(Level.DEBUG, sessionToken, "getAttachment", "ATTACHMENT_HOLDER_KIND(%s), ATTACHMENT_HOLDER_ID(%s), FILE_NAME(%s), VERSION(%s)",
                attachmentHolderKind,
                attachmentHolderId, fileName, versionOrNull);
        return null;
    }

    @Override
    public List<Attachment> listAttachments(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId)
    {
        logAccess(Level.DEBUG, sessionToken, "listAttachments", "ATTACHMENT_HOLDER_KIND(%s), ATTACHMENT_HOLDER_ID(%s)", attachmentHolderKind,
                attachmentHolderId);
        return null;
    }

    @Override
    public List<AbstractExternalData> listNotArchivedDatasetsWithMetaproject(String sessionToken, final IMetaprojectId metaprojectId)
    {
        logAccess(Level.DEBUG, sessionToken, "listNotArchivedDatasetsWithMetaproject", "TAG(%s)", metaprojectId);
        return null;
    }

    @Override
    public Experiment tryGetExperimentByPermId(String sessionToken, PermId permId) throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "tryGetExperimentByPermId", "PERM_ID(%s)", permId);
        return null;
    }

    @Override
    public Project tryGetProjectByPermId(String sessionToken, PermId permId) throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "tryGetProjectByPermId", "PERM_ID(%s)", permId);
        return null;
    }

    @Override
    public Sample tryGetSampleByPermId(String sessionToken, PermId permId) throws UserFailureException
    {
        logAccess(Level.DEBUG, sessionToken, "tryGetSampleByPermId", "PERM_ID(%s)", permId);
        return null;
    }

}
