/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
class GeneralInformationServiceLogger extends AbstractServerLogger implements
        IGeneralInformationService
{
    public GeneralInformationServiceLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        return null;
    }

    @Override
    public boolean isSessionActive(String sessionToken)
    {
        return false;
    }

    @Override
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        logAccess(sessionToken, "list-role-sets");
        return null;
    }

    @Override
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        logAccess(sessionToken, "list-spaces", "DATABASE_INSTANCE(%s)", databaseInstanceCodeOrNull);
        return null;
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public List<Sample> listSamplesForExperiment(String sessionToken,
            String experimentIdentifierString)
    {
        logAccess(sessionToken, "list-samples-for-experiment", "EXPERIMENT_IDENTIFIER(%s)",
                experimentIdentifierString);
        return null;
    }

    @Override
    public List<Sample> listSamplesForExperimentOnBehalfOfUser(String sessionToken,
            String experimentIdentifierString, String userId)
    {
        logAccess(sessionToken, "list-samples-for-experiment-on-behalf-of-user",
                "EXPERIMENT_IDENTIFIER(%s)", "USER(%s)", experimentIdentifierString, userId);
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA(%s) FETCH_OPTIONS(%s)",
                searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public List<Sample> searchForSamplesOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, EnumSet<SampleFetchOption> fetchOptions, String userId)
    {
        logAccess(sessionToken, "search-for-samples-on-behalf-of-user",
                "SEARCH_CRITERIA(%s) FETCH_OPTIONS(%s) USER(%S)", searchCriteria, fetchOptions,
                userId);
        return null;
    }

    @Override
    public List<Sample> filterSamplesVisibleToUser(String sessionToken, List<Sample> allSamples,
            String userId)
    {
        logAccess(sessionToken, "filter-samples-visible-to-user", "SAMPLES(%s)", "USER(%S)",
                abbreviate(allSamples), userId);
        return null;
    }

    @Override
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLES(%s)", abbreviate(samples));
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentType)
    {
        logAccess(sessionToken, "list-experiments", "PROJECTS(%s)", "EXP_TYPE(%s)",
                abbreviate(projects), experimentType);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            List<Project> projects, String experimentType)
    {
        logAccess(sessionToken, "list-experiments-having-samples", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            List<Project> projects, String experimentType)
    {
        logAccess(sessionToken, "list-experiments-having-data-sets", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    @Override
    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLE(%s) INCLUDE-CONNECTED(%s)", sample,
                areOnlyDirectlyConnectedIncluded);
        return null;
    }

    @Override
    public List<DataStore> listDataStores(String sessionToken)
    {
        logAccess(sessionToken, "list-data-stores");
        return null;
    }

    @Override
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "get-default-put-data-store-url");
        return null;
    }

    @Override
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        logAccess(sessionToken, "get-data-store-base-url", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    @Override
    public List<DataStoreURLForDataSets> getDataStoreBaseURLs(String sessionToken,
            List<String> dataSetCodes)
    {
        logAccess(sessionToken, "get-data-store-base-urls", "DATA_SETS(%s)",
                abbreviate(dataSetCodes));
        return null;
    }

    @Override
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list-data-set-types");
        return null;
    }

    @Override
    public List<SampleType> listSampleTypes(String sessionToken)
    {
        logAccess(sessionToken, "list-sample-types");
        return null;
    }

    @Override
    public List<ExperimentType> listExperimentTypes(String sessionToken)
    {
        logAccess(sessionToken, "list-experiment-types");
        return null;
    }

    @Override
    public HashMap<Vocabulary, List<VocabularyTerm>> getVocabularyTermsMap(String sessionToken)
    {
        logAccess(sessionToken, "get-vocabulary-terms-map");
        return null;
    }

    @Override
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connectionsToGet)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLES(%s) CONNECTIONS(%s)",
                abbreviate(samples), connectionsToGet);
        return null;
    }

    @Override
    public List<DataSet> listDataSetsOnBehalfOfUser(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connectionsToGet, String userId)
    {
        logAccess(sessionToken, "list-data-sets-on-behalf-of-user",
                "SAMPLES(%s) CONNECTIONS(%s) USER(%S)", abbreviate(samples), connectionsToGet,
                userId);
        return null;
    }

    @Override
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes)
    {
        logAccess(sessionToken, "get-data-set-meta-data", "DATA_SETS(%s)", abbreviate(dataSetCodes));
        return null;
    }

    @Override
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "get-data-set-meta-data",
                "DATA_SETS(%s), DATA_SETS_FETCH_OPTIONS(%s)", abbreviate(dataSetCodes),
                fetchOptions);
        return null;
    }

    @Override
    public List<SearchDomainSearchResult> searchOnSearchDomain(String sessionToken, String preferredSearchDomainOrNull,
            String searchString, Map<String, String> optionalParametersOrNull)
    {
        logAccess(sessionToken, "search-on-search-domain", "PREFERRED_SEARCH_DOMAIN(%s) SEARCH_STRING(%s)",
                preferredSearchDomainOrNull, searchString);
        return null;
    }

    @Override
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        logAccess(sessionToken, "list-available-search-domains");
        return null;
    }

    @Override
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-data-sets", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<DataSet> searchForDataSetsOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, String userId)
    {
        logAccess(sessionToken, "search-for-data-sets-on-behalf-of-user",
                "SEARCH_CRITERIA(%s) USER(%s)", searchCriteria, userId);
        return null;
    }

    @Override
    public List<DataSet> filterDataSetsVisibleToUser(String sessionToken,
            List<DataSet> allDataSets, String userId)
    {
        logAccess(sessionToken, "filter-data-sets-visible-to-user", "DATASETS(%s) USER(%s)",
                abbreviate(allDataSets), userId);
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, List<String> experimentIdentifiers)
    {
        logAccess(sessionToken, "list-experiments", "EXPERIMENT_IDENTIFIERS(%s)",
                abbreviate(experimentIdentifiers));
        return null;
    }

    @Override
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-experiments", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<Experiment> filterExperimentsVisibleToUser(String sessionToken,
            List<Experiment> allExperiments, String userId)
    {
        logAccess(sessionToken, "filter-experiments-visible-to-user", "EXPERIMENTS(%s)",
                "USER(%s)", abbreviate(allExperiments), userId);
        return null;
    }

    @Override
    public List<Project> listProjects(String sessionToken)
    {
        logAccess(sessionToken, "list-projects");
        return null;
    }

    @Override
    public List<Project> listProjectsOnBehalfOfUser(String sessionToken, String userId)
    {
        logAccess(sessionToken, "list-projects-on-behalf-of-user", "USER(%s)", userId);
        return null;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary> listVocabularies(
            String sessionToken)
    {
        logAccess(sessionToken, "list-vocabularies");
        return null;
    }

    @Override
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connectionsToGet)
    {
        logAccess(sessionToken, "list-data-sets-for-experiments",
                "EXPERIMENTS(%s) CONNECTIONS(%s)", abbreviate(experiments), connectionsToGet);
        return null;
    }

    @Override
    public List<DataSet> listDataSetsForExperimentsOnBehalfOfUser(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connectionsToGet, String userId)
    {
        logAccess(sessionToken, "list-data-sets-for-experiments-on-behalf-of-user",
                "EXPERIMENTS(%s) CONNECTIONS(%s) USER(%s)", abbreviate(experiments),
                connectionsToGet, userId);
        return null;
    }

    @Override
    public List<Material> getMaterialByCodes(String sessionToken,
            List<MaterialIdentifier> materialIdentifier)
    {
        logAccess(sessionToken, "get-material-by-codes", "MATERIAL_IDENTIFIERS(%s)",
                abbreviate(materialIdentifier));

        return null;
    }

    @Override
    public List<Material> searchForMaterials(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-materials", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<Metaproject> listMetaprojects(String sessionToken)
    {
        logAccess(sessionToken, "listMetaprojects");
        return null;
    }

    @Override
    public List<Metaproject> listMetaprojectsOnBehalfOfUser(String sessionToken, String userId)
    {
        logAccess(sessionToken, "listMetaprojectsOnBehalfOfUser", "USER(%s)", userId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "getMetaproject", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaprojectOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId)
    {
        logAccess(sessionToken, "getMetaprojectOnBehalfOfUser", "METAPROJECT_ID(%s) USER(%s)",
                metaprojectId, userId);
        return null;
    }

    @Override
    public List<Attachment> listAttachmentsForProject(String sessionToken, IProjectId projectId,
            boolean allVersions)
    {
        logAccess(sessionToken, "listAttachmentsForProject", "PROJECT(%s) ALL_VERSIONS(%s)",
                projectId, allVersions);
        return null;
    }

    @Override
    public List<Attachment> listAttachmentsForExperiment(String sessionToken,
            IExperimentId experimentId, boolean allVersions)
    {
        logAccess(sessionToken, "listAttachmentsForExperiment", "EXPERIMENT(%s) ALL_VERSIONS(%s)",
                experimentId, allVersions);
        return null;
    }

    @Override
    public List<Attachment> listAttachmentsForSample(String sessionToken, ISampleId sampleId,
            boolean allVersions)
    {
        logAccess(sessionToken, "listAttachmentsForSample", "SAMPLE(%s) ALL_VERSIONS(%s)",
                sampleId, allVersions);
        return null;
    }

    @Override
    public Map<String, String> getUserDisplaySettings(final String sessionToken)
    {
        logAccess(sessionToken, "getUserDisplaySettings", "sessionToken(%s)", sessionToken);
        return null;
    }

    @Override
    public List<PropertyType> listPropertyTypes(String sessionToken, boolean withRelations)
    {
        logAccess(sessionToken, "listPropertyTypes", "sessionToken(%s) withRelations(%s)", sessionToken, withRelations);
        return null;
    }

    @Override
    public String generateCode(String sessionToken, String prefix, String entityKind)
    {
        logAccess(sessionToken, "generateCode", "sessionToken(%s) prefix(%s) entityKind(%s)", sessionToken, prefix, entityKind);
        return null;
    }

    @Override
    public List<Deletion> listDeletions(String sessionToken, EnumSet<DeletionFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "listDeletions", "fetchOptions(%s)", fetchOptions);
        return null;
    }

    @Override
    public List<Person> listPersons(String sessionToken)
    {
        logAccess(sessionToken, "listPersons");
        return null;
    }

    @Override
    public Long countNumberOfSamplesForType(String sessionToken, String sampleTypeCode)
    {
        logAccess(sessionToken, "countNumberOfSamplesForType", "sampleTypeCode(%s)", sampleTypeCode);
        return null;
    }

}
