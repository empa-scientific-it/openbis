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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
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

/**
 * Service for retrieving general informations.
 * 
 * @author Franz Josef Elmer
 */
public interface IGeneralInformationService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "general-information";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v1";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    /**
     * Tries to authenticate specified user with specified password. Returns session token if succeeded otherwise <code>null</code> is returned. The
     * returned session token can be used for all methods and interfaces of the same openBIS server.
     */
    public String tryToAuthenticateForAllServices(String userID, String userPassword);

    /**
     * Logout the session with the specified session token.
     */
    public void logout(String sessionToken);

    /**
     * Returns true if session with the specified token is still active, false otherwise.
     * 
     * @since 1.4
     */
    public boolean isSessionActive(String sessionToken);

    /**
     * Returns all named role sets. The name is the key of the returned map.
     */
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken);

    /**
     * Returns all spaces of specified database instance enriched with their projects and role assignments.
     * 
     * @param databaseInstanceCodeOrNull Code of an imported database instance or <code>null</code> for the home database instance is meant.
     */
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull);

    /**
     * Return all samples that match the search criteria. This is a short cut for
     * 
     * <pre>
     * searchForSamples(sessionToken, searchCritera, EnumSet.of(SampleFetchOption.PROPERTIES))
     * </pre>
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     * @since 1.1
     */
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Return all samples that match the search criteria.
     * <p>
     * The fetch options set is interpreted by the following rules.
     * <ul>
     * <li>If it does not contain {@link SampleFetchOption#PROPERTIES} only the basic attributes are returned for all samples including possible
     * ancestors and descendants.
     * <li>{@link SampleFetchOption#CHILDREN} will be ignored if {@link SampleFetchOption#DESCENDANTS} is in the set.
     * <li>{@link SampleFetchOption#PARENTS} will be ignored if {@link SampleFetchOption#ANCESTORS} is in the set.
     * <li>It is possible to combine {@link SampleFetchOption#CHILDREN}/ {@link SampleFetchOption#DESCENDANTS} with {@link SampleFetchOption#PARENTS}/
     * {@link SampleFetchOption#ANCESTORS}.
     * </ul>
     * The samples of the returned list also contain appropriated fetch options sets which tells whether one can expect properties, children, or
     * parents. Note, that only the top-level samples can have both children or samples. For descendants and ancestors navigation is possible only in
     * one direction.
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     * @param fetchOptions Options that control which parts of the samples are fetched.
     * @since 1.17
     */
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions);

    /**
     * Return all samples that match the search criteria and that a particular user is allowed to see.
     * <p>
     * The fetch options set is interpreted by the following rules.
     * <ul>
     * <li>If it does not contain {@link SampleFetchOption#PROPERTIES} only the basic attributes are returned for all samples including possible
     * ancestors and descendants.
     * <li>{@link SampleFetchOption#CHILDREN} will be ignored if {@link SampleFetchOption#DESCENDANTS} is in the set.
     * <li>{@link SampleFetchOption#PARENTS} will be ignored if {@link SampleFetchOption#ANCESTORS} is in the set.
     * <li>It is possible to combine {@link SampleFetchOption#CHILDREN}/ {@link SampleFetchOption#DESCENDANTS} with {@link SampleFetchOption#PARENTS}/
     * {@link SampleFetchOption#ANCESTORS}.
     * </ul>
     * The samples of the returned list also contain appropriated fetch options sets which tells whether one can expect properties, children, or
     * parents. Note, that only the top-level samples can have both children or samples. For descendants and ancestors navigation is possible only in
     * one direction.
     * <p>
     * May only be called by users who are <code>INSTANCE_OBSERVER</code>.
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     * @param fetchOptions Options that control which parts of the samples are fetched.
     * @since 1.18
     */
    public List<Sample> searchForSamplesOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, EnumSet<SampleFetchOption> fetchOptions, String userId);

    /**
     * Returns a filtered list of <var>allSamples</var> containing those samples which are visible to <var>userId</var>.
     * 
     * @param allSamples The list of samples that should be filtered.
     * @param userId The user that the samples should be visible to that survive the filtering.
     * @return The filtered list of <var>allSamples</var> containing those samples which are visible to <var>userId</var>.
     * @since 1.18
     */
    public List<Sample> filterSamplesVisibleToUser(String sessionToken, List<Sample> allSamples,
            String userId);

    /**
     * Return all samples that belong to the supplied experiment.
     * 
     * @param experimentIdentifierString The identifier of the experiment samples will be listed for.
     * @since 1.1
     */
    public List<Sample> listSamplesForExperiment(String sessionToken,
            String experimentIdentifierString);

    /**
     * Return all samples that belong to the supplied experiment that are visible to user <var>userId</var>.
     * <p>
     * May only be called by users with capability <code>LIST_PROJECTS_ON_BEHALF_OF_USER</code>.
     * 
     * @param experimentIdentifierString The identifier of the experiment samples will be listed for.
     * @param userId The user to run this query on behalf of.
     * @since 1.18
     */
    public List<Sample> listSamplesForExperimentOnBehalfOfUser(String sessionToken,
            String experimentIdentifierString, String userId);

    /**
     * Return all data sets attached to the given samples.
     * 
     * @param samples The samples for which we return attached data sets.
     * @since 1.1
     */
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples);

    /**
     * Return all experiments of the given type that belong to the supplied projects.
     * 
     * @param projects The projects for which we return attached experiments.
     * @param experimentType The experiment type of the experiments we want to list. Since version 1.9 NULL are accepted. Specifying a NULL experiment
     *            type will result in all experiments for the specified projects being returned.
     * @since 1.2
     */
    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentType);

    /**
     * Return all experiments of the given type that belong to the supplied projects and have registered samles.
     * 
     * @param projects The projects for which we return attached experiments.
     * @param experimentType The experiment type of the experiments we want to list. Specifying a NULL experiment type will result in all experiments
     *            for the specified projects being returned.
     * @since 1.15
     */
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            List<Project> projects, String experimentType);

    /**
     * Return all experiments of the given type that belong to the supplied projects and have registered data sets.
     * 
     * @param projects The projects for which we return attached experiments.
     * @param experimentType The experiment type of the experiments we want to list. Specifying a NULL experiment type will result in all experiments
     *            for the specified projects being returned.
     * @since 1.15
     */
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            List<Project> projects, String experimentType);

    /**
     * Returns a filtered list of <var>allExperiments</var> containing those experiments which are visible to <var>userId</var>.
     * 
     * @param allExperiments The list of experiments that should be filtered.
     * @param userId The user that the experiments should be visible to that survive the filtering.
     * @return The filtered list of <var>allExperiments</var> containing all experiments which are visible to <var>userId</var>.
     * @since 1.18
     */
    public List<Experiment> filterExperimentsVisibleToUser(String sessionToken,
            List<Experiment> allExperiments, String userId);

    /**
     * Return the data sets attached to the specified sample, optionally including child samples. Note, that for returned container data sets the
     * contained data sets have only code, type and registration date set.
     * 
     * @param sample The sample for which we return attached data sets.
     * @param areOnlyDirectlyConnectedIncluded If true, only data sets that are directly connected to the sample are included, otherwise data sets of
     *            child samples are included as well.
     * @since 1.3
     */
    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded);

    /**
     * Lists all DSS server registered this openBIS server instance. Any of the returned instances could be offline at the time of the listing.
     * 
     * @since 1.23
     */
    public List<DataStore> listDataStores(String sessionToken);

    /**
     * Returns the URL for the default data store server for this openBIS AS.
     * 
     * @since 1.4
     */
    public String getDefaultPutDataStoreBaseURL(String sessionToken);

    /**
     * Returns the download URL for the data store of specified data set or null if such data set does not exist.
     * 
     * @since 1.4
     */
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode);

    /**
     * Returns the download URL for the data store of specified data sets.
     * 
     * @return One entry for each data store that has data sets from <var>dataSetCodes</var>, together with the data set codes that are in this data
     *         store.
     * @since 1.19
     */
    public List<DataStoreURLForDataSets> getDataStoreBaseURLs(String sessionToken,
            List<String> dataSetCodes);

    /**
     * Returns all data set types.
     * 
     * @since 1.5
     */
    public List<DataSetType> listDataSetTypes(String sessionToken);

    /**
     * Returns all sample types.
     * 
     * @since 1.25
     */
    public List<SampleType> listSampleTypes(String sessionToken);

    /**
     * Returns all experiment types.
     * 
     * @since 1.25
     */
    public List<ExperimentType> listExperimentTypes(String sessionToken);

    /**
     * Returns map of avaialable vocabulary terms.
     * <p>
     * The method cannot be fully utilized over JSON-RPC, because there is no sensible way to (de)serialize a {@link Vocabulary} object to/from
     * String. Any working implementation will make the life of non-java clients (e.g. Javascript) unnecessarily complicated.
     * 
     * @deprecated Please use {@link #listVocabularies(String)} instead.
     * @since 1.6
     */
    @Deprecated
    public HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> getVocabularyTermsMap(
            String sessionToken);

    /**
     * Returns all available vocabularies together with the contained terms.
     * 
     * @since 1.13
     */
    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary> listVocabularies(
            String sessionToken);

    /**
     * Return all data sets attached to the given samples with connections.
     * 
     * @param samples The samples for which we return attached data sets.
     * @since 1.7
     */
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connectionsToGet);

    /**
     * Return all data sets attached to the given samples with connections that the user <var>userId</var> is allowed to see.
     * <p>
     * May only be called by users with capability <code>LIST_PROJECTS_ON_BEHALF_OF_USER</code>.
     * 
     * @param samples The samples for which we return attached data sets.
     * @param userId The user to run this query on behalf of.
     * @since 1.18
     */
    public List<DataSet> listDataSetsOnBehalfOfUser(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connectionsToGet, String userId);

    /**
     * Return all data sets attached to the given experiments with connections.
     * 
     * @param experiments The experiments for which we return attached data sets.
     * @since 1.14
     */
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connectionsToGet);

    /**
     * Return all data sets attached to the given experiments with connections that the user <var>userId</var> is allowed to see.
     * <p>
     * May only be called by users with capability <code>LIST_PROJECTS_ON_BEHALF_OF_USER</code>.
     * 
     * @param experiments The experiments for which we return attached data sets.
     * @param userId The user to run this query on behalf of.
     * @since 1.18
     */
    public List<DataSet> listDataSetsForExperimentsOnBehalfOfUser(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connectionsToGet, String userId);

    /**
     * Returns meta data for all specified data sets. This contains data set type, properties, and codes of linked parent and children data sets. For
     * container data sets the contained data sets are not returned. Thus, {@link DataSet#getContainedDataSets()} is always empty.
     * 
     * @param dataSetCodes Codes of requested data sets.
     * @return result in the same order as the list of data set codes.
     * @since 1.12
     */
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes);

    /**
     * Returns meta data for all specified data sets. Which parts of the data sets objects are fetched is controlled with the
     * <code>fetchOptions</code> parameter.
     * 
     * @param dataSetCodes Codes of requested data sets.
     * @param fetchOptions Options that control which parts of the data sets are fetched.
     * @return result in the same order as the list of data set codes.
     * @since 1.16
     */
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes,
            EnumSet<DataSetFetchOption> fetchOptions);

    /**
     * Searches on a search domain. If no preferred search domain is specified the first available one will be used. If the preferred sequence search
     * domain doesn't exist or isn't available also the first available one will be used.
     * 
     * @param preferredSearchDomainOrNull The key of the preferred search domain or <code>null</code>.
     * @param searchString The search string.
     * @param optionalParametersOrNull Optional parameters. Can be <code>null</code>. The semantics depends on the type of search domain.
     * @since 1.29
     */
    public List<SearchDomainSearchResult> searchOnSearchDomain(String sessionToken,
            String preferredSearchDomainOrNull, String searchString,
            Map<String, String> optionalParametersOrNull);

    /**
     * Lists available search domains. Their name attribute can be used when invoking {@link #searchOnSearchDomain(String, String, String, Map)}.
     * 
     * @return empty list if no search domain is available.
     * @since 1.29
     */
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken);

    /**
     * Return all data sets matching specified search criteria. Note, that for returned container data sets the contained data sets have only code,
     * type and registration date set.
     * 
     * @param searchCriteria the criteria used for searching.
     * @since 1.8
     */
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Return all data sets matching specified search criteria and visible to user <var>userId</var>. Note, that for returned container data sets the
     * contained data sets have only code, type and registration date set.
     * <p>
     * May only be called by users who are <code>INSTANCE_OBSERVER</code>.
     * 
     * @param searchCriteria the criteria used for searching.
     * @since 1.18
     */
    public List<DataSet> searchForDataSetsOnBehalfOfUser(String sessionToken,
            SearchCriteria searchCriteria, String userId);

    /**
     * Returns a filtered list of <var>allDataSets</var> containing those data sets which are visible to <var>userId</var>.
     * 
     * @param allDataSets The list of data sets that should be filtered.
     * @param userId The user that the data sets should be visible to that survive the filtering.
     * @return The filtered list of <var>allDataSets</var> containing those data sets which are visible to <var>userId</var>.
     * @since 1.18
     */
    public List<DataSet> filterDataSetsVisibleToUser(String sessionToken,
            List<DataSet> allDataSets, String userId);

    /**
     * Return all experiments matching a specified set of identifiers.
     * 
     * @param experimentIdentifiers the identifiers of the experiments to be returned.
     * @since 1.9
     */
    public List<Experiment> listExperiments(String sessionToken, List<String> experimentIdentifiers);

    /**
     * Returns all experiments matching specified search criteria. Note, that sub criterias are not supported.
     * 
     * @since 1.21
     */
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Returns all available projects.
     */
    public List<Project> listProjects(String sessionToken);

    /**
     * Returns all available projects that a particular user is allowed to see.
     * <p>
     * May only be called by users with capability <code>LIST_PROJECTS_ON_BEHALF_OF_USER</code>.
     * 
     * @param userId The user identifier of the user to get the projects for.
     * @since 1.18
     */
    public List<Project> listProjectsOnBehalfOfUser(String sessionToken, String userId);

    /**
     * Returns the materials with specified identifiers (i.e. code and type).
     */
    public List<Material> getMaterialByCodes(String sessionToken,
            List<MaterialIdentifier> materialIdentifier);

    /**
     * Returns all material fulfilling specified search criteria.
     */
    public List<Material> searchForMaterials(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Lists all metaprojects belonging to current user.
     */
    public List<Metaproject> listMetaprojects(String sessionToken);

    /**
     * Lists all metaprojects belonging to specified user.
     * 
     * @since 1.24
     */
    public List<Metaproject> listMetaprojectsOnBehalfOfUser(String sessionToken, String userId);

    /**
     * Returns all entities tagged with given metaproject.
     * 
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     */
    public MetaprojectAssignments getMetaproject(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Returns all entities tagged with given metaproject for specified user.
     * 
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.24
     */
    public MetaprojectAssignments getMetaprojectOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId);

    /**
     * Lists attachments of specified project.
     * 
     * @param allVersions If <code>true</code>, return all versions of the attachments, otherwise return only the latest version.
     * @since 1.23
     */
    public List<Attachment> listAttachmentsForProject(String sessionToken, IProjectId projectId,
            boolean allVersions);

    /**
     * Lists attachments of specified experiment.
     * 
     * @param allVersions If <code>true</code>, return all versions of the attachments, otherwise return only the latest version.
     * @since 1.23
     */
    public List<Attachment> listAttachmentsForExperiment(String sessionToken,
            IExperimentId experimentId, boolean allVersions);

    /**
     * Lists attachments of specified sample.
     * 
     * @param allVersions If <code>true</code>, return all versions of the attachments, otherwise return only the latest version.
     * @since 1.23
     */
    public List<Attachment> listAttachmentsForSample(String sessionToken, ISampleId sampleId,
            boolean allVersions);

    public Map<String, String> getUserDisplaySettings(final String sessionToken);

    /**
     * Lists property types.
     * 
     * @param withRelations If <code>true</code>, return relations.
     * @since 1.26
     */
    public List<PropertyType> listPropertyTypes(String sessionToken, boolean withRelations);

    /**
     * Generate Code.
     * 
     * @param prefix
     * @param entityKind
     * @since 1.28
     */
    public String generateCode(String sessionToken, String prefix, String entityKind);

    /**
     * Returns all deletions.
     * 
     * @param fetchOptions Options that control which parts of the deletions are fetched.
     * @return a sorted list of {@link Deletion}.
     * @since 1.30
     */
    public List<Deletion> listDeletions(String sessionToken, EnumSet<DeletionFetchOption> fetchOptions);

    /**
     * Returns all persons.
     * 
     * @return a list of maps with the user information.
     * @since 1.31
     */
    public List<Person> listPersons(String sessionToken);

    /**
     * Returns number of samples for a sample type code.
     * 
     * @return the number of samples for certain type.
     * @since 1.32
     */
    public Long countNumberOfSamplesForType(String sessionToken, String sampleTypeCode);
}
