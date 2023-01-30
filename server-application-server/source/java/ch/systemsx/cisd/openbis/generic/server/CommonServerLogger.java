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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectNullRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectNullUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
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
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.ICorePluginResourceLoader;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Logger class for {@link CommonServer} which creates readable logs of method invocations.
 *
 * @author Franz-Josef Elmer
 */
final class CommonServerLogger extends AbstractServerLogger implements ICommonServerForInternalUse
{
    /**
     * Creates an instance for the specified session manager, invocation status and elapsed time. The session manager is used to retrieve user
     * information which will be a part of the log message.
     */
    CommonServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // IGenericServer
    //

    @Override
    public SessionContextDTO tryToAuthenticateAsSystem()
    {
        return null;
    }

    @Override
    public List<Space> listSpaces(final String sessionToken)
    {
        final String command = "list_spaces";
        logAccess(sessionToken, command);
        return null;
    }

    @Override
    public void registerSpace(final String sessionToken, final String groupCode,
            final String descriptionOrNull)
    {
        logTracking(sessionToken, "register_space", "CODE(%s)", groupCode);
    }

    @Override
    public ScriptUpdateResult updateScript(String sessionToken, IScriptUpdates updates)
    {
        logTracking(sessionToken, "update_script", "SCRIPT(%s)", updates.getId());
        return null;
    }

    @Override
    public void updateSpace(String sessionToken, ISpaceUpdates updates)
    {
        logTracking(sessionToken, "update_space", "SPACE(%s)", updates);
    }

    @Override
    public List<Person> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    @Override
    public List<Person> listActivePersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_active_persons");
        return null;
    }

    @Override
    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    @Override
    public List<RoleAssignment> listRoleAssignments(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    @Override
    public void registerProjectRole(final String sessionToken, final RoleCode roleCode,
            final ProjectIdentifier projectIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) PROJECT(%s) GRANTEE(%s)", roleCode,
                projectIdentifier, grantee);
    }

    @Override
    public void registerSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) SPACE(%s) GRANTEE(%s)", roleCode,
                spaceIdentifier, grantee);

    }

    @Override
    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  GRANTEE(%s)", roleCode, grantee);

    }

    @Override
    public void deleteProjectRole(final String sessionToken, final RoleCode roleCode,
            final ProjectIdentifier projectIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) PROJECT(%s) GRANTEE(%s)", roleCode,
                projectIdentifier, grantee);
    }

    @Override
    public void deleteSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) SPACE(%s) GRANTEE(%s)", roleCode,
                spaceIdentifier, grantee);

    }

    @Override
    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GRANTEE(%s)", roleCode, grantee);

    }

    @Override
    public final List<SampleType> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    @Override
    public Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            String sessionToken, EntityKind entityKind, String entityTypeCode)
    {
        logAccess(sessionToken, "list_managed_input_widget_descriptions", "TYPE(%s)",
                entityTypeCode);
        return null;
    }

    @Override
    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        if (criteria.isIncludeSpace())
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) OWNERS(space=%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getSpaceCode(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else if (criteria.isIncludeInstance())
        {
            logAccess(
                    sessionToken,
                    "list_samples",
                    "TYPE(%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getContainerSampleIds(),
                    criteria.getParentSampleId(), criteria.getChildSampleId(),
                    criteria.getExperimentId());
        }
        return null;
    }

    @Override
    public List<Sample> listMetaprojectSamples(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "list_metaproject_samples", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public List<Sample> listSamplesOnBehalfOfUser(String sessionToken, ListSampleCriteria criteria,
            String userId)
    {
        if (criteria.isIncludeSpace())
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) OWNERS(space=%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getSpaceCode(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else if (criteria.isIncludeInstance())
        {
            logAccess(
                    sessionToken,
                    "list_samples",
                    "TYPE(%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getContainerSampleIds(),
                    criteria.getParentSampleId(), criteria.getChildSampleId(),
                    criteria.getExperimentId());
        }
        return null;
    }

    @Override
    public List<Sample> listSamplesByMaterialProperties(String sessionToken, Collection<TechId> materialIds)
    {
        logAccess(sessionToken, "list_samples_by_material_properties", "IDS(%s)", materialIds);
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                abbreviate(propertyCodes));
        return null;
    }

    public final SampleParentWithDerivedDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    @Override
    public final List<AbstractExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "list_sample_external_data", "ID(%s) DIRECT(%s)", sampleId,
                showOnlyDirectlyConnected);
        return null;
    }

    @Override
    public List<AbstractExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId, boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "list_experiment_external_data", "ID(%s) DIRECT(%s)", experimentId,
                showOnlyDirectlyConnected);
        return null;
    }

    @Override
    public List<AbstractExternalData> listMetaprojectExternalData(final String sessionToken,
            final IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "list_metaproject_external_data", "METAPROJECT_ID(%s)",
                metaprojectId);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSetRelationships(String sessionToken,
            TechId datasetId, DataSetRelationshipRole role)
    {
        logAccess(sessionToken, "list_dataset_relationships", "ID(%s), ROLE(%s)", datasetId, role);
        return null;
    }

    @Override
    public final List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode, int maxSIze)
    {
        logAccess(sessionToken, "list_matching_entities",
                "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s) WILDCARD_MODE(%s) MAX_SIZE(%s)",
                abbreviate(searchableEntities), queryText, useWildcardSearchMode, maxSIze);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)",
                newSample.getSampleType(), newSample.getIdentifier());
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, ExperimentType experimentType, ProjectIdentifier project)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECT(%s)", experimentType, project);
        return null;
    }

    @Override
    public List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final List<ProjectIdentifier> projects)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECTS(%s)", experimentType, abbreviate(projects));
        return null;
    }

    @Override
    public List<Experiment> listMetaprojectExperiments(String sessionToken,
            IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "list_metaproject_experiments", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingSamples(final String sessionToken,
            final ExperimentType experimentType, final List<ProjectIdentifier> projects)
    {
        logAccess(sessionToken, "list_experiments_having_samples", "TYPE(%s) PROJECTS(%s)",
                experimentType, abbreviate(projects));
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingDataSets(final String sessionToken,
            final ExperimentType experimentType, final List<ProjectIdentifier> projects)
    {
        logAccess(sessionToken, "list_experiments_data_sets", "TYPE(%s) PROJECTS(%s)",
                experimentType, abbreviate(projects));
        return null;
    }

    @Override
    public List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final SpaceIdentifier space)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) SPACE(%s)", experimentType, space);
        return null;
    }

    @Override
    public List<Project> listProjects(final String sessionToken)
    {
        logAccess(sessionToken, "list_projects");
        return null;
    }

    @Override
    public List<ExperimentType> listExperimentTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_experiment_types");
        return null;
    }

    @Override
    public List<PropertyType> listPropertyTypes(final String sessionToken, boolean withRelations)
    {
        logAccess(sessionToken, "list_property_types", withRelations ? "WITH_RELATIONS" : "");
        return null;
    }

    @Override
    public List<EntityHistory> listEntityHistory(String sessionToken, EntityKind entityKind,
            TechId entityID)
    {
        logAccess(sessionToken, "list_entity_history", "ENTITY_KIND(%s) ENTITY_ID(%s)", entityKind,
                entityID);
        return null;
    }

    @Override
    public final List<DataType> listDataTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_data_types");
        return null;
    }

    @Override
    public final List<Script> listScripts(final String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        logAccess(sessionToken, "list_scripts", "SCRIPT_TYPE(%s) ENTITY_KIND(%s)",
                scriptTypeOrNull, entityKindOrNull);
        return null;
    }

    @Override
    public List<FileFormatType> listFileFormatTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_file_format_types");
        return null;
    }

    @Override
    public final List<Vocabulary> listVocabularies(final String sessionToken, boolean withTerms,
            boolean excludeInternal)
    {
        logAccess(sessionToken, "list_vocabularies");
        return null;
    }

    @Override
    public String assignPropertyType(final String sessionToken, NewETPTAssignment assignment)
    {
        final String entityTypeFormat = assignment.getEntityKind().name() + "_TYPE(%S)";
        logTracking(sessionToken, "assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                assignment.getPropertyTypeCode(), assignment.getEntityTypeCode(),
                assignment.isMandatory(), assignment.getDefaultValue(), assignment.getSection(),
                assignment.getOrdinal());
        return null;
    }

    @Override
    public void updatePropertyTypeAssignment(String sessionToken,
            NewETPTAssignment assignmentUpdates)
    {
        final String entityTypeFormat = assignmentUpdates.getEntityKind().name() + "_TYPE(%S)";
        logTracking(sessionToken, "update_property_type_assignment", " PROPERTY_TYPE(%S) "
                + entityTypeFormat + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                assignmentUpdates.getPropertyTypeCode(), assignmentUpdates.getEntityTypeCode(),
                assignmentUpdates.isMandatory(), assignmentUpdates.getDefaultValue(),
                assignmentUpdates.getSection(), assignmentUpdates.getOrdinal());
    }

    @Override
    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "unassign_property_type", " PROPERTY_TYPE(%S) "
                + entityTypeFormat, propertyTypeCode, entityTypeCode);
    }

    @Override
    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logAccess(sessionToken, "count_property_typed_entities", "PROPERTY_TYPE(%s) "
                + entityTypeFormat, propertyTypeCode, entityTypeCode);
        return 0;
    }

    @Override
    public final String registerEntitytypeAndAssignPropertyTypes(final String sessionToken, final NewETNewPTAssigments newETNewPTAssigments)
    {
        for (NewPTNewAssigment newAssigment : newETNewPTAssigments.getAssigments())
        {
            final String entityTypeFormat = newAssigment.getAssignment().getEntityKind().name() + "_TYPE(%S)";
            logTracking(sessionToken, "register_assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                    + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                    newAssigment.getAssignment().getPropertyTypeCode(), newAssigment.getAssignment().getEntityTypeCode(),
                    newAssigment.getAssignment().isMandatory(), newAssigment.getAssignment().getDefaultValue(), newAssigment.getAssignment()
                            .getSection(),
                    newAssigment.getAssignment().getOrdinal());
        }
        return null;
    }

    @Override
    public final String updateEntitytypeAndPropertyTypes(final String sessionToken, final NewETNewPTAssigments newETNewPTAssigments)
    {
        for (NewPTNewAssigment newAssigment : newETNewPTAssigments.getAssigments())
        {
            final String entityTypeFormat = newAssigment.getAssignment().getEntityKind().name() + "_TYPE(%S)";
            logTracking(sessionToken, "update_assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                    + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                    newAssigment.getAssignment().getPropertyTypeCode(), newAssigment.getAssignment().getEntityTypeCode(),
                    newAssigment.getAssignment().isMandatory(), newAssigment.getAssignment().getDefaultValue(), newAssigment.getAssignment()
                            .getSection(),
                    newAssigment.getAssignment().getOrdinal());
        }
        return null;
    }

    @Override
    public final String registerAndAssignPropertyType(final String sessionToken, final PropertyType propertyType, NewETPTAssignment assignment)
    {
        final String entityTypeFormat = assignment.getEntityKind().name() + "_TYPE(%S)";
        logTracking(sessionToken, "register_assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                assignment.getPropertyTypeCode(), assignment.getEntityTypeCode(),
                assignment.isMandatory(), assignment.getDefaultValue(), assignment.getSection(),
                assignment.getOrdinal());
        return null;
    }

    @Override
    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        logTracking(sessionToken, "register_property_type", "PROPERTY_TYPE(%s)",
                propertyType.getCode());
    }

    @Override
    public void updatePropertyType(String sessionToken, IPropertyTypeUpdates updates)
    {
        logTracking(sessionToken, "update_property_type", "PROPERTY_TYPE(%s)", updates);
    }

    @Override
    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        logTracking(sessionToken, "register_vocabulary", "VOCABULARY(%s)", vocabulary.getCode());
    }

    @Override
    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates)
    {
        logTracking(sessionToken, "update_vocabulary", "ID(%s) CODE(%s)", updates.getId(),
                updates.getCode());
    }

    @Override
    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> vocabularyTerms, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_vocabulary_terms",
                "ID(%s) TERMS(%s) PREVIOUS_ORDINAL(%s)",
                vocabularyId, abbreviate(vocabularyTerms), previousTermOrdinal);
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_unofficial_vocabulary_terms",
                "ID(%s) CODE(%s), LABEL(%s), DESCRIPTION(%s), PREVIOUS_ORDINAL(%s)", vocabularyId,
                code, label, description, previousTermOrdinal);
    }

    @Override
    public void updateVocabularyTerm(String sessionToken, IVocabularyTermUpdates updates)
    {
        logTracking(sessionToken, "update_vocabulary_term", "VOCABULARY_TERM(%s)", updates);
    }

    @Override
    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        logTracking(sessionToken, "delete_vocabulary_terms",
                "VOCABULARY_ID(%s) DELETED(%s) REPLACEMENTS(%s)", vocabularyId,
                abbreviate(termsToBeDeleted), abbreviate(termsToBeReplaced));
    }

    @Override
    public void makeVocabularyTermsOfficial(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial)
    {
        logTracking(sessionToken, "make_vocabulary_terms_official",
                "VOCABULARY_ID(%s) OFFICIAL(%s)", vocabularyId, abbreviate(termsToBeOfficial));
    }

    @Override
    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_project", "PROJECT(%s) ATTACHMNETS(%s)",
                projectIdentifier, abbreviate(attachments));
    }

    @Override
    public List<Experiment> searchForExperiments(String sessionToken,
            DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_experiments", "criteria(%s)", criteria);
        return null;
    }

    @Override
    public List<SearchDomainSearchResultWithFullEntity> searchOnSearchDomain(String sessionToken,
            String preferredSearchDomainOrNull, String searchString, Map<String, String> optionalParametersOrNull)
    {
        logAccess(sessionToken, "search_on_search_domain", "preferred_search_domain(%s) seerch_string(%s)",
                preferredSearchDomainOrNull, searchString);
        return null;
    }

    @Override
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        logAccess(sessionToken, "list_available_search_domains");
        return null;
    }

    @Override
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_datasets", "criteria(%s)", criteria);
        return null;
    }

    @Override
    public List<AbstractExternalData> searchForDataSetsOnBehalfOfUser(String sessionToken,
            DetailedSearchCriteria criteria, String userId)
    {
        logAccess(sessionToken, "search_for_datasets", "criteria(%s) user_id(%s)", criteria, userId);
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_samples", "criteria(%s)", criteria);
        return null;
    }

    @Override
    public AbstractExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getDataSetInfo", "datasetId(%s)", datasetId.getId());
        return null;
    }

    @Override
    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        logTracking(sessionToken, "updateDataSet", "DATA_SET(%s)", updates.getDatasetId());
        return null;
    }

    @Override
    public List<AbstractExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities entities, boolean withDetails)
    {
        logAccess(sessionToken, "list_related_datasets", "WITH_DETAILS(%s)", withDetails);
        return null;
    }

    @Override
    public List<AbstractExternalData> listRelatedDataSetsOnBehalfOfUser(String sessionToken,
            DataSetRelatedEntities entities, boolean withDetails, String userId)
    {
        logAccess(sessionToken, "list_related_datasets_on_behalf_of_user", "WITH_DETAILS(%s)",
                "WITH_USER(%s)", withDetails, userId);
        return null;
    }

    @Override
    public List<MaterialType> listMaterialTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_material_types");
        return null;
    }

    @Override
    public MaterialType getMaterialType(String sessionToken, String code)
    {
        logAccess(sessionToken, "get_material_type", "CODE(%s)", code);
        return null;
    }

    public List<Material> listMaterials(String sessionToken, MaterialType materialType,
            boolean withProperties)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s) withProperties(%s)", materialType,
                withProperties);
        return null;
    }

    @Override
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s) IDS(%s) withProperties(%s)",
                criteria.tryGetMaterialType(), criteria.tryGetMaterialIds() == null ? "-"
                        : abbreviate(criteria.tryGetMaterialIds()),
                withProperties);
        return null;
    }

    @Override
    public Collection<TechId> listMaterialIdsByMaterialProperties(String sessionToken, Collection<TechId> materialIds)
    {
        logAccess(sessionToken, "list_material_ids_by_material_properties", "IDS(%s)", materialIds);
        return null;
    }

    @Override
    public List<Material> listMetaprojectMaterials(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "list_metaproject_materials", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        logTracking(sessionToken, "register_material_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void updateMaterialType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_material_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        logTracking(sessionToken, "register_sample_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void updateSampleType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_sample_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        logTracking(sessionToken, "register_experiment_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void updateExperimentType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_experiment_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void registerFileFormatType(String sessionToken, FileFormatType type)
    {
        logTracking(sessionToken, "register_file_format_type", "CODE(%s)", type.getCode());
    }

    @Override
    public void registerDataSetType(String sessionToken, DataSetType entityType)
    {
        logTracking(sessionToken, "register_data_set_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void updateDataSetType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_data_set_type", "CODE(%s)", entityType.getCode());
    }

    @Override
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean isTrashEnabled)
    {
        logTracking(sessionToken, "delete_data_sets", "TYPE(%s) CODES(%s) REASON(%s)", type,
                abbreviate(dataSetCodes), reason);
    }

    @Override
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean isTrashEnabled)
    {
        logTracking(sessionToken, "delete_data_sets_forced", "TYPE(%s) CODES(%s) REASON(%s)", type,
                abbreviate(dataSetCodes), reason);
    }

    @Override
    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason,
            DeletionType deletionType)
    {
        logTracking(sessionToken, "delete_samples", "TYPE(%s) IDS(%s) REASON(%s)", deletionType,
                abbreviate(sampleIds), reason);
    }

    @Override
    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason,
            DeletionType deletionType)
    {
        logTracking(sessionToken, "delete_experiments", "TYPE(%s) IDS(%s) REASON(%s)",
                deletionType, abbreviate(experimentIds), reason);
    }

    @Override
    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason)
    {
        logTracking(sessionToken, "delete_vocabularies", "IDS(%s) REASON(%s)",
                abbreviate(vocabularyIds), reason);
    }

    @Override
    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason)
    {
        logTracking(sessionToken, "delete_property_types", "IDS(%s) REASON(%s)",
                abbreviate(propertyTypeIds), reason);
    }

    @Override
    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason)
    {
        logTracking(sessionToken, "delete_projects", "IDS(%s) REASON(%s)", abbreviate(projectIds),
                reason);
    }

    @Override
    public void deleteSpaces(String sessionToken, List<TechId> groupIds, String reason)
    {
        logTracking(sessionToken, "delete_spaces", "IDS(%s) REASON(%s)", abbreviate(groupIds),
                reason);
    }

    @Override
    public void deleteScripts(String sessionToken, List<TechId> scriptIds)
    {
        logTracking(sessionToken, "delete_scripts", "IDS(%s)", abbreviate(scriptIds));
    }

    @Override
    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_experiment_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    @Override
    public void deleteSampleAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_sample_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    @Override
    public void deleteProjectAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_project_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    @Override
    public List<Attachment> listExperimentAttachments(String sessionToken, TechId experimentId)
    {
        logAccess(sessionToken, "list_experiment_attachments", "ID(%s)", experimentId);
        return null;
    }

    @Override
    public List<Attachment> listSampleAttachments(String sessionToken, TechId sampleId)
    {
        logAccess(sessionToken, "list_sample_attachments", "ID(%s)", sampleId);
        return null;
    }

    @Override
    public List<Attachment> listProjectAttachments(String sessionToken, TechId projectId)
    {
        logAccess(sessionToken, "list_project_attachments", "ID(%s)", projectId);
        return null;
    }

    @Override
    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        logTracking(sessionToken, "upload_data_sets", "CODES(%s) CIFEX-URL(%s) FILE(%s)",
                dataSetCodes, uploadContext.getCifexURL(), uploadContext.getFileName());
        return null;
    }

    @Override
    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms_with_statistics", "VOCABULARY(%s)",
                vocabulary.getCode());
        return null;
    }

    @Override
    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms", "VOCABULARY(%s)", vocabulary.getCode());
        return null;
    }

    @Override
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_data_set_types");
        return null;
    }

    @Override
    public LastModificationState getLastModificationState(String sessionToken)
    {
        return null;
    }

    @Override
    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    @Override
    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_sample",
                "SAMPLE(%s) CHANGE_TO_PROJECT(%s) CHANGE_TO_EXPERIMENT(%s) ATTACHMENTS(%s)",
                updates.getSampleIdOrNull(), updates.getProjectIdentifier(),
                updates.getExperimentIdentifierOrNull(), updates.getAttachments().size());
        return null;
    }

    @Override
    public Experiment getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        logAccess(sessionToken, "get_experiment_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    @Override
    public Experiment getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        logAccess(sessionToken, "get_experiment_info", "ID(%s)", experimentId);
        return null;
    }

    @Override
    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        logTracking(sessionToken, "update_experiment", "EXPERIMENT(%s)", updates.getExperimentId());
        return null;
    }

    @Override
    public IIdHolder getProjectIdHolder(String sessionToken, String projectPermId)
    {
        logAccess(sessionToken, "get_project_id_holder", "PERM_ID(%s)", projectPermId);
        return null;
    }

    @Override
    public Project getProjectInfo(String sessionToken, TechId projectId)
    {
        logAccess(sessionToken, "get_project_info", "ID(%s)", projectId);
        return null;
    }

    @Override
    public Project getProjectInfo(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        logAccess(sessionToken, "get_project_info", "IDENTIFIER(%s)", projectIdentifier);
        return null;
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, String permId)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "get_entity_information_holder", entityTypeFormat
                + " PERM_ID(%S) ", entityKind, permId);
        return null;
    }

    @Override
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(String sessionToken,
            MaterialIdentifier identifier)
    {
        logTracking(sessionToken, "get_material_information_holder", " IDENTIFIER(%S) ", identifier);
        return null;
    }

    @Override
    public Material getMaterialInfo(String sessionToken, MaterialIdentifier identifier)
    {
        logTracking(sessionToken, "getMaterialInfo", " IDENTIFIER(%S) ", identifier);
        return null;
    }

    @Override
    public Material getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        logAccess(sessionToken, "get_material_info", "ID(%s)", materialId);
        return null;
    }

    @Override
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", materialId);
        return null;
    }

    @Override
    public String generateCode(String sessionToken, String prefix, EntityKind entityKind)
    {
        logAccess(sessionToken, "generate_code", "PREFIX(%s) ENTITY_KIND(%s)", prefix, entityKind);
        return null;
    }

    @Override
    public int updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_project", "PROJECT_ID(%s) ATTACHMENTS_ADDED(%s)",
                updates.getTechId(), updates.getAttachments().size());
        return 0;
    }

    @Override
    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_data_set_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    @Override
    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_experiment_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    @Override
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_material_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    @Override
    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_sample_types", "CODES(%s)", abbreviate(entityTypesCodes));
    }

    @Override
    public String getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            final String format, boolean autoGenerate, boolean withExperiments, boolean withSpace,
            BatchOperationKind operationKind)
    {
        logAccess(sessionToken, "get_template_columns",
                "ENTITY_KIND(%s) ENTITY_TYPE(%s) FORMAT(%s) AUTO_GENERATE(%s) WITH_EXP(%s) "
                        + "WITH_SPACE(%s) OPERATION(%s)",
                entityKind, type, format, autoGenerate,
                withExperiments, withSpace, operationKind.getDescription());
        return null;
    }

    @Override
    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
    {
        logTracking(sessionToken, "delete_file_format_types", "CODES(%s)", abbreviate(codes));
    }

    @Override
    public void updateFileFormatType(String sessionToken, AbstractType type)
    {
        logTracking(sessionToken, "update_file_format_type", "CODE(%s)", type.getCode());

    }

    @Override
    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment)
    {
        logTracking(sessionToken, "update_experiment_attachment",
                "EXPERIMENT_ID(%s) ATTACHMENT(%s)", experimentId, attachment.getFileName());
    }

    @Override
    public void addExperimentAttachment(String sessionToken, TechId experimentId,
            NewAttachment attachment)
    {
        logTracking(sessionToken, "add_experiment_attachment", "EXPERIMENT_ID(%s) ATTACHMENT(%s)",
                experimentId, attachment.getFileName());
    }

    @Override
    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment)
    {
        logTracking(sessionToken, "update_project_attachment", "PROJECT_ID(%s) ATTACHMENT(%s)",
                projectId, attachment.getFileName());
    }

    @Override
    public void addProjectAttachments(String sessionToken, TechId projectId,
            NewAttachment attachment)
    {
        logTracking(sessionToken, "add_project_attachment", "PROJECT_ID(%s) ATTACHMENT(%s)",
                projectId, attachment.getFileName());
    }

    @Override
    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment)
    {
        logTracking(sessionToken, "update_sample_attachment", "SAMPLE_ID(%s) ATTACHMENT(%s)",
                sampleId, attachment.getFileName());
    }

    @Override
    public void addSampleAttachments(String sessionToken, TechId sampleId, NewAttachment attachment)
    {
        logTracking(sessionToken, "add_sample_attachment", "SAMPLE_ID(%s) ATTACHMENT(%s)",
                sampleId, attachment.getFileName());
    }

    @Override
    public List<DataStore> listDataStores(String sessionToken)
    {
        logAccess(sessionToken, "listDataStores");
        return null;
    }

    @Override
    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind)
    {
        logAccess(sessionToken, "listDataStoreServices");
        return null;
    }

    @Override
    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        logAccess(sessionToken, "createReportFromDatasets", "SERVICE(%s), DATASETS(%s)",
                serviceDescription, abbreviate(datasetCodes));
        return null;
    }

    @Override
    public TableModel createReportFromDatasets(String sessionToken, String serviceKey,
            List<String> datasetCodes)
    {
        logAccess(sessionToken, "createReportFromDatasets", "SERVICE(%s), DATASETS(%s)",
                serviceKey, abbreviate(datasetCodes));
        return null;
    }

    @Override
    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        logTracking(sessionToken, "processDatasets", "SERVICE(%s), DATASETS(%s)",
                serviceDescription, abbreviate(datasetCodes));
    }

    @Override
    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        logTracking(sessionToken, "registerAuthorizationGroup", "CODE(%s)",
                newAuthorizationGroup.getCode());

    }

    @Override
    public void registerScript(String sessionToken, Script script)
    {
        logTracking(sessionToken, "registerScript", "NAME(%s)", script.getName());

    }

    @Override
    public void deleteAuthorizationGroups(String sessionToken, List<TechId> authGroupIds,
            String reason)
    {
        logTracking(sessionToken, "deleteAuthorizationGroups", "TECH_IDS(%s)",
                abbreviate(authGroupIds));
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        logAccess(sessionToken, "listAuthorizatonGroups");
        return null;
    }

    @Override
    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates)
    {
        logTracking(sessionToken, "updateAuthorizationGroup", "TECH_ID(%s)", updates.getId());
        return null;
    }

    @Override
    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        logAccess(sessionToken, "listPersonInAuthorizationGroup", "ID(%s)", authorizatonGroupId);
        return null;
    }

    @Override
    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes)
    {
        logTracking(sessionToken, "addPersonsToAuthorizationGroup", "TECH_ID(%s) PERSONS(%s)",
                authorizationGroupId, abbreviate(personsCodes));
    }

    @Override
    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        logTracking(sessionToken, "removePersonsFromAuthorizationGroup", "TECH_ID(%s) PERSONS(%s)",
                authorizationGroupId, abbreviate(personsCodes));
    }

    @Override
    public List<GridCustomFilter> listFilters(String sessionToken, String gridId)
    {
        logAccess(sessionToken, "listFilters", "GRID(%s)", gridId);
        return null;
    }

    @Override
    public void registerFilter(String sessionToken, NewColumnOrFilter filter)
    {
        logTracking(sessionToken, "registerFilter", "FILTER(%s)", filter);
    }

    @Override
    public void deleteFilters(String sessionToken, List<TechId> filterIds)
    {
        logTracking(sessionToken, "deleteFilters", "TECH_IDS(%s)", abbreviate(filterIds));
    }

    @Override
    public void updateFilter(String sessionToken, IExpressionUpdates updates)
    {
        logTracking(sessionToken, "updateFilters", "ID(%s) NAME(%s)", updates.getId(),
                updates.getName());
    }

    // -- columns

    @Override
    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column)
    {
        logTracking(sessionToken, "registerGridCustomColumn", "COLUMN(%s)", column);
    }

    @Override
    public void deleteGridCustomColumns(String sessionToken, List<TechId> columnIds)
    {
        logTracking(sessionToken, "deleteGridCustomColumns", "TECH_IDS(%s)", abbreviate(columnIds));
    }

    @Override
    public void updateGridCustomColumn(String sessionToken, IExpressionUpdates updates)
    {
        logTracking(sessionToken, "updateGridCustomColumn", "ID(%s) NAME(%s)", updates.getId(),
                updates.getName());
    }

    @Override
    public void keepSessionAlive(String sessionToken)
    {
        logAccess(Level.DEBUG, sessionToken, "keepSessionAlive", "TOKEN(%s)", sessionToken);
    }

    @Override
    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms)
    {
        logTracking(sessionToken, "update_vocabulary_terms", "VOCABULARY_TERMS(%s) VOCABULARY(%s)",
                abbreviate(terms), vocabularyId);
    }

    @Override
    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason)
    {
        logTracking(sessionToken, "delete_materials", "IDS(%s) REASON(%s)",
                abbreviate(materialIds), reason);
    }

    @Override
    public int lockDatasets(String sessionToken, List<String> datasetCodes)
    {
        logTracking(sessionToken, "lockDatasets", "DATASETS(%s)", abbreviate(datasetCodes));
        return 0;
    }

    @Override
    public int unlockDatasets(String sessionToken, List<String> datasetCodes)
    {
        logTracking(sessionToken, "unlockDatasets", "DATASETS(%s)", abbreviate(datasetCodes));
        return 0;
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String sessionToken,
            DatastoreServiceDescription serviceDescription, String dataSetCode)
    {
        {
            logAccess(sessionToken, "retrieveLinkFromDataSet", "SERVICE(%s), DATASET(%s)",
                    serviceDescription, dataSetCode);
            return null;
        }
    }

    @Override
    public Script getScriptInfo(String sessionToken, TechId scriptId)
    {
        logAccess(sessionToken, "getScriptInfo", "SCRIPT(%s)", scriptId);
        return null;
    }

    @Override
    public String evaluate(String sessionToken, DynamicPropertyEvaluationInfo info)
    {
        logAccess(sessionToken, "evaluate", "%s(%s)", info.getEntityKind().name(),
                info.getEntityIdentifier());
        return null;
    }

    @Override
    public String evaluate(String sessionToken, EntityValidationEvaluationInfo info)
    {
        logAccess(sessionToken, "evaluate", "%s(%s)", info.getEntityKind().name(),
                info.getEntityIdentifier());
        return null;
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            BasicEntityDescription info)
    {
        logAccess(sessionToken, "getEntityInformationHolder", "KIND(%s) IDENTIFIER(%s)",
                info.getEntityKind(), info.getEntityIdentifier());
        return null;
    }

    @Override
    public void updateManagedPropertyOnExperiment(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnExperiment",
                "ID(%s) PROPERTY(%s) ACTION(%s)", experimentId,
                managedProperty.getPropertyTypeCode(), updateAction.getName());
    }

    @Override
    public void updateManagedPropertyOnSample(String sessionToken, TechId sampleId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnSample",
                "ID(%s) PROPERTY(%s) ACTION(%s)", sampleId, managedProperty.getPropertyTypeCode(),
                updateAction.getName());
    }

    @Override
    public void updateManagedPropertyOnDataSet(String sessionToken, TechId dataSetId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnDataSet",
                "ID(%s) PROPERTY(%s) ACTION(%s)", dataSetId, managedProperty.getPropertyTypeCode(),
                updateAction.getName());
    }

    @Override
    public void updateManagedPropertyOnMaterial(String sessionToken, TechId materialId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnMaterial",
                "ID(%s) PROPERTY(%s) ACTION(%s)", materialId,
                managedProperty.getPropertyTypeCode(), updateAction.getName());
    }

    @Override
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "getDefaultPutDataStoreBaseURL");
        return null;
    }

    @Override
    public void updateDataSetProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateDataSetProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    @Override
    public void updateExperimentProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateExperimentProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    @Override
    public void updateSampleProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateSampleProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    @Override
    public void updateMaterialProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)

    {
        logTracking(sessionToken, "updateMaterialProperty", "ID(%s), MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken,
            List<ExperimentIdentifier> experimentIdentifiers)
    {
        logTracking(sessionToken, "listExperiments", "experimentIdentifiers(%s)",
                abbreviate(experimentIdentifiers));
        return null;
    }

    @Override
    public List<Deletion> listDeletions(String sessionToken, boolean withDeletedEntities)
    {
        logAccess(sessionToken, "listDeletions", "WITH_ENTITIES(%s)", withDeletedEntities);
        return null;
    }

    @Override
    public List<Deletion> listOriginalDeletions(String sessionToken)
    {
        logAccess(sessionToken, "listOriginalDeletions");
        return null;
    }

    @Override
    public void revertDeletions(String sessionToken, List<TechId> deletionIds)
    {
        logTracking(sessionToken, "revertDeletions", "ID(%s)", abbreviate(deletionIds));
    }

    @Override
    public void deletePermanently(String sessionToken, List<TechId> deletionIds, boolean forceToDeleteDependentDeletionSets)
    {
        logTracking(sessionToken, "deletePermanently", "ID(%s) FORCE_TO_DELETE_DEPENDENT_DELETION_SETS(%s)",
                abbreviate(deletionIds), forceToDeleteDependentDeletionSets);
    }

    @Override
    public void deletePermanentlyForced(String sessionToken, List<TechId> deletionIds, boolean forceToDeleteDependentDeletionSets)
    {
        logTracking(sessionToken, "deletePermanentlyForced", "ID(%s) FORCE_TO_DELETE_DEPENDENT_DELETION_SETS(%s)",
                abbreviate(deletionIds), forceToDeleteDependentDeletionSets);
    }

    @Override
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(String sessionToken)
    {
        logTracking(sessionToken, "listEntityTypePropertyTypes", "");
        return null;
    }

    @Override
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(String sessionToken,
            EntityType entityType)
    {
        logTracking(sessionToken, "listEntityTypePropertyTypes", "ENTITY_TYPE(%s)",
                entityType != null ? entityType.getCode() : null);
        return null;
    }

    @Override
    public void registerPlugin(String sessionToken, CorePlugin plugin,
            ICorePluginResourceLoader resourceLoader)
    {
        logTracking(sessionToken, "registerPlugin", "PLUGIN(%s), LOADER(%s)", plugin,
                resourceLoader);
    }

    @Override
    public List<DataStore> listDataStores()
    {
        logTracking("internal_call", "listDataStores", "");
        return null;
    }

    @Override
    public List<Material> searchForMaterials(String sessionToken, DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_materials", "criteria(%s)", criteria);
        return null;
    }

    @Override
    public TableModel createReportFromAggregationService(String sessionToken,
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters)
    {
        logAccess(sessionToken, "createReportFromAggregationService",
                "SERVICE(%s), PARAMETERS(%s)", serviceDescription, parameters != null ? parameters.keySet() : "[]");
        return null;
    }

    @Override
    public void performCustomImport(String sessionToken, String customImportCode,
            CustomImportFile customImportFile)
    {
        logAccess(sessionToken, "performCustomImport",
                "CUSTOM_IMPORT_CODE(%s), CUSTOM_IMPORT_FILE(%s)", customImportCode,
                customImportFile);
    }

    @Override
    public void sendCountActiveUsersEmail(String sessionToken)
    {
        logAccess(sessionToken, "sendCountActiveUsersEmail", "");
    }

    @Override
    public List<ExternalDataManagementSystem> listExternalDataManagementSystems(String sessionToken)
    {
        logAccess(sessionToken, "listExternalDataManagementSystems", "");

        return null;
    }

    @Override
    public ExternalDataManagementSystem getExternalDataManagementSystem(String sessionToken,
            String code)
    {
        logAccess(sessionToken, "getExternalDataManagementSystem", "");

        return null;
    }

    @Override
    public void createOrUpdateExternalDataManagementSystem(String sessionToken,
            ExternalDataManagementSystem edms)
    {
        logAccess(sessionToken, "createOrUpdateExternalDataManagementSystem CODE(%s)",
                edms.getCode());

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
    public List<MetaprojectAssignmentsCount> listMetaprojectAssignmentsCounts(String sessionToken)
    {
        logAccess(sessionToken, "listMetaprojectAssignmentsCounts");
        return null;
    }

    @Override
    public MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(String sessionToken,
            IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "getMetaprojectAssignmentsCount", "METAPROJECT_ID(%s)",
                metaprojectId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "getMetaprojectAssignments",
                "METAPROJECT_ID(%s) FETCH_OPTIONS(%s)", metaprojectId, fetchOptions);
        return null;
    }

    @Override
    public Metaproject getMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "getMetaproject", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public Metaproject getMetaprojectWithoutOwnershipChecks(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "getMetaprojectInGodMode", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "getMetaprojectAssignments", "METAPROJECT_ID(%s)", metaprojectId);
        return null;
    }

    @Override
    public MetaprojectAssignments getMetaprojectAssignmentsOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId)
    {
        logAccess(sessionToken, "getMetaprojectAssignmentsOnBehalfOfUser",
                "METAPROJECT_ID(%s) USER(%s)", metaprojectId, userId);
        return null;
    }

    @Override
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToAdd != null ? assignmentsToAdd : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "addToMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToRemove != null ? assignmentsToRemove : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "removeFromMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId, String reason)
    {
        logAccess(sessionToken, "deleteMetaproject", "METAPROJECT_ID(%s)", metaprojectId);
    }

    @Override
    public void deleteMetaprojects(String sessionToken, List<IMetaprojectId> metaprojectIds,
            String reason)
    {
        logTracking(sessionToken, "deleteMetaprojects", "METAPROJECT_IDS(%s) REASON(%s)",
                abbreviate(metaprojectIds), reason);
    }

    @Override
    public Metaproject registerMetaproject(String sessionToken,
            IMetaprojectRegistration registration)
    {
        IMetaprojectRegistration notNullRegistration;

        if (registration == null)
        {
            notNullRegistration = new MetaprojectNullRegistration();
        } else
        {
            notNullRegistration = registration;
        }

        logAccess(sessionToken, "registerMetaproject", "NAME(%s) DESCRIPTION(%s)",
                notNullRegistration.getName(), notNullRegistration.getDescription());
        return null;
    }

    @Override
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            IMetaprojectUpdates updates)
    {
        IMetaprojectUpdates notNullUpdate;

        if (updates == null)
        {
            notNullUpdate = new MetaprojectNullUpdates();
        } else
        {
            notNullUpdate = updates;
        }

        logAccess(sessionToken, "updateMetaproject", "METAPROJECT_ID(%s) NAME(%s) DESCRIPTION(%s)",
                metaprojectId, notNullUpdate.getName(), notNullUpdate.getDescription());
        return null;
    }

    @Override
    public List<String> listPredeployedPlugins(String sessionToken, ScriptType scriptType)
    {
        logAccess(sessionToken, "list_predeployed_plugins", "SCRIPT_TYPE(%s)", scriptType);
        return null;
    }

    @Override
    public void registerOrUpdatePredeployedPlugin(String sessionToken, Script script)
    {
        logAccess(sessionToken, "register_or_update_predeployed_plugin", "NAME(%s)",
                script.getName());
    }

    @Override
    public void invalidatePredeployedPlugin(String sessionToken, String name, ScriptType scriptType)
    {
        logAccess(sessionToken, "invalidate_predeployed_plugin", "NAME(%s)", name);
    }

    @Override
    public String getDisabledText()
    {
        logAccess(null, "getDisabledText");
        return null;
    }
}
