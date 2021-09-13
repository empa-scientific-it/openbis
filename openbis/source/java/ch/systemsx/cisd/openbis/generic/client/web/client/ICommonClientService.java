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
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ArchivingResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedCriteriaOrSelectedEntityHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IUpdateResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMetaprojectsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleChildrenInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityValidationEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;

/**
 * Service interface for the generic GWT client.
 * <p>
 * Each method should declare throwing {@link UserFailureException}. The authorization framework can throw it when the user has insufficient
 * privileges. If it is not marked, the GWT client will report unexpected exception.
 * </p>
 *
 * @author Franz-Josef Elmer
 */
public interface ICommonClientService extends IClientService
{
    /**
     * Keeps the logged in user session alive.
     *
     * @return <code>null</code> if session was successfully prolonged, otherwise the reason for failure.
     */
    public String keepSessionAlive() throws UserFailureException;

    /**
     * Returns a list of all spaces.
     */
    public TypedTableResultSet<Space> listSpaces(
            DefaultResultSetConfig<String, TableModelRowWithObject<Space>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all scripts.
     */
    public TypedTableResultSet<Script> listScripts(ListScriptsCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of all metaprojects.
     */
    public List<Metaproject> listMetaprojects() throws UserFailureException;

    /**
     * Returns a list of all metaprojects.
     */
    public TypedTableResultSet<Metaproject> listMetaprojects(ListMetaprojectsCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of all metaproject assignments counts.
     */
    public List<MetaprojectAssignmentsCount> listMetaprojectAssignmentsCounts()
            throws UserFailureException;

    /**
     * Returns metaproject assignments count.
     */
    public MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(Long metaprojectId)
            throws UserFailureException;

    /**
     * Assigns samples to a metaproject.
     */
    public void assignEntitiesToMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> sampleIds);

    /**
     * removes samples from a metaproject.
     */
    public void removeEntitiesFromMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> sampleIds);

    /**
     * Returns metaproject.
     */
    public Metaproject getMetaproject(Long metaprojectId) throws UserFailureException;

    /**
     * Returns metaproject.
     */
    public Metaproject getMetaproject(String metaprojectIdentifier) throws UserFailureException;

    /**
     * Updates a metaproject.
     */
    public Metaproject updateMetaproject(Long metaprojectId, IMetaprojectUpdates updates)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for scripts.
     */
    public String prepareExportScripts(
            final TableExportCriteria<TableModelRowWithObject<Script>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for metaprojects.
     */
    public String prepareExportMetaprojects(
            final TableExportCriteria<TableModelRowWithObject<Metaproject>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for spaces.
     */
    public String prepareExportSpaces(
            final TableExportCriteria<TableModelRowWithObject<Space>> criteria)
            throws UserFailureException;

    /**
     * Registers a new space with specified code and optional description.
     */
    public void registerSpace(String spaceCode, String descriptionOrNull)
            throws UserFailureException;

    /**
     * Updates script.
     */
    public ScriptUpdateResult updateScript(final IScriptUpdates updates) throws UserFailureException;

    /**
     * Updates space.
     */
    public void updateSpace(final ISpaceUpdates updates) throws UserFailureException;

    /**
     * Returns a list of all persons which belong to the current database instance.
     */
    public TypedTableResultSet<Person> listPersons(ListPersonsCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of persons registered in given database instance.
     */
    public List<Person> listPersons() throws UserFailureException;

    /**
     * Returns a list of active persons registered in given database instance.
     */
    public List<Person> listActivePersons() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for persons.
     */
    public String prepareExportPersons(
            final TableExportCriteria<TableModelRowWithObject<Person>> criteria)
            throws UserFailureException;

    /**
     * Registers a new person with specified code.
     */
    public void registerPerson(String code) throws UserFailureException;

    /**
     * Returns a list of all role assignments.
     */
    public TypedTableResultSet<RoleAssignment> listRoleAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<RoleAssignment>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for role assignments.
     */
    public String prepareExportRoleAssignments(
            final TableExportCriteria<TableModelRowWithObject<RoleAssignment>> criteria)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code, project identifier and grantee.
     */
    public void registerProjectRole(RoleWithHierarchy role, String projectIdentifier, Grantee grantee)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, project identifier and grantee.
     */
    public void deleteProjectRole(RoleWithHierarchy role, String projectIdentifier, Grantee grantee)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code, space code and grantee.
     */
    public void registerSpaceRole(RoleWithHierarchy role, String spaceCode, Grantee grantee)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, space code and grantee.
     */
    public void deleteSpaceRole(RoleWithHierarchy role, String spaceCode, Grantee grantee)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code and grantee.
     */
    public void registerInstanceRole(RoleWithHierarchy role, Grantee grantee)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code and grantee.
     */
    public void deleteInstanceRole(RoleWithHierarchy role, Grantee grantee)
            throws UserFailureException;

    /**
     * Returns a list of sample types.
     */
    public List<SampleType> listSampleTypes() throws UserFailureException;

    public Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            EntityKind entityKind, String entityTypeCode) throws UserFailureException;

    /**
     * Returns a list of samples matching given criteria.
     */
    public ResultSetWithEntityTypes<Sample> listSamples(final ListSampleDisplayCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of samples matching given criteria.
     */
    public TypedTableResultSet<Sample> listSamples2(final ListSampleDisplayCriteria2 criteria)
            throws UserFailureException;

    /**
     * Returns a list of metaproject samples.
     */
    public List<Sample> listMetaprojectSamples(final Long metaprojectId)
            throws UserFailureException;

    /**
     * Returns a key which can be used be the export servlet (and eventually {@link #getExportTable(String, String)}) to reference the export criteria
     * in an easy way.
     */
    public String prepareExportSamples(
            final TableExportCriteria<TableModelRowWithObject<Sample>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of experiments.
     */
    public TypedTableResultSet<Experiment> listExperiments(final ListExperimentsCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of metaproject experiments.
     */
    public List<Experiment> listMetaprojectExperiments(final Long metaprojectId)
            throws UserFailureException;

    /**
     * Returns a list of materials.
     */
    public TypedTableResultSet<Material> listMaterials(final ListMaterialDisplayCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of metaproject materials.
     */
    public List<Material> listMetaprojectMaterials(final Long metaprojectId)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for experiments.
     */

    public String prepareExportExperiments(
            final TableExportCriteria<TableModelRowWithObject<Experiment>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for data set search hits.
     */
    public String prepareExportDataSetSearchHits(
            TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> exportCriteria)
            throws UserFailureException;

    /**
     * Lists the entities matching the search.
     */
    public TypedTableResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final boolean useWildcardSearchMode,
            final IResultSetConfig<String, TableModelRowWithObject<MatchingEntity>> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for matching entites.
     */
    public String prepareExportMatchingEntities(
            final TableExportCriteria<TableModelRowWithObject<MatchingEntity>> criteria)
            throws UserFailureException;

    /**
     * Returns all property types.
     */
    public List<PropertyType> listPropertyTypes(boolean withRelations) throws UserFailureException;

    /**
     * Returns a chunk of the property types list.
     */
    public TypedTableResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types.
     */
    public String prepareExportPropertyTypes(
            final TableExportCriteria<TableModelRowWithObject<PropertyType>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<EntityHistory> listEntityHistory(ListEntityHistoryCriteria criteria);

    public String prepareExportEntityHistory(
            TableExportCriteria<TableModelRowWithObject<EntityHistory>> criteria);

    /**
     * Returns a chunk of the property types assignment list.
     */
    public TypedTableResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignmentsFromBrowser(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity,
            List<NewPTNewAssigment> propertyTypesAsgs)
            throws UserFailureException;

    /**
     * Returns a chunk of the property types assignment list.
     */
    public TypedTableResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity)
            throws UserFailureException;

    /**
     * Returns a property types assignment list for the given entity type.
     */
    public List<EntityTypePropertyType<?>> listPropertyTypeAssignments(EntityType entityType)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types assignments.
     */
    public String prepareExportPropertyTypeAssignments(
            final TableExportCriteria<TableModelRowWithObject<EntityTypePropertyType<?>>> criteria)
            throws UserFailureException;

    /**
     * Returns the number of entities of specified kind and type which have a property of specified type.
     */
    public int countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode) throws UserFailureException;

    /**
     * Returns a list of all projects.
     */
    public TypedTableResultSet<Project> listProjects(
            DefaultResultSetConfig<String, TableModelRowWithObject<Project>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all projects for use in the tree view of projects.
     */
    public List<Project> listProjectsForTree() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for projects.
     */
    public String prepareExportProjects(
            final TableExportCriteria<TableModelRowWithObject<Project>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for deletions.
     */
    public String prepareExportDeletions(
            final TableExportCriteria<TableModelRowWithObject<Deletion>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all vocabularies.
     * <p>
     * Note that the vocabulary terms are included/loaded.
     * </p>
     */
    public TypedTableResultSet<Vocabulary> listVocabularies(boolean withTerms,
            boolean excludeInternal,
            DefaultResultSetConfig<String, TableModelRowWithObject<Vocabulary>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for Vocabularies.
     */
    public String prepareExportVocabularies(
            final TableExportCriteria<TableModelRowWithObject<Vocabulary>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all vocabulary terms for a specified vocabulary.
     */
    public TypedTableResultSet<VocabularyTermWithStats> listVocabularyTerms(
            Vocabulary vocabulary,
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for Vocabulary Terms.
     */
    public String prepareExportVocabularyTerms(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<MaterialType> listMaterialTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for MaterialType.
     */
    public String prepareExportMaterialTypes(
            final TableExportCriteria<TableModelRowWithObject<MaterialType>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<SampleType> listSampleTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<SampleType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for SampleType.
     */
    public String prepareExportSampleTypes(
            final TableExportCriteria<TableModelRowWithObject<SampleType>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<ExperimentType> listExperimentTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<ExperimentType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for ExperimentType.
     */
    public String prepareExportExperimentTypes(
            final TableExportCriteria<TableModelRowWithObject<ExperimentType>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<DataSetType> listDataSetTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<DataSetType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for DataSetType.
     */
    public String prepareExportDataSetTypes(
            final TableExportCriteria<TableModelRowWithObject<DataSetType>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<FileFormatType> listFileTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria)
            throws UserFailureException;

    public List<FileFormatType> listFileTypes() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for FileType.
     */
    public String prepareExportFileTypes(TableExportCriteria<FileFormatType> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for AttachmentVersions.
     */
    public String prepareExportAttachmentVersions(
            TableExportCriteria<TableModelRowWithObject<AttachmentVersions>> criteria)
            throws UserFailureException;

    /**
     * Assumes that preparation of the export ( {@link #prepareExportSamples(TableExportCriteria)} or
     * {@link #prepareExportExperiments(TableExportCriteria)} has been invoked before and returned with an exportDataKey passed here as a parameter.
     */
    public String getExportTable(String exportDataKey, String lineSeparator)
            throws UserFailureException;

    /**
     * Removes the session result set associated with given key.
     */
    public void removeResultSet(final String resultSetKey) throws UserFailureException;

    /**
     * For given <var>sampleId</var> returns corresponding list of {@link AbstractExternalData}.
     */
    public TypedTableResultSet<AbstractExternalData> listSampleDataSets(final TechId sampleId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            final boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * For given the given sample returns contained data sets.
     */
    public List<String> listSampleDataSets(TechId sampleId, boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * For given the given samples, returns information about derived sampled and contained data sets.
     */
    public List<SampleChildrenInfo> getSampleChildrenInfo(List<TechId> sampleIds, boolean showOnlyDirectlyConnected);

    /**
     * For given <var>experimentId</var> returns corresponding list of {@link AbstractExternalData}.
     */
    public TypedTableResultSet<AbstractExternalData> listExperimentDataSets(
            final TechId experimentId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            boolean onlyDirectlyConnected) throws UserFailureException;

    /**
     * For given <var>metaprojectId</var> returns corresponding list of {@link AbstractExternalData} .
     */
    public TypedTableResultSet<AbstractExternalData> listMetaprojectDataSets(
            final TechId metaprojectId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria)
            throws UserFailureException;

    /**
     * For given <var>metaprojectId</var> returns corresponding list of {@link AbstractExternalData} .
     */
    public List<AbstractExternalData> listMetaprojectDataSets(final Long metaprojectId)
            throws UserFailureException;

    /**
     * For given <var>datasetId</var> in given relationship <var>role</var> returns corresponding list of {@link AbstractExternalData}.
     */
    public TypedTableResultSet<AbstractExternalData> listDataSetRelationships(TechId datasetId,
            DataSetRelationshipRole role,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria)
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
     * Creates Entity Type and Assigns Property types creating them if they don't exist
     */
    public String registerEntitytypeAndAssignPropertyTypes(NewETNewPTAssigments newETNewPTAssigments) throws UserFailureException;

    /**
     * Updates Entity Type and Updates Property types creating them if they don't exist
     */
    public String updateEntitytypeAndPropertyTypes(NewETNewPTAssigments newETNewPTAssigments) throws UserFailureException;

    /**
     * Creates and assigns a property type.
     */
    public String registerAndAssignPropertyType(PropertyType propertyType, NewETPTAssignment assignment) throws UserFailureException;

    /**
     * Assigns property type to entity type.
     */
    public String assignPropertyType(NewETPTAssignment assignment) throws UserFailureException;

    /**
     * Unassigns property type to entity type.
     */
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode) throws UserFailureException;

    /**
     * Updates specified property type assignment.
     */
    public void updatePropertyTypeAssignment(NewETPTAssignment assignmentUpdates)
            throws UserFailureException;

    /**
     * Registers given {@link PropertyType}.
     */
    public void registerPropertyType(final PropertyType propertyType) throws UserFailureException;

    /**
     * Updates property type.
     */
    public void updatePropertyType(final IPropertyTypeUpdates updates) throws UserFailureException;

    /**
     * Registers given {@link NewVocabulary}.
     */
    public void registerVocabulary(final String termsSessionKey, final NewVocabulary vocabulary)
            throws UserFailureException;

    /**
     * Updates vocabulary.
     */
    public void updateVocabulary(final IVocabularyUpdates updates) throws UserFailureException;

    /**
     * Updates vocabulary terms.
     */
    public void updateVocabularyTerms(final String termsSessionKey, TechId vocabularyId)
            throws UserFailureException;

    /**
     * Adds specified terms to the specified vocabulary after specified ordinal (first shift all terms with bigger ordinal).
     */
    public void addVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> vocabularyTerms,
            Long previousTermOrdinal) throws UserFailureException;

    /**
     * Adds specified unofficial terms to the specified vocabulary after specified ordinal (first shift all terms with bigger ordinal).
     */
    public void addUnofficialVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal) throws UserFailureException;

    /**
     * Updates vocabulary term.
     */
    public void updateVocabularyTerm(final IVocabularyTermUpdates updates)
            throws UserFailureException;

    /**
     * Deletes the specified terms of the specified vocabulary. Terms in use will be replaced.
     */
    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced) throws UserFailureException;

    /**
     * Makes given terms official
     */
    public void makeVocabularyTermsOfficial(TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial) throws UserFailureException;

    /** Lists terms of a specified vocabulary */
    public List<VocabularyTerm> listVocabularyTerms(Vocabulary vocabulary)
            throws UserFailureException;

    /**
     * Registers given {@link Project}.
     */
    public void registerProject(String sessionKey, final Project project)
            throws UserFailureException;

    /**
     * Returns {@link AbstractExternalData} fulfilling given {@link DetailedSearchCriteria}.
     */
    public TypedTableResultSet<AbstractExternalData> searchForDataSets(
            DetailedSearchCriteria criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig)
            throws UserFailureException;

    /**
     * Returns {@link AbstractExternalData} fulfilling given {@link RelatedDataSetCriteria}.
     */
    public TypedTableResultSet<AbstractExternalData> searchForDataSets(
            RelatedDataSetCriteria<? extends IEntityInformationHolder> criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig)
            throws UserFailureException;

    /**
     * Returns a list of all material types.
     */
    public List<MaterialType> listMaterialTypes() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for materials.
     */
    public String prepareExportMaterials(
            final TableExportCriteria<TableModelRowWithObject<Material>> criteria)
            throws UserFailureException;

    /** Registers a new material type */
    public void registerMaterialType(MaterialType entityType) throws UserFailureException;

    /** Registers a new data set type */
    public void registerDataSetType(DataSetType entityType) throws UserFailureException;

    /** Registers a new file type */
    public void registerFileType(FileFormatType type) throws UserFailureException;

    /** Registers a new sample type */
    public void registerSampleType(SampleType entityType) throws UserFailureException;

    /** Registers a new experiment type */
    public void registerExperimentType(ExperimentType entityType) throws UserFailureException;

    /**
     * Updates specified entity type of specified kind.
     */
    public void updateEntityType(EntityKind entityKind, EntityType entityType)
            throws UserFailureException;

    /**
     * Updates project.
     */
    public int updateProject(ProjectUpdates updates) throws UserFailureException;

    /** Deletes/Trashes the specified data sets. */
    public void deleteDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, String reason,
            DeletionType deletionType, boolean forceDisallowedTypes) throws UserFailureException;

    /** Deletes/Trashes the specified data set. */
    public void deleteDataSet(String singleData, String reason, DeletionType deletionType,
            boolean forceDisallowedTypes) throws UserFailureException;

    /**
     * Deletes/Trashes the specified samples. NOTE: this is a stale version used only for samples with abundance.
     */
    public void deleteSamples(List<TechId> sampleIds, String reason, DeletionType deletionType)
            throws UserFailureException;

    /** Deletes/Trashes the specified samples. */
    public void deleteSamples(DisplayedOrSelectedIdHolderCriteria<? extends IIdHolder> criteria,
            String reason, DeletionType deletionType) throws UserFailureException;

    /** Deletes/Trashes the specified sample. */
    public void deleteSample(TechId sampleId, String reason, DeletionType deletionType)
            throws UserFailureException;

    /** Deletes/Trashes the specified experiments. */
    public void deleteExperiments(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Experiment>> criteria,
            String reason, DeletionType deletionType) throws UserFailureException;

    /** Deletes/Trashes the specified experiment. */
    public void deleteExperiment(TechId experimentId, String reason, DeletionType deletionType)
            throws UserFailureException;

    /** Deletes the specified projects. */
    public void deleteProjects(List<TechId> projectIds, String reason) throws UserFailureException;

    /** Deletes the specified metaprojects. */
    public void deleteMetaprojects(List<TechId> metaprojectIds, String reason)
            throws UserFailureException;

    /** Deletes the specified spaces. */
    public void deleteSpaces(List<TechId> spaceIds, String reason) throws UserFailureException;

    /** Deletes the specified scripts. */
    public void deleteScripts(List<TechId> scriptIds) throws UserFailureException;

    /** Deletes the specified vocabularies. */
    public void deleteVocabularies(List<TechId> vocabualryIds, String reason)
            throws UserFailureException;

    /** Deletes the specified property types. */
    public void deletePropertyTypes(List<TechId> propertyTypeIds, String reason)
            throws UserFailureException;

    /**
     * Deletes specified attachments (all versions with given file names) of specified attachment holder.
     */
    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason) throws UserFailureException;

    /**
     * Returns a list of all attachments which belong to the specified holder grouped in {@link AttachmentVersions}.
     */
    public TypedTableResultSet<AttachmentVersions> listAttachmentVersions(TechId holderId,
            AttachmentHolderKind holderKind,
            DefaultResultSetConfig<String, TableModelRowWithObject<AttachmentVersions>> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all available data set types.
     */
    public List<DataSetType> listDataSetTypes() throws UserFailureException;

    /**
     * Uploads the specified data sets to the specified CIFEX server using the specified parameters.
     *
     * @return a message or an empty string.
     */
    public String uploadDataSets(DisplayedOrSelectedDatasetCriteria criteria,
            DataSetUploadParameters uploadParameters) throws UserFailureException;

    /**
     * Information about the time and kind of the last modification, separately for each kind of database object.
     */
    public LastModificationState getLastModificationState() throws UserFailureException;

    /**
     * For given <var>experimentIdentifier</var> returns corresponding {@link Experiment}.
     */
    public Experiment getExperimentInfo(final String experimentIdentifier)
            throws UserFailureException;

    /**
     * For given <var>experiment perm id</var> returns corresponding {@link Experiment}.
     */
    public Experiment getExperimentInfoByPermId(final String experimentPermId)
            throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Experiment}.
     */
    public Experiment getExperimentInfo(final TechId experimentId) throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Project}.
     */
    public Project getProjectInfo(final TechId projectId) throws UserFailureException;

    /**
     * For given {@link BasicProjectIdentifier} returns corresponding {@link Project}.
     */
    public Project getProjectInfo(final BasicProjectIdentifier projectIdentifier)
            throws UserFailureException;

    /**
     * For given <var>project perm id</var> returns corresponding {@link Project}.
     */
    public Project getProjectInfoByPermId(final String projectPermId) throws UserFailureException;

    /**
     * Generates unique code.
     */
    public String generateCode(final String prefix, EntityKind entityKind)
            throws UserFailureException;

    /**
     * Delete entity types.
     */
    public void deleteEntityTypes(final EntityKind entityKind, final List<String> codes)
            throws UserFailureException;

    /**
     * Delete file format types.
     */
    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes) throws UserFailureException;

    /**
     * For given {@link EntityKind} and <var>permId</var> returns the corresponding {@link IEntityInformationHolderWithPermId}.
     */
    public IEntityInformationHolderWithPermId getEntityInformationHolder(EntityKind entityKind,
            String permId) throws UserFailureException;

    /**
     * For given {@link BasicEntityDescription} returns the corresponding {@link IEntityInformationHolderWithPermId}.
     */
    public IEntityInformationHolderWithPermId getEntityInformationHolder(BasicEntityDescription info)
            throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    public Material getMaterialInfo(MaterialIdentifier identifier) throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    public Material getMaterialInfo(TechId techId) throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link IEntityInformationHolderWithPermId}.
     */
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(
            MaterialIdentifier identifier) throws UserFailureException;

    /**
     * Returns example file format for batch operation on entities.
     */
    public String getTemplate(EntityKind kind, String type, final String format, boolean autoGenerate,
            boolean withExperiments, boolean withSpace, BatchOperationKind operationKind)
            throws UserFailureException;

    /**
     * Updates the file format.
     */
    public void updateFileFormatType(AbstractType type) throws UserFailureException;

    /**
     * Updates the attachment.
     */
    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment) throws UserFailureException;

    /**
     * Add attachment.
     */
    public void addAttachment(TechId holderId, String sessionKey, AttachmentHolderKind holderKind,
            NewAttachment attachment) throws UserFailureException;

    /** Updates managed property of specified entity. */
    public void updateManagedProperty(TechId entityId, EntityKind entityKind,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
            throws UserFailureException;

    /**
     * For given {@link DataStoreServiceKind} returns a list of all corresponding {@link DatastoreServiceDescription}s.
     */
    public List<DatastoreServiceDescription> listDataStoreServices(
            DataStoreServiceKind pluginTaskKind) throws UserFailureException;

    /**
     * Uses the specified datastore service to generate reports from the specified datasets.
     */
    public TableModelReference createReportFromDatasets(
            DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
            throws UserFailureException;

    /**
     * Uses the specified table model to generate a table/report.
     */
    public TableModelReference createReportFromTableModel(TableModel tableModel)
            throws UserFailureException;

    /**
     * Execute an aggregation service to generate a report.
     */
    TableModelReference createReportFromAggregationService(
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters);

    /**
     * Returns a list of report rows.
     */
    public TypedTableResultSet<ReportRowModel> listReport(
            IResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for TableModelRow.
     */
    public String prepareExportReport(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> exportCriteria)
            throws UserFailureException;

    /**
     * Uses the specified datastore service to schedule processing of the specified datasets.
     */
    public void processDatasets(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria) throws UserFailureException;

    /**
     * Schedules archiving of the specified datasets.
     */
    public ArchivingResult archiveDatasets(DisplayedOrSelectedDatasetCriteria criteria, boolean removeFromDataStore)
            throws UserFailureException;

    /**
     * Schedules unarchiving of the specified datasets.
     */
    public ArchivingResult unarchiveDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws UserFailureException;

    /**
     * Schedules archiving of the datasets connected to the specified experiments.
     */
    public ArchivingResult archiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria, boolean removeFromDataStore)
            throws UserFailureException;

    /**
     * Schedules unarchiving of the datasets connected to the specified experiments.
     */
    public ArchivingResult unarchiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
            throws UserFailureException;

    /**
     * Deletes selected authorization groups.
     */
    public void deleteAuthorizationGroups(List<TechId> createList, String reason)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for AuthorizationGroups.
     */
    public String prepareExportAuthorizationGroups(
            TableExportCriteria<TableModelRowWithObject<AuthorizationGroup>> exportCriteria)
            throws UserFailureException;

    /**
     * Returns {@link AuthorizationGroup}s for given criteria.
     */
    public TypedTableResultSet<AuthorizationGroup> listAuthorizationGroups(
            DefaultResultSetConfig<String, TableModelRowWithObject<AuthorizationGroup>> resultSetConfig)
            throws UserFailureException;

    /**
     * Returns a list of authorization groups registered in given database instance.
     */
    public List<AuthorizationGroup> listAuthorizationGroups() throws UserFailureException;

    /**
     * Creates a new authorization group.
     */
    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthorizationGroup)
            throws UserFailureException;

    /**
     * Creates a new script.
     */
    public void registerScript(Script newScript) throws UserFailureException;

    /**
     * Returns a list persons belonging to given authorization group.
     */
    public List<Person> listPersonsInAuthorizationGroup(TechId group) throws UserFailureException;

    /**
     * Updates given authorization group.
     */
    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates)
            throws UserFailureException;

    /**
     * Adds persons with specified codes to the authorization group with given tech id.
     */
    public void addPersonsToAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes) throws UserFailureException;

    /**
     * Removes persons with specified codes from the authorization group with given tech id.
     */
    public void removePersonsFromAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes) throws UserFailureException;

    // -- custom grid filters

    /**
     * Lists custom grid filters available for given grid.
     */
    public List<GridCustomFilter> listFilters(String gridId) throws UserFailureException;

    /**
     * Returns {@link GridCustomFilter}s for given grid and display criteria.
     */
    public TypedTableResultSet<GridCustomFilter> listFilters(
            String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for custom grid filters.
     */
    public String prepareExportFilters(
            final TableExportCriteria<TableModelRowWithObject<GridCustomFilter>> criteria)
            throws UserFailureException;

    /**
     * Registers a new grid custom filter.
     */
    public void registerFilter(NewColumnOrFilter filter) throws UserFailureException;

    /** Deletes the specified grid custom filters. */
    public void deleteFilters(List<TechId> filterIds) throws UserFailureException;

    /**
     * Updates grid custom filter.
     */
    public void updateFilter(final IExpressionUpdates updates) throws UserFailureException;

    // -- custom grid columns

    /**
     * Lists custom grid columns available for given grid.
     */
    public List<GridCustomColumn> listGridCustomColumns(String gridId) throws UserFailureException;

    /**
     * Returns {@link GridCustomColumn}s for given grid and display criteria.
     */
    public TypedTableResultSet<GridCustomColumn> listGridCustomColumns(
            String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomColumn>> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for custom grid filters.
     */
    public String prepareExportColumns(
            final TableExportCriteria<TableModelRowWithObject<GridCustomColumn>> criteria)
            throws UserFailureException;

    /**
     * Registers a new grid custom column.
     */
    public void registerColumn(NewColumnOrFilter newColumn) throws UserFailureException;

    /** Deletes the specified grid custom columns. */
    public void deleteColumns(List<TechId> columnIds) throws UserFailureException;

    /**
     * Updates grid custom column.
     */
    public void updateColumn(IExpressionUpdates updates) throws UserFailureException;

    /** Deletes the specified materials. */
    public void deleteMaterials(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Material>> criteria,
            String reason) throws UserFailureException;

    /**
     * Locks the specified datasets.
     */
    public ArchivingResult lockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws UserFailureException;

    /**
     * Unlocks the specified datasets.
     */
    public ArchivingResult unlockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws UserFailureException;

    /**
     * Locks the datasets connected to the specified experiments.
     */
    public ArchivingResult lockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
            throws UserFailureException;

    /**
     * Unlocks the datasets connected to the specified experiments.
     */
    public ArchivingResult unlockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
            throws UserFailureException;

    /**
     * Gets the link from a service that supports the IReportingPluginTask#createLink method.
     */
    public LinkModel retrieveLinkFromDataSet(DatastoreServiceDescription serviceDescription,
            String dataSetCode);

    /**
     * Returns script with given {@link TechId}.
     */
    public Script getScriptInfo(TechId scriptId) throws UserFailureException;

    /**
     * Returns script evaluation result.
     */
    public String evaluate(DynamicPropertyEvaluationInfo info) throws UserFailureException;

    /**
     * Returns script evaluation result.
     */
    public String evaluate(EntityValidationEvaluationInfo info) throws UserFailureException;

    /**
     * Updates specified properties of an entity.
     */
    public IUpdateResult updateProperties(EntityPropertyUpdates updates)
            throws UserFailureException;

    /**
     * Updates specified properties of list of entities.
     */
    public IUpdateResult updateProperties(List<EntityPropertyUpdates> updates)
            throws UserFailureException;

    /**
     * Returns a list of all deletions.
     */
    public TypedTableResultSet<Deletion> listDeletions(
            DefaultResultSetConfig<String, TableModelRowWithObject<Deletion>> criteria)
            throws UserFailureException;

    /**
     * Reverts specified deletions.
     */
    public void revertDeletions(List<TechId> deletionIds) throws UserFailureException;

    /**
     * Reverts specified deletions.
     */
    public void revertDeletions(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria) throws UserFailureException;

    /**
     * Permanently deletes entities moved to trash with specified deletions.
     */
    public void deletePermanently(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria, boolean forceDisallowedTypes)
            throws UserFailureException;

    /**
     * Permanently deletes all entities moved to trash.
     */
    public void emptyTrash(boolean forceDisallowedTypes) throws UserFailureException;

    /**
     * Performs custom import operation
     */
    public List<BatchRegistrationResult> performCustomImport(String sessionKey, String customImportCode, boolean async, String userEmail)
            throws UserFailureException;

    /**
     * Sends the e-mail containing number of active users to CISD Help desk and user, who triggered the action
     */
    public void sendCountActiveUsersEmail() throws UserFailureException;

    /**
     * Register a new metaproject by given name. Throws exception if metaproject by this name already exists.
     */
    public void registerMetaProject(String name) throws UserFailureException;

    /**
     * Lists all the available predeployed plugin names for given script type.
     */
    public List<String> listPredeployedPlugins(ScriptType scriptType);

    /**
     * Gets text for front page if the AS is disabled, null otherwise.
     */
    public String getDisabledText();

}
