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

import com.google.gwt.user.client.rpc.AsyncCallback;

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
 * Asynchronous version of {@link ICommonClientService}.
 *
 * @author Franz-Josef Elmer
 */
public interface ICommonClientServiceAsync extends IClientServiceAsync
{
    /** @see ICommonClientService#keepSessionAlive() */
    public void keepSessionAlive(final AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#listSpaces(DefaultResultSetConfig) */
    public void listSpaces(DefaultResultSetConfig<String, TableModelRowWithObject<Space>> criteria,
            final AsyncCallback<TypedTableResultSet<Space>> asyncCallback);

    /** @see ICommonClientService#listScripts(ListScriptsCriteria) */
    public void listScripts(ListScriptsCriteria criteria,
            final AsyncCallback<TypedTableResultSet<Script>> asyncCallback);

    /** @see ICommonClientService#listMetaprojects() */
    public void listMetaprojects(final AsyncCallback<List<Metaproject>> asyncCallback);

    /** @see ICommonClientService#listMetaprojects(ListMetaprojectsCriteria) */
    public void listMetaprojects(ListMetaprojectsCriteria criteria,
            final AsyncCallback<TypedTableResultSet<Metaproject>> asyncCallback);

    /** @see ICommonClientService#listMetaprojectAssignmentsCounts() */
    public void listMetaprojectAssignmentsCounts(
            final AsyncCallback<List<MetaprojectAssignmentsCount>> asyncCallback);

    /** @see ICommonClientService#getMetaprojectAssignmentsCount(Long) */
    public void getMetaprojectAssignmentsCount(Long metaprojectId,
            final AsyncCallback<MetaprojectAssignmentsCount> asyncCallback);

    /** @see ICommonClientService#assignEntitiesToMetaProjects(EntityKind, List, List) */
    public void assignEntitiesToMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> sampleIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#removeEntitiesFromMetaProjects(EntityKind, List, List) */
    public void removeEntitiesFromMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> sampleIds, AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#getMetaproject(Long)
     */
    public void getMetaproject(Long metaprojectId, final AsyncCallback<Metaproject> asyncCallback);

    /**
     * @see ICommonClientService#getMetaproject(String)
     */
    public void getMetaproject(String metaprojectIdentifier,
            final AsyncCallback<Metaproject> asyncCallback);

    /**
     * @see ICommonClientService#updateMetaproject(Long, IMetaprojectUpdates)
     */
    public void updateMetaproject(Long metaprojectId, IMetaprojectUpdates updates,
            AsyncCallback<Metaproject> callback);

    /** @see ICommonClientService#prepareExportScripts(TableExportCriteria) */
    public void prepareExportScripts(
            TableExportCriteria<TableModelRowWithObject<Script>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#prepareExportMetaprojects(TableExportCriteria) */
    public void prepareExportMetaprojects(
            TableExportCriteria<TableModelRowWithObject<Metaproject>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#prepareExportSpaces(TableExportCriteria) */
    public void prepareExportSpaces(
            TableExportCriteria<TableModelRowWithObject<Space>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerSpace(String, String) */
    public void registerSpace(String spaceCode, String descriptionOrNull,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerScript(Script) */
    public void registerScript(Script newScript, AsyncCallback<Void> callback);

    /** @see ICommonClientService#updateScript(IScriptUpdates) */
    public void updateScript(final IScriptUpdates updates, final AsyncCallback<ScriptUpdateResult> asyncCallback);

    /** @see ICommonClientService#updateSpace(ISpaceUpdates) */
    public void updateSpace(final ISpaceUpdates updates, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listPersons(ListPersonsCriteria) */
    public void listPersons(ListPersonsCriteria criteria,
            AsyncCallback<TypedTableResultSet<Person>> asyncCallback);

    /** @see ICommonClientService#prepareExportPersons(TableExportCriteria) */
    public void prepareExportPersons(
            TableExportCriteria<TableModelRowWithObject<Person>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerPerson(String) */
    public void registerPerson(String code, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listRoleAssignments(DefaultResultSetConfig) */
    public void listRoleAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<RoleAssignment>> criteria,
            AsyncCallback<TypedTableResultSet<RoleAssignment>> asyncCallback);

    /** @see ICommonClientService#prepareExportRoleAssignments(TableExportCriteria) */
    public void prepareExportRoleAssignments(
            TableExportCriteria<TableModelRowWithObject<RoleAssignment>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerProjectRole(RoleWithHierarchy, String, Grantee) */
    public void registerProjectRole(RoleWithHierarchy role, String projectIdentifier, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteProjectRole(RoleWithHierarchy, String, Grantee) */
    public void deleteProjectRole(RoleWithHierarchy role, String projectIdentifier, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerSpaceRole(RoleWithHierarchy, String, Grantee) */
    public void registerSpaceRole(RoleWithHierarchy role, String spaceCode, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSpaceRole(RoleWithHierarchy, String, Grantee) */
    public void deleteSpaceRole(RoleWithHierarchy role, String spaceCode, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerInstanceRole(RoleWithHierarchy, Grantee) */
    public void registerInstanceRole(RoleWithHierarchy role, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteInstanceRole(RoleWithHierarchy, Grantee) */
    public void deleteInstanceRole(RoleWithHierarchy role, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listSampleTypes() */
    public void listSampleTypes(AsyncCallback<List<SampleType>> asyncCallback);

    /** @see ICommonClientService#listManagedInputWidgetDescriptions(EntityKind, String) */
    public void listManagedInputWidgetDescriptions(EntityKind entityKind, String entityTypeCode,
            AsyncCallback<Map<String, List<IManagedInputWidgetDescription>>> asyncCallback);

    /** @see ICommonClientService#listFileTypes() */
    public void listFileTypes(AsyncCallback<List<FileFormatType>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples(ListSampleDisplayCriteria)
     */
    public void listSamples(final ListSampleDisplayCriteria criteria,
            AsyncCallback<ResultSetWithEntityTypes<Sample>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples2(ListSampleDisplayCriteria2)
     */
    public void listSamples2(final ListSampleDisplayCriteria2 criteria,
            AsyncCallback<TypedTableResultSet<Sample>> asyncCallback);

    /**
     * @see ICommonClientService#listMetaprojectSamples(Long)
     */
    public void listMetaprojectSamples(final Long metaprojectId,
            AsyncCallback<List<Sample>> asyncCallback);

    /** @see ICommonClientService#prepareExportSamples(TableExportCriteria) */
    public void prepareExportSamples(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listPropertyTypeAssignments(DefaultResultSetConfig) */
    public void listPropertyTypeAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity,
            final AsyncCallback<TypedTableResultSet<EntityTypePropertyType<?>>> asyncCallback);

    /** @see ICommonClientService#listPropertyTypeAssignmentsFromBrowser(DefaultResultSetConfig, EntityType, List) */
    public void listPropertyTypeAssignmentsFromBrowser(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity,
            List<NewPTNewAssigment> propertyTypesAsgs,
            final AsyncCallback<TypedTableResultSet<EntityTypePropertyType<?>>> asyncCallback);

    /** @see ICommonClientService#listPropertyTypeAssignments(EntityType) */
    public void listPropertyTypeAssignments(EntityType entityType,
            final AsyncCallback<List<EntityTypePropertyType<?>>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportPropertyTypeAssignments(TableExportCriteria)
     */
    public void prepareExportPropertyTypeAssignments(
            final TableExportCriteria<TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#countPropertyTypedEntities(EntityKind, String, String)
     */
    public void countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, AsyncCallback<Integer> asyncCallback);

    /**
     * @see ICommonClientService#listExperiments(ListExperimentsCriteria)
     */
    public void listExperiments(
            final ListExperimentsCriteria criteria,
            AsyncCallback<TypedTableResultSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment>> asyncCallback);

    /**
     * @see ICommonClientService#listMetaprojectExperiments(Long)
     */
    public void listMetaprojectExperiments(
            final Long metaprojectId,
            AsyncCallback<List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportExperiments(TableExportCriteria)
     */
    public void prepareExportExperiments(
            TableExportCriteria<TableModelRowWithObject<Experiment>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#prepareExportDataSetSearchHits(TableExportCriteria)
     */
    public void prepareExportDataSetSearchHits(
            TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listPropertyTypes(boolean) */
    public void listPropertyTypes(boolean withRelations,
            AsyncCallback<List<PropertyType>> asyncCallback);

    /** @see ICommonClientService#listPropertyTypes(DefaultResultSetConfig) */
    public void listPropertyTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> criteria,
            final AsyncCallback<TypedTableResultSet<PropertyType>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportPropertyTypes(TableExportCriteria)
     */
    public void prepareExportPropertyTypes(
            final TableExportCriteria<TableModelRowWithObject<PropertyType>> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#listEntityHistory(ListEntityHistoryCriteria)
     */
    public void listEntityHistory(ListEntityHistoryCriteria criteria,
            AsyncCallback<TypedTableResultSet<EntityHistory>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportEntityHistory(TableExportCriteria)
     */
    public void prepareExportEntityHistory(
            TableExportCriteria<TableModelRowWithObject<EntityHistory>> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#listMatchingEntities(SearchableEntity, String, boolean, IResultSetConfig)
     */
    public void listMatchingEntities(
            final SearchableEntity searchableEntity,
            final String queryText,
            final boolean useWildcardSearchMode,
            final IResultSetConfig<String, TableModelRowWithObject<MatchingEntity>> resultSetConfig,
            final AsyncCallback<TypedTableResultSet<MatchingEntity>> asyncCallback);

    /** @see ICommonClientService#prepareExportMatchingEntities(TableExportCriteria) */
    public void prepareExportMatchingEntities(
            TableExportCriteria<TableModelRowWithObject<MatchingEntity>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listProjects(DefaultResultSetConfig) */
    public void listProjects(
            DefaultResultSetConfig<String, TableModelRowWithObject<Project>> criteria,
            final AsyncCallback<TypedTableResultSet<Project>> asyncCallback);

    /** @see ICommonClientService#listProjectsForTree() */
    public void listProjectsForTree(final AsyncCallback<List<Project>> asyncCallback);

    /** @see ICommonClientService#prepareExportProjects(TableExportCriteria) */
    public void prepareExportProjects(
            TableExportCriteria<TableModelRowWithObject<Project>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listVocabularies(boolean, boolean, DefaultResultSetConfig)
     */
    public void listVocabularies(final boolean withTerms, boolean excludeInternal,
            DefaultResultSetConfig<String, TableModelRowWithObject<Vocabulary>> criteria,
            final AsyncCallback<TypedTableResultSet<Vocabulary>> asyncCallback);

    /** @see ICommonClientService#prepareExportVocabularies(TableExportCriteria) */
    public void prepareExportVocabularies(
            TableExportCriteria<TableModelRowWithObject<Vocabulary>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listVocabularyTerms(Vocabulary, DefaultResultSetConfig)
     */
    public void listVocabularyTerms(
            Vocabulary vocabulary,
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<VocabularyTermWithStats>> callback);

    /** @see ICommonClientService#prepareExportVocabularyTerms(TableExportCriteria) */
    public void prepareExportVocabularyTerms(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listMaterialTypes(DefaultResultSetConfig) */
    public void listMaterialTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialType>> criteria,
            final AsyncCallback<TypedTableResultSet<MaterialType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportMaterialTypes(TableExportCriteria) */
    public void prepareExportMaterialTypes(
            final TableExportCriteria<TableModelRowWithObject<MaterialType>> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listSampleTypes(DefaultResultSetConfig) */
    public void listSampleTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<SampleType>> criteria,
            final AsyncCallback<TypedTableResultSet<SampleType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportSampleTypes(TableExportCriteria) */
    public void prepareExportSampleTypes(
            final TableExportCriteria<TableModelRowWithObject<SampleType>> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listExperimentTypes(DefaultResultSetConfig) */
    public void listExperimentTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<ExperimentType>> criteria,
            final AsyncCallback<TypedTableResultSet<ExperimentType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportExperimentTypes(TableExportCriteria) */
    public void prepareExportExperimentTypes(
            final TableExportCriteria<TableModelRowWithObject<ExperimentType>> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listDataSetTypes(DefaultResultSetConfig) */
    public void listDataSetTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<DataSetType>> criteria,
            final AsyncCallback<TypedTableResultSet<DataSetType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportDataSetTypes(TableExportCriteria) */
    public void prepareExportDataSetTypes(
            final TableExportCriteria<TableModelRowWithObject<DataSetType>> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listFileTypes(DefaultResultSetConfig) */
    public void listFileTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria,
            final AsyncCallback<TypedTableResultSet<FileFormatType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportFileTypes(TableExportCriteria) */
    public void prepareExportFileTypes(
            TableExportCriteria<TableModelRowWithObject<FileFormatType>> criteria,
            AsyncCallback<String> callback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportAttachmentVersions(TableExportCriteria) */
    public void prepareExportAttachmentVersions(
            TableExportCriteria<TableModelRowWithObject<AttachmentVersions>> criteria,
            AsyncCallback<String> callback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * @see ICommonClientService#getExportTable(String, String)
     */
    public void getExportTable(String exportDataKey, String lineSeparator,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#removeResultSet(String)
     */
    public void removeResultSet(final String resultSetKey, final AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#listSampleDataSets(TechId, DefaultResultSetConfig, boolean)
     */
    public void listSampleDataSets(TechId sampleId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            boolean showOnlyDirectlyConnected,
            AsyncCallback<TypedTableResultSet<AbstractExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listSampleDataSets(TechId sampleId, boolean showOnlyDirectlyConnected)
     */
    public void listSampleDataSets(TechId sampleId, boolean showOnlyDirectlyConnected,
            AsyncCallback<List<String>> asyncCallback);

    /**
     * @see ICommonClientService#getSampleChildrenInfo(List<TechId> sampleIds, boolean showOnlyDirectlyConnected)
     */
    public void getSampleChildrenInfo(List<TechId> sampleIds, boolean showOnlyDirectlyConnected,
            AsyncCallback<List<SampleChildrenInfo>> asyncCallback);

    /**
     * @see ICommonClientService#listExperimentDataSets(TechId, DefaultResultSetConfig, boolean)
     */
    public void listExperimentDataSets(TechId experimentId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            boolean onlyDirectlyConnected,
            AsyncCallback<TypedTableResultSet<AbstractExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listMetaprojectDataSets(TechId, DefaultResultSetConfig)
     */
    public void listMetaprojectDataSets(TechId metaprojectId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            AsyncCallback<TypedTableResultSet<AbstractExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listMetaprojectDataSets(Long)
     */
    public void listMetaprojectDataSets(Long metaprojectId,
            AsyncCallback<List<AbstractExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listDataSetRelationships(TechId, DataSetRelationshipRole, DefaultResultSetConfig)
     */
    public void listDataSetRelationships(
            TechId datasetId,
            DataSetRelationshipRole role,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<AbstractExternalData>> callback);

    /**
     * @see ICommonClientService#listSearchableEntities()
     */
    public void listSearchableEntities(final AsyncCallback<List<SearchableEntity>> asyncCallback);

    /** @see ICommonClientService#listExperimentTypes() */
    public void listExperimentTypes(
            final AsyncCallback<List<ExperimentType>> listExperimentTypesCallback);

    /** @see ICommonClientService#listDataTypes() */
    public void listDataTypes(final AsyncCallback<List<DataType>> asyncCallback);

    /**
     * @see ICommonClientService#registerEntitytypeAndAssignPropertyTypes(NewETNewPTAssigments newETNewPTAssigments)
     */
    public void registerEntitytypeAndAssignPropertyTypes(NewETNewPTAssigments newETNewPTAssigments, AsyncCallback<String> process);

    /**
     * @see ICommonClientService#updateEntitytypeAndPropertyTypes(NewETNewPTAssigments newETNewPTAssigments)
     */
    public void updateEntitytypeAndPropertyTypes(NewETNewPTAssigments newETNewPTAssigments, AsyncCallback<String> process);

    /**
     * @see ICommonClientService#registerAndAssignPropertyType(PropertyType propertyType, NewETPTAssignment assignment)
     */
    public void registerAndAssignPropertyType(PropertyType propertyType, NewETPTAssignment assignment, AsyncCallback<String> process);

    /**
     * @see ICommonClientService#assignPropertyType(NewETPTAssignment assignment)
     */
    public void assignPropertyType(NewETPTAssignment assignment, AsyncCallback<String> process);

    /**
     * @see ICommonClientService#updatePropertyTypeAssignment(NewETPTAssignment)
     */
    public void updatePropertyTypeAssignment(NewETPTAssignment assignmentUpdates,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#unassignPropertyType(EntityKind, String, String)
     */
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerPropertyType(PropertyType) */
    public void registerPropertyType(final PropertyType propertyType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updatePropertyType(IPropertyTypeUpdates) */
    public void updatePropertyType(final IPropertyTypeUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerVocabulary(String, NewVocabulary) */
    public void registerVocabulary(final String termsSessionKey, final NewVocabulary vocabulary,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateVocabulary(IVocabularyUpdates) */
    public void updateVocabulary(final IVocabularyUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#addVocabularyTerms(TechId, List, Long) */
    public void addVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> vocabularyTerms,
            Long previousTermOrdinal, AsyncCallback<Void> callback);

    /** @see ICommonClientService#addUnofficialVocabularyTerm(TechId, String, String, String, Long) */
    public void addUnofficialVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal, AsyncCallback<Void> callback);

    /** @see ICommonClientService#updateVocabularyTerm(IVocabularyTermUpdates) */
    public void updateVocabularyTerm(final IVocabularyTermUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateVocabularyTerms(String,TechId) */
    public void updateVocabularyTerms(final String termsSessionKey, TechId vocabularyId,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteVocabularyTerms(TechId, List, List) */
    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced, AsyncCallback<Void> callback);

    /** @see ICommonClientService#makeVocabularyTermsOfficial(TechId, List) */
    public void makeVocabularyTermsOfficial(TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial, AsyncCallback<Void> callback);

    /** @see ICommonClientService#listVocabularyTerms(Vocabulary) */
    public void listVocabularyTerms(Vocabulary vocabulary,
            AsyncCallback<List<VocabularyTerm>> callback);

    /** @see ICommonClientService#registerProject(String, Project) */
    public void registerProject(String sessionKey, Project project,
            final AsyncCallback<Void> projectRegistrationCallback);

    /**
     * @see ICommonClientService#searchForDataSets(DetailedSearchCriteria, IResultSetConfig)
     */
    public void searchForDataSets(
            DetailedSearchCriteria criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            final AsyncCallback<TypedTableResultSet<AbstractExternalData>> callback);

    /**
     * @see ICommonClientService#searchForDataSets(RelatedDataSetCriteria, IResultSetConfig)
     */
    public void searchForDataSets(
            RelatedDataSetCriteria<? extends IEntityInformationHolder> criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            final AsyncCallback<TypedTableResultSet<AbstractExternalData>> callback);

    /**
     * @see ICommonClientService#listMaterialTypes()
     */
    public void listMaterialTypes(AsyncCallback<List<MaterialType>> callback);

    /**
     * @see ICommonClientService#listMaterials(ListMaterialDisplayCriteria)
     */
    public void listMaterials(ListMaterialDisplayCriteria criteria,
            AsyncCallback<TypedTableResultSet<Material>> callback);

    /**
     * @see ICommonClientService#listMetaprojectMaterials(Long)
     */
    public void listMetaprojectMaterials(Long metaprojectId, AsyncCallback<List<Material>> callback);

    /** @see ICommonClientService#prepareExportMaterials(TableExportCriteria) */
    public void prepareExportMaterials(
            TableExportCriteria<TableModelRowWithObject<Material>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerMaterialType(MaterialType) */
    public void registerMaterialType(MaterialType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerDataSetType(DataSetType) */
    public void registerDataSetType(DataSetType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerFileType(FileFormatType) */
    public void registerFileType(FileFormatType type, AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerSampleType(SampleType) */
    public void registerSampleType(SampleType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerExperimentType(ExperimentType) */
    public void registerExperimentType(ExperimentType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateEntityType(EntityKind, EntityType) */
    public void updateEntityType(EntityKind entityKind, EntityType entityType,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#deleteDataSets(DisplayedOrSelectedDatasetCriteria, String, DeletionType, boolean)
     */
    public void deleteDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, String reason,
            DeletionType deletionType, boolean forceDisallowedTypes,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteDataSet(String, String, DeletionType, boolean) */
    public void deleteDataSet(String singleData, String reason, DeletionType deletionType,
            boolean forceDisallowedTypes, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSamples(List, String, DeletionType) */
    public void deleteSamples(List<TechId> sampleIds, String reason, DeletionType deletionType,
            AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#deleteSamples(DisplayedOrSelectedIdHolderCriteria, String, DeletionType)
     */
    public void deleteSamples(DisplayedOrSelectedIdHolderCriteria<? extends IIdHolder> criteria,
            String reason, DeletionType deletionType, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSample(TechId, String, DeletionType) */
    public void deleteSample(TechId sampleIs, String reason, DeletionType deletionType,
            AsyncCallback<Void> asyncCallback);

    /**
     * @param deletionType
     * @see ICommonClientService#deleteExperiments(DisplayedOrSelectedIdHolderCriteria, String, DeletionType)
     */
    public void deleteExperiments(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Experiment>> criteria,
            String reason, DeletionType deletionType, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteExperiment(TechId, String, DeletionType) */
    public void deleteExperiment(TechId experimentId, String reason, DeletionType deletionType,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteProjects(List, String) */
    public void deleteProjects(List<TechId> projectIds, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteMetaprojects(List, String) */
    public void deleteMetaprojects(List<TechId> metaprojectIds, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSpaces(List, String) */
    public void deleteSpaces(List<TechId> spaceIds, String reason, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteScripts(List) */
    public void deleteScripts(List<TechId> scriptIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteVocabularies(List, String) */
    public void deleteVocabularies(List<TechId> vocabularyIds, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deletePropertyTypes(List, String) */
    public void deletePropertyTypes(List<TechId> propertyTypeIds, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteAttachments(TechId, AttachmentHolderKind, List, String) */
    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason, AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#listAttachmentVersions(TechId, AttachmentHolderKind, DefaultResultSetConfig)
     */
    public void listAttachmentVersions(TechId holderId, AttachmentHolderKind holderKind,
            DefaultResultSetConfig<String, TableModelRowWithObject<AttachmentVersions>> criteria,
            AsyncCallback<TypedTableResultSet<AttachmentVersions>> asyncCallback);

    /** @see ICommonClientService#listDataSetTypes() */
    public void listDataSetTypes(AsyncCallback<List<DataSetType>> callback);

    /**
     * @see ICommonClientService#uploadDataSets(DisplayedOrSelectedDatasetCriteria, DataSetUploadParameters)
     */
    public void uploadDataSets(DisplayedOrSelectedDatasetCriteria criteria,
            DataSetUploadParameters uploadParameters, AsyncCallback<String> callback);

    /** @see ICommonClientService#getLastModificationState() */
    public void getLastModificationState(AsyncCallback<LastModificationState> asyncCallback);

    /**
     * @see ICommonClientService#getExperimentInfo(TechId)
     */
    public void getExperimentInfo(String experimentIdentifier,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see ICommonClientService#getExperimentInfo(TechId)
     */
    public void getExperimentInfoByPermId(String experimentPermId,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see ICommonClientService#getExperimentInfo(TechId)
     */
    public void getExperimentInfo(TechId experimentId,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see ICommonClientService#getProjectInfo(TechId)
     */
    public void getProjectInfo(TechId projectId, AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#getProjectInfo(BasicProjectIdentifier)
     */
    public void getProjectInfo(BasicProjectIdentifier projectIdentifier,
            AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#getProjectInfoByPermId(String)
     */
    public void getProjectInfoByPermId(String projectPermId,
            final AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#generateCode(String, EntityKind)
     */
    public void generateCode(String codePrefix, EntityKind entityKind,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateProject(ProjectUpdates)
     */
    public void updateProject(ProjectUpdates updates, AsyncCallback<Integer> projectEditCallback);

    /**
     * @see ICommonClientService#deleteEntityTypes(EntityKind, List)
     */
    public void deleteEntityTypes(EntityKind entityKind, List<String> entityTypeCodes,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#deleteFileFormatTypes(List)
     */
    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#getEntityInformationHolder(EntityKind, String)
     */
    public void getEntityInformationHolder(EntityKind entityKind, String permId,
            AsyncCallback<IEntityInformationHolderWithPermId> callback);

    /**
     * @see ICommonClientService#getEntityInformationHolder(BasicEntityDescription)
     */
    public void getEntityInformationHolder(BasicEntityDescription info,
            AsyncCallback<IEntityInformationHolderWithPermId> callback);

    /**
     * @see ICommonClientService#getMaterialInfo(MaterialIdentifier)
     */
    public void getMaterialInfo(MaterialIdentifier materialIdentifier,
            AsyncCallback<Material> callback);

    /**
     * @see ICommonClientService#getMaterialInfo(TechId)
     */
    public void getMaterialInfo(TechId techId, AsyncCallback<Material> callback);

    /**
     * @see ICommonClientService#getMaterialInformationHolder(MaterialIdentifier)
     */
    public void getMaterialInformationHolder(MaterialIdentifier materialIdentifier,
            AsyncCallback<IEntityInformationHolderWithPermId> openEntityDetailsTabCallback);

    /**
     * @see ICommonClientService#getTemplate(EntityKind, String, String, boolean, boolean, boolean, BatchOperationKind)
     */
    public void getTemplate(EntityKind kind, String type, String format, boolean autoGenerate,
            boolean withExperiments, boolean withSpace, BatchOperationKind operationKind,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateFileFormatType(AbstractType type)
     */
    public void updateFileFormatType(AbstractType type, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#updateAttachment(TechId, AttachmentHolderKind, Attachment) */
    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#addAttachment(TechId, String, AttachmentHolderKind, NewAttachment) */
    public void addAttachment(TechId holderId, String sessionKey, AttachmentHolderKind holderKind,
            NewAttachment attachment, AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#updateManagedProperty(TechId, EntityKind, IManagedProperty, IManagedUiAction)
     */
    public void updateManagedProperty(TechId entityId, EntityKind entityKind,
            IManagedProperty managedProperty, IManagedUiAction updateAction,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listDataStoreServices(DataStoreServiceKind) */
    public void listDataStoreServices(DataStoreServiceKind pluginTaskKind,
            AsyncCallback<List<DatastoreServiceDescription>> callback);

    /**
     * @see ICommonClientService#createReportFromDatasets(DatastoreServiceDescription, DisplayedOrSelectedDatasetCriteria)
     */
    public void createReportFromDatasets(DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            AsyncCallback<TableModelReference> callback);

    /**
     * @see ICommonClientService#createReportFromTableModel(TableModel)
     */
    public void createReportFromTableModel(TableModel tableModel,
            AsyncCallback<TableModelReference> callback);

    /**
     * @see ICommonClientService#createReportFromAggregationService(DatastoreServiceDescription, Map)
     */
    public void createReportFromAggregationService(DatastoreServiceDescription serviceDescription,
            Map<String, Object> parameters, AsyncCallback<TableModelReference> callback);

    /**
     * @see ICommonClientService#listReport(IResultSetConfig)
     */
    public void listReport(
            IResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<ReportRowModel>> callback);

    /**
     * @see ICommonClientService#prepareExportReport(TableExportCriteria)
     */
    public void prepareExportReport(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#processDatasets(DatastoreServiceDescription, DisplayedOrSelectedDatasetCriteria)
     */
    public void processDatasets(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#archiveDatasets(DisplayedOrSelectedDatasetCriteria, boolean)
     */
    public void archiveDatasets(DisplayedOrSelectedDatasetCriteria criteria, boolean removeFromDataStore,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unarchiveDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void unarchiveDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#archiveDatasets(DisplayedCriteriaOrSelectedEntityHolder, boolean)
     */
    public void archiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria, boolean removeFromDataStore,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unarchiveDatasets(DisplayedCriteriaOrSelectedEntityHolder)
     */
    public void unarchiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#deleteAuthorizationGroups(List, String)
     */
    public void deleteAuthorizationGroups(List<TechId> createList, String reason,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#prepareExportAuthorizationGroups(TableExportCriteria)
     */
    public void prepareExportAuthorizationGroups(
            TableExportCriteria<TableModelRowWithObject<AuthorizationGroup>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listAuthorizationGroups(DefaultResultSetConfig)
     */
    public void listAuthorizationGroups(
            DefaultResultSetConfig<String, TableModelRowWithObject<AuthorizationGroup>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<AuthorizationGroup>> callback);

    /**
     * @see ICommonClientService#registerAuthorizationGroup(NewAuthorizationGroup)
     */
    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthGroup,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#listPersonsInAuthorizationGroup(TechId)
     */
    public void listPersonsInAuthorizationGroup(TechId group, AsyncCallback<List<Person>> callback)
            throws UserFailureException;

    /**
     * @see ICommonClientService#updateAuthorizationGroup(AuthorizationGroupUpdates)
     */
    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#listPersons()
     */
    public void listPersons(AsyncCallback<List<Person>> callback);

    /**
     * @see ICommonClientService#listActivePersons()
     */
    public void listActivePersons(AsyncCallback<List<Person>> callback);

    /**
     * @see ICommonClientService#listAuthorizationGroups()
     */
    public void listAuthorizationGroups(AsyncCallback<List<AuthorizationGroup>> callback);

    /**
     * @see ICommonClientService#addPersonsToAuthorizationGroup(TechId, List)
     */
    public void addPersonsToAuthorizationGroup(TechId authGroupId, List<String> personsCodes,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#removePersonsFromAuthorizationGroup(TechId, List)
     */
    public void removePersonsFromAuthorizationGroup(TechId create, List<String> personsCodes,
            AsyncCallback<Void> callback);

    // -- custom grid filters

    /**
     * @see ICommonClientService#listFilters(String)
     */
    public void listFilters(String gridId, AsyncCallback<List<GridCustomFilter>> callback);

    /**
     * @see ICommonClientService#listFilters(String, DefaultResultSetConfig)
     */
    public void listFilters(
            String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<GridCustomFilter>> callback);

    /**
     * @see ICommonClientService#prepareExportFilters(TableExportCriteria)
     */
    public void prepareExportFilters(
            final TableExportCriteria<TableModelRowWithObject<GridCustomFilter>> criteria,
            AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#registerFilter(NewColumnOrFilter) */
    public void registerFilter(NewColumnOrFilter newFilter, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteFilters(List) */
    public void deleteFilters(List<TechId> filterIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateFilter(IExpressionUpdates) */
    public void updateFilter(IExpressionUpdates updates, AsyncCallback<Void> registrationCallback);

    // -- custom grid columns

    /**
     * @see ICommonClientService#listGridCustomColumns(String)
     */
    public void listGridCustomColumns(String gridId, AsyncCallback<List<GridCustomColumn>> callback);

    /**
     * @see ICommonClientService#listGridCustomColumns(String, DefaultResultSetConfig)
     */
    public void listGridCustomColumns(
            String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomColumn>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<GridCustomColumn>> callback);

    /**
     * @see ICommonClientService#prepareExportColumns(TableExportCriteria)
     */
    public void prepareExportColumns(
            final TableExportCriteria<TableModelRowWithObject<GridCustomColumn>> criteria,
            AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#registerColumn(NewColumnOrFilter) */
    public void registerColumn(NewColumnOrFilter newColumn, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteColumns(List) */
    public void deleteColumns(List<TechId> columnIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateColumn(IExpressionUpdates) */
    public void updateColumn(IExpressionUpdates updates, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteMaterials(DisplayedOrSelectedIdHolderCriteria, String) */
    public void deleteMaterials(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Material>> uploadCriteria,
            String reason, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#lockDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void lockDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unlockDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void unlockDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#lockDatasets(DisplayedCriteriaOrSelectedEntityHolder)
     */
    public void lockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unlockDatasets(DisplayedCriteriaOrSelectedEntityHolder)
     */
    public void unlockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#retrieveLinkFromDataSet(DatastoreServiceDescription, String)
     */
    public void retrieveLinkFromDataSet(DatastoreServiceDescription serviceDescription,
            String dataSetCode, AsyncCallback<LinkModel> callback);

    /**
     * @see ICommonClientService#getScriptInfo(TechId)
     */
    public void getScriptInfo(TechId scriptId, AsyncCallback<Script> scriptInfoCallback);

    /**
     * @see ICommonClientService#evaluate(DynamicPropertyEvaluationInfo)
     */
    public void evaluate(DynamicPropertyEvaluationInfo dynamicPropertyEvaluationInfo,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#evaluate(EntityValidationEvaluationInfo)
     */
    public void evaluate(EntityValidationEvaluationInfo entityValidationEvaluationInfo,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateProperties(EntityPropertyUpdates)
     */
    public void updateProperties(EntityPropertyUpdates entityPropertyUpdates,
            AsyncCallback<IUpdateResult> callback);

    /**
     * @see ICommonClientService#updateProperties(List)
     */
    public void updateProperties(List<EntityPropertyUpdates> entityPropertyUpdates,
            AsyncCallback<IUpdateResult> callback);

    /** @see ICommonClientService#listDeletions(DefaultResultSetConfig) */
    public void listDeletions(
            DefaultResultSetConfig<String, TableModelRowWithObject<Deletion>> criteria,
            final AsyncCallback<TypedTableResultSet<Deletion>> asyncCallback);

    /** @see ICommonClientService#prepareExportDeletions(TableExportCriteria) */
    public void prepareExportDeletions(
            TableExportCriteria<TableModelRowWithObject<Deletion>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#revertDeletions(List)
     */
    public void revertDeletions(List<TechId> deletionIds, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#revertDeletions(DisplayedOrSelectedIdHolderCriteria)
     */
    public void revertDeletions(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#deletePermanently(DisplayedOrSelectedIdHolderCriteria, boolean, boolean)
     */
    public void deletePermanently(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria,
            boolean forceToDeleteDependentDeletionSets, boolean forceDisallowedTypes,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#emptyTrash(boolean)
     */
    public void emptyTrash(boolean forceDisallowedTypes, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#performCustomImport(String, String, boolean, String)
     */
    public void performCustomImport(String sessionKey, String customImportCode, boolean async, String userEmail,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * @see ICommonClientService#sendCountActiveUsersEmail()
     */
    public void sendCountActiveUsersEmail(AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#registerMetaProject(String)
     */
    public void registerMetaProject(String name, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#listPredeployedPlugins(ScriptType)
     */
    public void listPredeployedPlugins(ScriptType scriptType, AsyncCallback<List<String>> callback);

    /**
     * @see ICommonClientService#getDisabledText()
     */
    public void getDisabledText(AsyncCallback<String> callback);
}
