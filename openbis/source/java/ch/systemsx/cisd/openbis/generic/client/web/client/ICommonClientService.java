/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.shared.DataType;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.shared.Person;
import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Service interface for the generic GWT client.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonClientService extends IClientService
{
    /**
     * Returns a list of all groups which belong to the specified database instance.
     */
    public List<Group> listGroups(String databaseInstanceCode) throws UserFailureException;

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
            throws UserFailureException;

    /**
     * Returns a list of all persons which belong to the current database instance.
     */
    public List<Person> listPersons() throws UserFailureException;

    /**
     * Registers a new person with specified code.
     */
    public void registerPerson(String code) throws UserFailureException;

    /**
     * Returns a list of all roles.
     */
    public List<RoleAssignment> listRoles() throws UserFailureException;

    /**
     * Registers a new role from given role set code, group code and person code
     */
    public void registerGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, group code and person code
     */
    public void deleteGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code and person code
     */
    public void registerInstanceRole(String roleSetCode, String person) throws UserFailureException;

    /**
     * Deletes the role described by given role set code and person code
     */
    public void deleteInstanceRole(String roleSetCode, String person) throws UserFailureException;

    /**
     * Returns a list of sample types.
     */
    public List<SampleType> listSampleTypes() throws UserFailureException;

    /**
     * Returns a list of samples for given sample type.
     */
    public ResultSet<Sample> listSamples(final ListSampleCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a key which can be used be the export servlet (and eventually
     * {@link #getExportTable(String)}) to reference the export criteria in an easy way.
     */
    public String prepareExportSamples(final TableExportCriteria<Sample> criteria)
            throws UserFailureException;

    /**
     * Returns a list of experiments.
     */
    public ResultSet<Experiment> listExperiments(final ListExperimentsCriteria criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for experiments.
     */

    public String prepareExportExperiments(final TableExportCriteria<Experiment> criteria)
            throws UserFailureException;

    /**
     * Lists the entities matching the search.
     */
    public ResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final IResultSetConfig<String, MatchingEntity> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for matching entites.
     */
    public String prepareExportMatchingEntities(final TableExportCriteria<MatchingEntity> criteria)
            throws UserFailureException;

    /**
     * Returns a chunk of the property types list.
     */
    public ResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, PropertyType> criteria) throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types.
     */
    public String prepareExportPropertyTypes(final TableExportCriteria<PropertyType> criteria)
            throws UserFailureException;

    /**
     * Returns a chunk of the property types assignment list.
     */
    public ResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria);

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types assignments.
     */
    public String prepareExportPropertyTypeAssignments(
            final TableExportCriteria<EntityTypePropertyType<?>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all projects.
     */
    public ResultSet<Project> listProjects(DefaultResultSetConfig<String, Project> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for projects.
     */
    public String prepareExportProjects(final TableExportCriteria<Project> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all vocabularies.
     * <p>
     * Note that the vocabulary terms are included/loaded.
     * </p>
     */
    public ResultSet<Vocabulary> listVocabularies(boolean withTerms, boolean excludeInternal,
            DefaultResultSetConfig<String, Vocabulary> criteria) throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for Vocabularies.
     */
    public String prepareExportVocabularies(final TableExportCriteria<Vocabulary> criteria)
            throws UserFailureException;

    /**
     * Assumes that preparation of the export ({@link #prepareExportSamples(TableExportCriteria)}
     * or {@link #prepareExportExperiments(TableExportCriteria)} has been invoked before and
     * returned with an exportDataKey passed here as a parameter.
     */
    public String getExportTable(String exportDataKey) throws UserFailureException;

    /**
     * Removes the session result set associated with given key.
     */
    public void removeResultSet(final String resultSetKey) throws UserFailureException;

    /**
     * For given <var>sampleIdentifier</var> returns corresponding list of {@link ExternalData}.
     */
    public List<ExternalData> listExternalData(final String sampleIdentifier)
            throws UserFailureException;

    public List<ExternalData> listExternalDataForExperiment(String experimentIdentifier)
            throws UserFailureException;

    /**
     * Lists the searchable entities.
     */
    public List<SearchableEntity> listSearchableEntities() throws UserFailureException;

    /**
     * Returns a list of all experiment types.
     */
    public List<ExperimentType> listExperimentTypes() throws UserFailureException;

    /**
     * Returns a list of all data types.
     */
    public List<DataType> listDataTypes() throws UserFailureException;

    /**
     * Assigns property type to entity type.
     */
    public String assignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, boolean isMandatory, String defaultValue)
            throws UserFailureException;

    /**
     * Registers given {@link PropertyType}.
     */
    public void registerPropertyType(final PropertyType propertyType) throws UserFailureException;

    /**
     * Registers given {@link Vocabulary}.
     */
    public void registerVocabulary(final Vocabulary vocabulary) throws UserFailureException;

    /**
     * Registers given {@link Project}.
     */
    public void registerProject(final Project project) throws UserFailureException;

}
