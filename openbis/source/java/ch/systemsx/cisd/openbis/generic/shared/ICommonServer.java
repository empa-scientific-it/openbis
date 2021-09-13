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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Definition of the client-server interface of the openBIS core. It contains entity-type unspecific operations needed by all openBIS installations.
 * Customization for a specific use cases (i.e. specific entity types) can be achieved through implementing plugins.
 *
 * @author Franz-Josef Elmer
 */
public interface ICommonServer extends IServer
{
    /** Keeps the session with specified token alive. */
    @Transactional(readOnly = true)
    public void keepSessionAlive(String sessionToken);

    /**
     * Returns scripts of specified type compatible with selected {@link EntityKind}. If script type or entity kind is not specified no restriction on
     * them is set.
     *
     * @return a sorted list of {@link Script}s.
     */
    @Transactional(readOnly = true)
    public List<Script> listScripts(String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull);

    /**
     * Returns deletions which belong to the specified database instance. *
     *
     * @return a sorted list of {@link Space}.
     */
    @Transactional(readOnly = true)
    public List<Space> listSpaces(String sessionToken);

    /**
     * Registers a new space with specified code and optional description.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public void registerSpace(String sessionToken, String spaceCode, String descriptionOrNull);

    /**
     * Updates a script.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SCRIPT)
    public ScriptUpdateResult updateScript(final String sessionToken, final IScriptUpdates updates);

    /**
     * Updates a property type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SPACE)
    public void updateSpace(final String sessionToken, final ISpaceUpdates updates);

    /**
     * Registers a new authorization group.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup);

    /**
     * Registers a new script.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SCRIPT)
    public void registerScript(String sessionToken, Script script);

    /**
     * Deletes selected authorization groups.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public void deleteAuthorizationGroups(String sessionToken, List<TechId> authGroupIds,
            String reason);

    /**
     * Returns all persons from current instance.
     *
     * @return a sorted list of {@link Person}.
     */
    @Transactional(readOnly = true)
    public List<Person> listPersons(String sessionToken);

    /**
     * Returns all active persons from current instance.
     *
     * @return a sorted list of {@link Person}.
     */
    @Transactional(readOnly = true)
    public List<Person> listActivePersons(String sessionToken);

    /**
     * Returns all projects.
     *
     * @return a sorted list of {@link Project}.
     */
    @Transactional(readOnly = true)
    public List<Project> listProjects(String sessionToken);

    /**
     * Registers a new person.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PERSON)
    public void registerPerson(String sessionToken, String userID);

    /**
     * Returns a list of all roles.
     */
    @Transactional(readOnly = true)
    public List<RoleAssignment> listRoleAssignments(String sessionToken);

    /**
     * Registers a new project role.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void registerProjectRole(String sessionToken, RoleCode roleCode,
            ProjectIdentifier identifier, Grantee grantee);

    /**
     * Registers a new space role.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void registerSpaceRole(String sessionToken, RoleCode roleCode,
            SpaceIdentifier identifier, Grantee grantee);

    /**
     * Registers a new instance role.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee);

    /**
     * Deletes role described by given role code, project identifier and grantee.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void deleteProjectRole(String sessionToken, RoleCode roleCode, ProjectIdentifier identifier,
            Grantee grantee);

    /**
     * Deletes role described by given role code, space identifier and grantee.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void deleteSpaceRole(String sessionToken, RoleCode roleCode, SpaceIdentifier identifier,
            Grantee grantee);

    /**
     * Deletes role described by given role code and user id.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee);

    /**
     * Lists sample types which are appropriate for listing.
     *
     * @return a sorted list of {@link SampleType}.
     */
    @Transactional(readOnly = true)
    public List<SampleType> listSampleTypes(String sessionToken);

    /**
     * Lists managed properties widgets descriptions for the specified entity kind and entity type code.
     */
    @Transactional(readOnly = true)
    public Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            String sessionToken, EntityKind entityKind, String entityTypeCode);

    /**
     * Lists samples using given configuration.
     *
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    public List<Sample> listSamples(final String sessionToken, final ListSampleCriteria criteria);

    /**
     * Lists samples for metaproject.
     *
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    public List<Sample> listMetaprojectSamples(final String sessionToken,
            IMetaprojectId metaprojectId);

    /**
     * Lists samples using given configuration on behalf of another user.
     *
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    public List<Sample> listSamplesOnBehalfOfUser(final String sessionToken,
            final ListSampleCriteria criteria, String userId);

    /**
     * Returns all samples which have at least one property of type MATERIAL referring to one of the specified materials.
     */
    @Transactional(readOnly = true)
    public List<Sample> listSamplesByMaterialProperties(String sessionToken, Collection<TechId> materialIds);

    /**
     * Lists experiments for metaproject.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listMetaprojectExperiments(final String sessionToken,
            IMetaprojectId metaprojectId);

    /**
     * Lists experiments by project.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(final String sessionToken,
            ExperimentType experimentType, ProjectIdentifier project);

    /**
     * Lists experiments by projects.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(final String sessionToken,
            ExperimentType experimentType, List<ProjectIdentifier> projects);

    /**
     * Lists experiments having data sets by project.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperimentsHavingDataSets(final String sessionToken,
            ExperimentType experimentType, List<ProjectIdentifier> projects);

    /**
     * Lists experiments having samples by project.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperimentsHavingSamples(final String sessionToken,
            ExperimentType experimentType, List<ProjectIdentifier> projects);

    /**
     * Lists experiments by space.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(final String sessionToken,
            ExperimentType experimentType, SpaceIdentifier space);

    /**
     * Lists experiments by project.
     *
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public List<Experiment> listExperiments(final String sessionToken,
            List<ExperimentIdentifier> experimentIdentifiers);

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link AbstractExternalData}.
     *
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected);

    /**
     * For given experiment {@link TechId} returns the corresponding list of {@link AbstractExternalData}.
     *
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId, boolean showOnlyDirectlyConnected);

    /**
     * For given metaproject {@link IMetaprojectId} returns the corresponding list of {@link AbstractExternalData}.
     *
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listMetaprojectExternalData(final String sessionToken,
            final IMetaprojectId metaprojectId);

    /**
     * For given data set {@link TechId} in given relationship <var>role</var> returns corresponding list of {@link AbstractExternalData}.
     *
     * @return a sorted list of {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listDataSetRelationships(final String sessionToken,
            final TechId datasetId, final DataSetRelationshipRole role);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     *
     * @param maxSize
     */
    @Transactional(readOnly = true)
    public List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode, int maxSize);

    /**
     * List experiment types.
     *
     * @return a sorted list of {@link ExperimentType}.
     */
    @Transactional(readOnly = true)
    public List<ExperimentType> listExperimentTypes(String sessionToken);

    /**
     * List property types.
     *
     * @return a sorted list of {@link PropertyType}.
     */
    @Transactional(readOnly = true)
    public List<PropertyType> listPropertyTypes(final String sessionToken, boolean withRelations);

    /**
     * Lists historical values of specified entity.
     */
    @Transactional(readOnly = true)
    public List<EntityHistory> listEntityHistory(String sessionToken, EntityKind entityKind,
            TechId entityID);

    /**
     * List property assigments.
     *
     * @return a sorted list of {@link PropertyType}.
     */
    @Transactional(readOnly = true)
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(final String sessionToken);

    /**
     * List property assigments for the given entity type.
     *
     * @return a sorted list of {@link PropertyType}.
     */
    @Transactional(readOnly = true)
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(final String sessionToken,
            final EntityType entityType);

    /**
     * Lists data types.
     *
     * @return a sorted list of {@link DataType}.
     */
    @Transactional(readOnly = true)
    public List<DataType> listDataTypes(final String sessionToken);

    /**
     * Lists file format types.
     *
     * @return a sorted list of {@link FileFormatType}.
     */
    @Transactional(readOnly = true)
    public List<FileFormatType> listFileFormatTypes(String sessionToken);

    /**
     * Lists vocabularies.
     *
     * @return a sorted list of {@link Vocabulary}.
     */
    @Transactional(readOnly = true)
    public List<Vocabulary> listVocabularies(final String sessionToken, final boolean withTerms,
            boolean excludeInternal);

    /**
     * Registers given {@link PropertyType}.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE)
    public void registerPropertyType(final String sessionToken, final PropertyType propertyType);

    /**
     * Updates a property type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROPERTY_TYPE)
    public void updatePropertyType(final String sessionToken, final IPropertyTypeUpdates updates);

    /**
     * Deletes specified property types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE)
    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason);

    /**
     * Creates and assigns property type to entity type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROPERTY_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT, ObjectKind.DATASET_TYPE,
            ObjectKind.SAMPLE_TYPE, ObjectKind.EXPERIMENT_TYPE, ObjectKind.MATERIAL_TYPE })
    public String registerEntitytypeAndAssignPropertyTypes(final String sessionToken, NewETNewPTAssigments newETNewPTAssigments);

    /**
     * Updates and assigns property type to entity type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROPERTY_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT, ObjectKind.DATASET_TYPE,
            ObjectKind.SAMPLE_TYPE, ObjectKind.EXPERIMENT_TYPE, ObjectKind.MATERIAL_TYPE })
    public String updateEntitytypeAndPropertyTypes(final String sessionToken, NewETNewPTAssigments newETNewPTAssigments);

    /**
     * Creates and assigns property type to entity type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROPERTY_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public String registerAndAssignPropertyType(final String sessionToken, PropertyType propertyType, NewETPTAssignment assignment);

    /**
     * Assigns property type to entity type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public String assignPropertyType(final String sessionToken, NewETPTAssignment assignment);

    /**
     * Update property type assignment to entity type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public void updatePropertyTypeAssignment(final String sessionToken,
            NewETPTAssignment assignmentUpdates);

    /**
     * Unassigns property type to entity type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode);

    /**
     * Returns the number of entities of specified kind and type which have a property of specified type.
     */
    @Transactional(readOnly = true)
    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode);

    /**
     * Registers given {@link NewVocabulary}.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    public void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary);

    /**
     * Updates a vocabulary.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY)
    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates);

    /**
     * Deletes specified vocabularies.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason);

    /**
     * Deletes specified projects.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason);

    /**
     * Deletes specified spaces.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public void deleteSpaces(String sessionToken, List<TechId> spaceIds, String reason);

    /**
     * Deletes specified scripts.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SCRIPT)
    public void deleteScripts(String sessionToken, List<TechId> scriptIds);

    /**
     * Adds new terms to a vocabulary starting from specified ordinal + 1.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> vocabularyTerms, Long previousTermOrdinal);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal);

    /**
     * Updates a vocabulary term.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void updateVocabularyTerm(final String sessionToken, final IVocabularyTermUpdates updates);

    /**
     * Deletes from the specified vocabulary the specified terms.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced);

    /**
     * Makes given vocabulary terms official.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void makeVocabularyTermsOfficial(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial);

    /**
     * Registers new project.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments);

    /**
     * Searches for experiments that fulfill the specified search criteria.
     */
    @Transactional(readOnly = true)
    public List<Experiment> searchForExperiments(String sessionToken,
            DetailedSearchCriteria criteria);

    /**
     * Searches on a search domain. The result is sorted by score in descending order.
     */
    @Transactional(readOnly = true)
    public List<SearchDomainSearchResultWithFullEntity> searchOnSearchDomain(String sessionToken,
            String preferredSearchDomainOrNull, String searchString, Map<String, String> optionalParametersOrNull);

    /**
     * Lists all available search domains.
     */
    @Transactional(readOnly = true)
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken);

    /**
     * Searches for data sets that fulfill the specified search criteria.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            DetailedSearchCriteria criteria);

    /**
     * Searches for samples that fulfill the specified search criteria. The search is executed on behalf of a user identified by the userId (the
     * returned results are exactly the same as if that user called the search method).
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> searchForDataSetsOnBehalfOfUser(String sessionToken,
            DetailedSearchCriteria criteria, String userId);

    /**
     * For given {@link TechId} returns the corresponding {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData getDataSetInfo(String sessionToken, TechId datasetId);

    /**
     * Saves changed data set.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria);

    /**
     * Returns all data sets related to specified entities.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities entities, boolean withDetails);

    /**
     * Returns all data sets related to specified entities on behalf of given user.
     */
    @Transactional(readOnly = true)
    public List<AbstractExternalData> listRelatedDataSetsOnBehalfOfUser(String sessionToken,
            DataSetRelatedEntities entities, boolean withDetails, String userId);

    /**
     * List material types.
     *
     * @return a sorted list of {@link MaterialType}.
     */
    @Transactional(readOnly = true)
    public List<MaterialType> listMaterialTypes(String sessionToken);

    /**
     * Returns material type for given code.
     *
     * @return {@link MaterialType} for given code.
     */
    @Transactional(readOnly = true)
    public MaterialType getMaterialType(String sessionToken, String code);

    /**
     * Lists materials using given criteria.
     *
     * @return a sorted list of {@link Material}.
     */
    @Transactional(readOnly = true)
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties);

    /**
     * Returns the technical ids of all materials which have at least one property of type MATERIAL referring to one of the specified materials.
     */
    @Transactional(readOnly = true)
    public Collection<TechId> listMaterialIdsByMaterialProperties(String sessionToken, Collection<TechId> materialIds);

    /**
     * Lists materials for metaproject.
     *
     * @return a sorted list of {@link Material}.
     */
    @Transactional(readOnly = true)
    public List<Material> listMetaprojectMaterials(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Creates a new material type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL_TYPE)
    public void registerMaterialType(String sessionToken, MaterialType entityType);

    /**
     * Updates a material type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL_TYPE)
    public void updateMaterialType(String sessionToken, EntityType entityType);

    /**
     * Creates a new sample type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE_TYPE)
    public void registerSampleType(String sessionToken, SampleType entityType);

    /**
     * Updates a sample type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE_TYPE)
    public void updateSampleType(String sessionToken, EntityType entityType);

    /**
     * Creates a new experiment type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void registerExperimentType(String sessionToken, ExperimentType entityType);

    /**
     * Updates a experiment type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void updateExperimentType(String sessionToken, EntityType entityType);

    /**
     * Creates a new file format type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.FILE_FORMAT_TYPE)
    public void registerFileFormatType(String sessionToken, FileFormatType type);

    /**
     * Creates a new data set type.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATASET_TYPE)
    public void registerDataSetType(String sessionToken, DataSetType entityType);

    /**
     * Updates a data set type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATASET_TYPE)
    public void updateDataSetType(String sessionToken, EntityType entityType);

    /**
     * Deletes/Trashes specified data sets. This method CANNOT delete data sets with deletion_disallow flag set to true in their type (compare with
     * {@link #deleteDataSetsForced(String, List, String, DeletionType, boolean)}).
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean isTrashEnabled);

    /**
     * Deletes/Trashes specified data sets. It CAN delete data sets with deletion_disallow flag set to true in their type (compare with
     * {@link #deleteDataSets(String, List, String, DeletionType, boolean)}).
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean isTrashEnabled);

    /**
     * Deletes/Trashes specified samples.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE, ObjectKind.DELETION })
    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason,
            DeletionType type);

    /**
     * Deletes/Trashes specified experiments.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT, ObjectKind.DELETION })
    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason,
            DeletionType deletionType);

    /**
     * Deletes specified attachments (all versions with given file names) of specified experiment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason);

    /**
     * Deletes specified attachments (all versions with given file names) of specified sample.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void deleteSampleAttachments(String sessionToken, TechId sampleId,
            List<String> fileNames, String reason);

    /**
     * Deletes specified attachments (all versions with given file names) of specified project.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void deleteProjectAttachments(String sessionToken, TechId projectId,
            List<String> fileNames, String reason);

    /**
     * Returns all attachments (all versions) of specified experiment.
     */
    @Transactional(readOnly = true)
    public List<Attachment> listExperimentAttachments(String sessionToken, TechId experimentId);

    /**
     * Returns all attachments (all versions) of specified sample.
     */
    @Transactional(readOnly = true)
    public List<Attachment> listSampleAttachments(String sessionToken, TechId sampleId);

    /**
     * Returns all attachments (all versions) of specified project.
     */
    @Transactional(readOnly = true)
    public List<Attachment> listProjectAttachments(String sessionToken, TechId projectId);

    /**
     * Uploads specified data sets to CIFEX server of specified URL with specified password.
     *
     * @return a message or an empty string
     */
    @Transactional
    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext);

    /**
     * Lists vocabulary terms of a given vocabulary. Includes terms usage statistics.
     */
    @Transactional(readOnly = true)
    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary);

    /**
     * Lists vocabulary terms of a given vocabulary.
     */
    @Transactional(readOnly = true)
    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary);

    /**
     * List data set types.
     *
     * @return a sorted list of {@link DataSetType}.
     */
    @Transactional(readOnly = true)
    public List<DataSetType> listDataSetTypes(String sessionToken);

    /**
     * @return Information about the time and kind of the last modification, separately for each kind of database object.
     */
    public LastModificationState getLastModificationState(String sessionToken);

    /**
     * For given {@link TechId} returns the {@link Sample} and its derived (child) samples.
     *
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample uniquely identified by given <var>sampleId</var>
     *             does not exist.
     */
    @Transactional(readOnly = true)
    public SampleParentWithDerived getSampleInfo(final String sessionToken, final TechId sampleId)
            throws UserFailureException;

    /**
     * Saves changed sample.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates);

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public Experiment getExperimentInfo(String sessionToken, ExperimentIdentifier identifier);

    /**
     * For given {@link TechId} returns the corresponding {@link Experiment}.
     */
    @Transactional(readOnly = true)
    public Experiment getExperimentInfo(String sessionToken, TechId experimentId);

    /**
     * Saves changed experiment.
     */
    @Transactional
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates);

    /**
     * For given {@link TechId} returns the corresponding {@link Project}.
     */
    @Transactional(readOnly = true)
    public Project getProjectInfo(String sessionToken, TechId projectId);

    /**
     * For given {@link ProjectIdentifier} returns the corresponding {@link Project} (without attachments).
     */
    @Transactional(readOnly = true)
    public Project getProjectInfo(String sessionToken, ProjectIdentifier projectIdentifier);

    /**
     * For given <var>project perm id</var> returns the corresponding {@link IIdHolder}.
     */
    @Transactional(readOnly = true)
    public IIdHolder getProjectIdHolder(String sessionToken, String projectPermId);

    /**
     * Returns unique code.
     */
    @Transactional
    public String generateCode(String sessionToken, String prefix, EntityKind entityKind);

    /**
     * Saves changed project.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public int updateProject(String sessionToken, ProjectUpdatesDTO updates);

    /**
     * Deletes specified data set types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATASET_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified sample types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified experiment types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified file format types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.FILE_FORMAT_TYPE })
    public void deleteFileFormatTypes(String sessionToken, List<String> codes);

    /**
     * Deletes specified material types.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.MATERIAL_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * For given {@link EntityKind} and permanent <var>identifier</var> returns the corresponding {@link IEntityInformationHolderWithPermId}.
     */
    @Transactional(readOnly = true)
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            EntityKind entityKind, String permId);

    /**
     * Returns requested entity.
     */
    @Transactional(readOnly = true)
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            BasicEntityDescription info);

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    public Material getMaterialInfo(String sessionToken, MaterialIdentifier identifier);

    /**
     * For given {@link TechId} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    public Material getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link IEntityInformationHolder}.
     */
    @Transactional(readOnly = true)
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(String sessionToken,
            MaterialIdentifier identifier);

    /**
     * Saves changed material.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version);

    /**
     * Returns file template available during batch operation of entity of given type.
     */
    @Transactional(readOnly = true)
    public String getTemplateColumns(String sessionToken, EntityKind kind, String type,
            final String format, boolean autoGenerate, boolean withExperiments, boolean withSpace,
            BatchOperationKind operationKind);

    /**
     * Updates file format type.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.FILE_FORMAT_TYPE)
    public void updateFileFormatType(String sessionToken, AbstractType type);

    /**
     * Updates the experiment attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment);

    /**
     * Adds the experiment attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void addExperimentAttachment(String sessionToken, TechId experimentId,
            NewAttachment attachment);

    /**
     * Updates the sample attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment);

    /**
     * Adds the sample attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void addSampleAttachments(String sessionToken, TechId sampleId, NewAttachment attachment);

    /**
     * Updates the project attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment);

    /**
     * Adds the project attachment.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void addProjectAttachments(String sessionToken, TechId projectId,
            NewAttachment attachment);

    /**
     * Lists all DSS server registered this openBIS server instance. Any of the returned instances could be offline at the time of the listing.
     */
    @Transactional(readOnly = true)
    public List<DataStore> listDataStores(String sessionToken);

    /** Lists all available datastore services of the specified kind */
    @Transactional(readOnly = true)
    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind);

    @Transactional(readOnly = true)
    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes);

    @Transactional(readOnly = true)
    public TableModel createReportFromDatasets(String sessionToken, String serviceKey,
            List<String> datasetCodes);

    @Transactional(readOnly = true)
    public TableModel createReportFromAggregationService(String sessionToken,
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters);

    @Transactional(readOnly = true)
    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes);

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
     * Locks data sets.
     *
     * @return number of data sets scheduled for locking.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int lockDatasets(String sessionToken, List<String> datasetCodes);

    /**
     * Unlocks data sets.
     *
     * @return number of data sets scheduled for unlocking.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int unlockDatasets(String sessionToken, List<String> datasetCodes);

    /**
     * Returns all authorization groups.
     */
    @Transactional(readOnly = true)
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken);

    /**
     * Saves changed authorization group.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates);

    /**
     * Returns all persons belonging to given authorization group.
     */
    @Transactional(readOnly = true)
    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId);

    /**
     * Adds specified persons to given authorization group.
     */
    @Transactional
    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes);

    /**
     * Removes specified persons from given authorization group.
     */
    @Transactional
    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes);

    /**
     * Lists filters available for selected grid.
     */
    @Transactional
    public List<GridCustomFilter> listFilters(String sessionToken, String gridId);

    /**
     * Creates a new filter.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void registerFilter(String sessionToken, NewColumnOrFilter filter);

    /**
     * Deletes specified filters.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void deleteFilters(String sessionToken, List<TechId> filterIds);

    /**
     * Updates a filter.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void updateFilter(String sessionToken, IExpressionUpdates updates);

    // columns

    /**
     * Creates a new column.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column);

    /**
     * Deletes specified columns.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void deleteGridCustomColumns(String sessionToken, List<TechId> columnIds);

    /**
     * Updates a column.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void updateGridCustomColumn(String sessionToken, IExpressionUpdates updates);

    /**
     * Updates vocabulary terms.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms);

    /**
     * Deletes specified materials.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason);

    /**
     * Gets the link from a service that supports the IReportingPluginTask#createLink method.
     */
    @Transactional(readOnly = true)
    public LinkModel retrieveLinkFromDataSet(String sessionToken,
            DatastoreServiceDescription serviceDescription, String dataSetCode);

    /**
     * For given {@link TechId} returns the corresponding {@link Script}.
     */
    @Transactional(readOnly = true)
    public Script getScriptInfo(String sessionToken, TechId scriptId);

    /**
     * Evaluates given script for selected entity.
     */
    @Transactional(readOnly = true)
    public String evaluate(String sessionToken, DynamicPropertyEvaluationInfo info);

    /**
     * Evaluates given script for selected entity.
     */
    @Transactional(readOnly = true)
    public String evaluate(String sessionToken, EntityValidationEvaluationInfo info);

    /**
     * Evaluates the managed property script and updates the entity.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateManagedPropertyOnExperiment(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction);

    /**
     * Evaluates the managed property script and updates the entity.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateManagedPropertyOnSample(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction);

    /**
     * Evaluates the managed property script and updates the entity.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateManagedPropertyOnDataSet(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction);

    /**
     * Evaluates the managed property script and updates the entity.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public void updateManagedPropertyOnMaterial(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction);

    /**
     * Get the default url for data store put.
     */
    @Transactional(readOnly = true)
    public String getDefaultPutDataStoreBaseURL(String sessionToken);

    /**
     * Updates properties of a data set with given id.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSetProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties);

    /**
     * Updates properties of an experiment with given id.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperimentProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties);

    /**
     * Updates properties of a sample with given id.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSampleProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties);

    /**
     * Updates properties of a material with given id.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public void updateMaterialProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties);

    /**
     * Returns all deletions.
     *
     * @return a sorted list of {@link Deletion}.
     */
    @Transactional(readOnly = true)
    public List<Deletion> listDeletions(String sessionToken, boolean withDeletedEntities);

    /**
     * Returns all deletions, but only including original entities.
     *
     * @return a sorted list of {@link Deletion}.
     */
    @Transactional(readOnly = true)
    public List<Deletion> listOriginalDeletions(String sessionToken);

    /**
     * Reverts specified deletions (puts back all entities moved to trash in the deletions).
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DELETION)
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void revertDeletions(final String sessionToken, final List<TechId> deletionIds);

    /**
     * Permanently deletes entities moved to trash in specified deletions. This method CANNOT delete data sets with deletion_disallow flag set to true
     * in their type (compare with {@link #deletePermanentlyForced(String, List)})
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void deletePermanently(final String sessionToken, final List<TechId> deletionIds);

    /**
     * Permanently deletes entities moved to trash in specified deletions. It CAN delete data sets with deletion_disallow flag set to true in their
     * type (compare with {@link #deletePermanently(String, List)}).
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void deletePermanentlyForced(final String sessionToken, final List<TechId> deletionIds);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    public List<Material> searchForMaterials(String sessionToken, DetailedSearchCriteria criteria);

    /**
     * Performs an import of file to the dss.
     */
    @Transactional
    public void performCustomImport(String sessionToken, String customImportCode,
            CustomImportFile customImportFile) throws UserFailureException;

    /**
     * Sends the e-mail containing number of active users to CISD Help desk and user, who triggered the action
     */
    @Transactional(readOnly = true)
    public void sendCountActiveUsersEmail(String sessionToken);

    /**
     * Lists all external data management systems registered within openBIS.
     */
    @Transactional(readOnly = true)
    public List<ExternalDataManagementSystem> listExternalDataManagementSystems(String sessionToken);

    /**
     * Gets external data management system specified by given code or <code>null</code> if one couldn't be found.
     */
    @Transactional(readOnly = true)
    public ExternalDataManagementSystem getExternalDataManagementSystem(String sessionToken,
            String code);

    /**
     * Registers new or updates existing external data management system.
     */
    @Transactional()
    public void createOrUpdateExternalDataManagementSystem(String sessionToken,
            ExternalDataManagementSystem edms);

    /**
     * Lists all metaprojects registered for session user.
     */
    @Transactional(readOnly = true)
    public List<Metaproject> listMetaprojects(String sessionToken);

    /**
     * Lists all metaprojects registered for given user.
     */
    @Transactional(readOnly = true)
    public List<Metaproject> listMetaprojectsOnBehalfOfUser(String sessionToken, String userId);

    /**
     * Lists all metaproject assignments counts for given user.
     */
    @Transactional(readOnly = true)
    public List<MetaprojectAssignmentsCount> listMetaprojectAssignmentsCounts(String sessionToken);

    /**
     * Get metaproject assignments counts for given user and metaproject.
     */
    @Transactional(readOnly = true)
    public MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(String sessionToken,
            IMetaprojectId metaprojectId);

    /**
     * Returns object containing all entities assigned to given metaproject.
     */
    @Transactional(readOnly = true)
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId);

    /**
     * Returns object containing all entities assigned to given metaproject of specified user.
     */
    @Transactional(readOnly = true)
    public MetaprojectAssignments getMetaprojectAssignmentsOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId);

    /**
     * Returns object containing chosen entities assigned to given metaproject.
     */
    @Transactional(readOnly = true)
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions);

    /**
     * Returns metaproject.
     */
    @Transactional(readOnly = true)
    public Metaproject getMetaproject(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Returns metaproject avoiding permission checks. Etl server only
     */
    @Transactional(readOnly = true)
    public Metaproject getMetaprojectWithoutOwnershipChecks(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Adds specified entities to given metaproject.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.METAPROJECT)
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd);

    /**
     * Removes specified entities to given metaproject.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.METAPROJECT)
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove);

    /**
     * Deletes given metaproject.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.METAPROJECT)
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId, String reason);

    /**
     * Deletes given metaprojects.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.METAPROJECT)
    public void deleteMetaprojects(String sessionToken, List<IMetaprojectId> metaprojectIds,
            String reason);

    /**
     * Registers a new metaproject.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.METAPROJECT)
    public Metaproject registerMetaproject(String sessionToken,
            IMetaprojectRegistration registration);

    /**
     * Updates existing metaprojest.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.METAPROJECT)
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            IMetaprojectUpdates updates);

    /**
     * Lists all the predeployed plugin names for given script type.
     */
    @Transactional(readOnly = true)
    public List<String> listPredeployedPlugins(String sessionToken, ScriptType scriptType);

    /**
     * Gets text for front page if the AS is disabled, null otherwise.
     */
    public String getDisabledText();
}
