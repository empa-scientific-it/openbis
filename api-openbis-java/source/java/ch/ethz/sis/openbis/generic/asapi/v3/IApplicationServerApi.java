/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.PersonDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.AggregationService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ProcessingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.AggregationServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ProcessingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ReportingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.SearchDomainServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.AggregationServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ProcessingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ReportingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * V3 application server API. Detailed documentation on how to use the API together code examples in both Java and Javascript can be found at "openBIS
 * V3 API" openBIS WIKI page.
 * <p>
 * The required access rights of the methods are the default ones. They can be configured with a capability-role map. For more details see
 * "Installation and Administrator Guide of the openBIS Server" openBIS WIKI page.
 *
 * @author pkupczyk
 */
public interface IApplicationServerApi extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "application-server";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v3";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    /**
     * Authenticates a user basing on the provided user id and password. If the authentication is successful, then returns a session token. Otherwise
     * returns null.
     *
     * @throws UserFailureException in case of any problems
     */
    public String login(String userId, String password);

    /**
     * Authenticates a user basing on the provided user id and password and makes the session look like as if it was a different user. If the
     * authentication is successful, then returns a session token. Otherwise returns null. The provided user id and password must represent an
     * {@code INSTANCE_ADMIN} account.
     *
     * @throws UserFailureException in case of any problems
     */
    public String loginAs(String userId, String password, String asUserId);

    /**
     * Authenticates as an anonymous user who does not require a password.
     *
     * @throws UserFailureException in case of any problems
     */
    public String loginAsAnonymousUser();

    /**
     * Terminates a user's session.
     *
     * @throws UserFailureException in case of any problems
     */
    public void logout(String sessionToken);

    /**
     * Returns detailed information about a user's session.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SessionInformation getSessionInformation(String sessionToken);

    /**
     * Returns true if a user's session is active. Otherwise returns false.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public boolean isSessionActive(String sessionToken);

    /**
     * Creates spaces basing on the provided {@code SpaceCreation} objects. Returns ids of the newly created spaces where nth id corresponds to nth
     * creation object.
     * <p>
     * Required access rights: {@code SPACE_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SpaceCreation} objects, insufficient access rights etc.
     */
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces);

    /**
     * Creates projects basing on the provided {@code ProjectCreation} objects. Returns ids of the newly created projects where nth id corresponds to
     * nth creation object.
     * <p>
     * Required access rights: {@code SPACE_POWER_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ProjectCreation} objects, insufficient access rights etc.
     */
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects);

    /**
     * Creates experiments basing on the provided {@code ExperimentCreation} objects. Returns ids of the newly created experiments where nth id
     * corresponds to nth creation object.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExperimentCreation} objects, insufficient access rights etc.
     */
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments);

    /**
     * Creates experiment types basing on the provided {@code ExperimentTypeCreation} objects. Returns ids of the newly created experiment types where
     * nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExperimentTypeCreation} objects, insufficient access rights etc.
     */
    public List<EntityTypePermId> createExperimentTypes(String sessionToken, List<ExperimentTypeCreation> newExperimentTypes);

    /**
     * Creates samples basing on the provided {@code SampleCreation} objects. Returns ids of the newly created samples where nth id corresponds to nth
     * creation object.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SampleCreation} objects, insufficient access rights etc.
     */
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples);

    /**
     * Creates sample types basing on the provided {@code SampleTypeCreation} objects. Returns ids of the newly created sample types where nth id
     * corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SampleTypeCreation} objects, insufficient access rights etc.
     */
    public List<EntityTypePermId> createSampleTypes(String sessionToken, List<SampleTypeCreation> newSampleTypes);

    /**
     * Creates data sets basing on the provided {@code DataSetCreation} objects. Returns ids of the newly created data sets where nth id corresponds
     * to nth creation object.
     * <p>
     * Required access rights: {@code SPACE_ETL_SERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code DataSetCreation} objects, insufficient access rights etc.
     */
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> newDataSets);

    /**
     * Creates data set types basing on the provided {@code DataSetTypeCreation} objects. Returns ids of the newly created data set types where nth id
     * corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code DataSetTypeCreation} objects, insufficient access rights etc.
     */
    public List<EntityTypePermId> createDataSetTypes(String sessionToken, List<DataSetTypeCreation> newDataSetTypes);

    /**
     * Creates materials basing on the provided {@code MaterialCreation} objects. Returns ids of the newly created materials where nth id corresponds
     * to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code MaterialCreation} objects, insufficient access rights etc.
     */
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials);

    /**
     * Creates material types basing on the provided {@code MaterialTypeCreation} objects. Returns ids of the newly created material types where nth
     * id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code MaterialTypeCreation} objects, insufficient access rights etc.
     */
    public List<EntityTypePermId> createMaterialTypes(String sessionToken, List<MaterialTypeCreation> newMaterialTypes);

    /**
     * Creates property types basing on the provided {@code PropertyTypeCreation} objects. Returns ids of the newly created property types where nth
     * id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PropertyTypeCreation} objects, insufficient access rights etc.
     */
    public List<PropertyTypePermId> createPropertyTypes(String sessionToken, List<PropertyTypeCreation> newPropertyTypes);

    /**
     * Creates plugins (i.e. dynamic property evaluators, managed property handlers, entity validators) basing on the provided {@code PluginCreation}
     * objects. Returns ids of the newly created plugins where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PluginCreation} objects, insufficient access rights etc.
     */
    public List<PluginPermId> createPlugins(String sessionToken, List<PluginCreation> newPlugins);

    /**
     * Creates vocabularies and vocabulary terms (optionally) basing on the provided {@code VocabularyCreation} objects. Returns ids of the newly
     * created vocabularies where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code VocabularyCreation} objects, insufficient access rights etc.
     */
    public List<VocabularyPermId> createVocabularies(String sessionToken, List<VocabularyCreation> newVocabularies);

    /**
     * Creates vocabulary terms basing on the provided {@code VocabularyTermCreation} objects. Returns ids of the newly created vocabulary terms where
     * nth id corresponds to nth creation object.
     * <ul>
     * Required access rights:
     * <li>unofficial terms - {@code PROJECT_USER} or stronger</li>
     * <li>official terms - {@code PROJECT_POWER_USER} or stronger</li>
     * <li>internally managed - {@code INSTANCE_ADMIN}</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code VocabularyTermCreation} objects, insufficient access rights etc.
     */
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> newVocabularyTerms);

    /**
     * Creates tags basing on the provided {@code TagCreation} objects. Returns ids of the newly created tags where nth id corresponds to nth creation
     * object. Tags are only visible to a user who created them.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code TagCreation} objects, insufficient access rights etc.
     */
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> newTags);

    /**
     * Creates authorization groups basing on the provided {@code AuthorizationGroupCreation} objects. Returns ids of the newly created authorization
     * groups where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code AuthorizationGroupCreation} objects, insufficient access rights
     *             etc.
     */
    public List<AuthorizationGroupPermId> createAuthorizationGroups(String sessionToken, List<AuthorizationGroupCreation> newAuthorizationGroups);

    /**
     * Creates role assignments basing on the provided {@code RoleAssignmentCreation} objects. Returns ids of the newly created role assignments where
     * nth id corresponds to nth creation object.
     * <ul>
     * Required access rights:
     * <li>instance roles - {@code INSTANCE_ADMIN}</li>
     * <li>space roles - {@code SPACE_ADMIN} or stronger</li>
     * <li>project roles - {@code PROJECT_ADMIN} or stronger</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code RoleAssignmentCreation} objects, insufficient access rights etc.
     */
    public List<RoleAssignmentTechId> createRoleAssignments(String sessionToken, List<RoleAssignmentCreation> newRoleAssignments);

    /**
     * Creates persons basing on the provided {@code PersonCreation} objects. Returns ids of the newly created persons where nth id corresponds to nth
     * creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PersonCreation} objects, insufficient access rights etc.
     */
    public List<PersonPermId> createPersons(String sessionToken, List<PersonCreation> newPersons);

    /**
     * Creates external data management systems basing on the provided {@code ExternalDmsCreation} objects. Returns ids of the newly created external
     * data management systems where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExternalDmsCreation} objects, insufficient access rights etc.
     */
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String sessionToken,
            List<ExternalDmsCreation> newExternalDataManagementSystems);

    /**
     * Creates queries basing on the provided {@code QueryCreation} objects. Returns ids of the newly created queries where nth id corresponds to nth
     * creation object.
     * <p>
     * Required access rights: depends on a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExternalDmsCreation} objects, insufficient access rights etc.
     */
    public List<QueryTechId> createQueries(String sessionToken, List<QueryCreation> newQueries);

    /**
     * Creates semantic annotations basing on the provided {@code SemanticAnnotationCreation} objects. Returns ids of the newly created semantic
     * annotations where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SemanticAnnotationCreation} objects, insufficient access rights
     *             etc.
     */
    public List<SemanticAnnotationPermId> createSemanticAnnotations(String sessionToken, List<SemanticAnnotationCreation> newAnnotations);

    /**
     * Creates personal access tokens basing on the provided {@code PersonalAccessTokenCreation} objects. Returns ids of the newly created personal access tokens
     * where nth id corresponds to nth creation object.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PersonalAccessTokenCreation} objects, insufficient access rights
     *                              etc.
     */
    public List<PersonalAccessTokenPermId> createPersonalAccessTokens(String sessionToken, List<PersonalAccessTokenCreation> newPersonalAccessTokens);

    /**
     * Updates spaces basing on the provided {@code SpaceUpdate} objects.
     * <p>
     * Required access rights: {@code SPACE_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SpaceUpdate} objects, insufficient access rights etc.
     */
    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates);

    /**
     * Updates projects basing on the provided {@code ProjectUpdate} objects.
     * <p>
     * Required access rights: {@code SPACE_POWER_USER} / {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ProjectUpdate} objects, insufficient access rights etc.
     */
    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates);

    /**
     * Updates experiments basing on the provided {@code ExperimentUpdate} objects.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExperimentUpdate} objects, insufficient access rights etc.
     */
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates);

    /**
     * Updates experiment types basing on the provided {@code ExperimentTypeUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExperimentTypeUpdate} objects, insufficient access rights etc.
     */
    public void updateExperimentTypes(String sessionToken, List<ExperimentTypeUpdate> experimentTypeUpdates);

    /**
     * Updates samples basing on the provided {@code SampleUpdate} objects.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SampleUpdate} objects, insufficient access rights etc.
     */
    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates);

    /**
     * Updates sample types basing on the provided {@code SampleTypeUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SampleTypeUpdate} objects, insufficient access rights etc.
     */
    public void updateSampleTypes(String sessionToken, List<SampleTypeUpdate> sampleTypeUpdates);

    /**
     * Updates data sets basing on the provided {@code DataSetUpdate} objects.
     * <p>
     * Required access rights: {@code PROJECT_POWER_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code DataSetUpdate} objects, insufficient access rights etc.
     */
    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates);

    /**
     * Updates data set types basing on the provided {@code DataSetTypeUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code DataSetTypeUpdate} objects, insufficient access rights etc.
     */
    public void updateDataSetTypes(String sessionToken, List<DataSetTypeUpdate> dataSetTypeUpdates);

    /**
     * Updates materials basing on the provided {@code MaterialUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code MaterialUpdate} objects, insufficient access rights etc.
     */
    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates);

    /**
     * Updates material types basing on the provided {@code MaterialTypeUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code MaterialTypeUpdate} objects, insufficient access rights etc.
     */
    public void updateMaterialTypes(String sessionToken, List<MaterialTypeUpdate> materialTypeUpdates);

    /**
     * Updates external data management systems basing on the provided {@code ExternalDmsUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code ExternalDmsUpdate} objects, insufficient access rights etc.
     */
    public void updateExternalDataManagementSystems(String sessionToken, List<ExternalDmsUpdate> externalDmsUpdates);

    /**
     * Updates property types basing on the provided {@code PropertyTypeUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PropertyTypeUpdate} objects, insufficient access rights etc.
     */
    public void updatePropertyTypes(String sessionToken, List<PropertyTypeUpdate> propertyTypeUpdates);

    /**
     * Updates plugins (i.e. dynamic property evaluators, managed property handlers, entity validators) basing on the provided {@code PluginUpdate}
     * objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PluginUpdate} objects, insufficient access rights etc.
     */
    public void updatePlugins(String sessionToken, List<PluginUpdate> pluginUpdates);

    /**
     * Updates vocabularies basing on the provided {@code VocabularyUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code VocabularyUpdate} objects, insufficient access rights etc.
     */
    public void updateVocabularies(String sessionToken, List<VocabularyUpdate> vocabularyUpdates);

    /**
     * Updates vocabulary terms basing on the provided {@code VocabularyTermUpdate} objects.
     * <ul>
     * Required access rights:
     * <li>unofficial terms - {@code PROJECT_USER} or stronger</li>
     * <li>official terms - {@code PROJECT_POWER_USER} or stronger</li>
     * <li>internally managed - {@code INSTANCE_ADMIN}</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code VocabularyTermUpdate} objects, insufficient access rights etc.
     */
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> vocabularyTermUpdates);

    /**
     * Updates tags basing on the provided {@code TagUpdate} objects. A user can only update own tags (i.e. tags a user has created).
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code TagUpdate} objects, insufficient access rights etc.
     */
    public void updateTags(String sessionToken, List<TagUpdate> tagUpdates);

    /**
     * Updates authorization groups basing on the provided {@code AuthorizationGroupUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code AuthorizationGroupUpdate} objects, insufficient access rights etc.
     */
    public void updateAuthorizationGroups(String sessionToken, List<AuthorizationGroupUpdate> authorizationGroupUpdates);

    /**
     * Updates persons basing on the provided {@code PersonUpdate} objects.
     * <ul>
     * Required access rights:
     * <li>activate/deactivate - {@code INSTANCE_ADMIN}</li>
     * <li>home space - user himself/herself / {@code SPACE_ADMIN} or stronger</li>
     * <li>webapp settings - user himself/herself / {@code INSTANCE_ADMIN}</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PersonUpdate} objects, insufficient access rights etc.
     */
    public void updatePersons(String sessionToken, List<PersonUpdate> personUpdates);

    /**
     * Updates operation executions basing on the provided {@code OperationExecutionUpdate} objects.
     * <p>
     * Required access rights: user who created the operation execution / {@code INSTANCE_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code OperationExecutionUpdate} objects, insufficient access rights etc.
     */
    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> executionUpdates);

    /**
     * Updates semantic annotations basing on the provided {@code SemanticAnnotationUpdate} objects.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code SemanticAnnotationUpdate} objects, insufficient access rights etc.
     */
    public void updateSemanticAnnotations(String sessionToken, List<SemanticAnnotationUpdate> annotationUpdates);

    /**
     * Updates queries basing on the provided {@code QueryUpdate} objects.
     * <p>
     * Required access rights: depends on a query and a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code QueryUpdate} objects, insufficient access rights etc.
     */
    public void updateQueries(String sessionToken, List<QueryUpdate> queryUpdates);

    /**
     * Updates personal access tokens basing on the provided {@code PersonalAccessTokenUpdate} objects.
     * <ul>
     * Required access rights: {@code ETL_SERVER}
     * </ul>
     *
     * @throws UserFailureException in case of any problems, e.g. incorrect {@code PersonalAccessTokenUpdate} objects, insufficient access rights etc.
     */
    public void updatePersonalAccessTokens(String sessionToken, List<PersonalAccessTokenUpdate> personalAccessTokenUpdates);

    /**
     * Gets authorization rights for the provided {@link IObjectId} ids. A result map contains an entry for a given id only if an object for that id
     * has been found and that object can be accessed by the user.
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IObjectId, Rights> getRights(String sessionToken, List<? extends IObjectId> ids, RightsFetchOptions fetchOptions);

    /**
     * Gets spaces for the provided {@code ISpaceId} ids. A result map contains an entry for a given id only if a space for that id has been found and
     * that space can be accessed by the user.
     * <p>
     * By default the returned spaces contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code SpaceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger (a project user can see a space if it has access to any of the projects in that
     * space)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds,
            SpaceFetchOptions fetchOptions);

    /**
     * Gets projects for the provided {@code IProjectId} ids. A result map contains an entry for a given id only if a project for that id has been
     * found and that project can be accessed by the user.
     * <p>
     * By default the returned projects contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code ProjectFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds,
            ProjectFetchOptions fetchOptions);

    /**
     * Gets experiments for the provided {@code IExperimentId} ids. A result map contains an entry for a given id only if an experiment for that id
     * has been found and that experiment can be accessed by the user.
     * <p>
     * By default the returned experiments contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code ExperimentFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions);

    /**
     * Gets experiment types for the provided {@code IEntityTypeId} ids. A result map contains an entry for a given id only if an experiment type for
     * that id has been found and that experiment type can be accessed by the user.
     * <p>
     * By default the returned experiment types contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code ExperimentTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeFetchOptions fetchOptions);

    /**
     * Gets samples for the provided {@code ISampleId} ids. A result map contains an entry for a given id only if a sample for that id has been found
     * and that sample can be accessed by the user.
     * <p>
     * By default the returned samples contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code SampleFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<ISampleId, Sample> getSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions);

    /**
     * Gets sample types for the provided {@code IEntityTypeId} ids. A result map contains an entry for a given id only if an sample type for that id
     * has been found and that sample type can be accessed by the user.
     * <p>
     * By default the returned sample types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code SampleTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IEntityTypeId, SampleType> getSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds,
            SampleTypeFetchOptions fetchOptions);

    /**
     * Gets data sets for the provided {@code IDataSetId} ids. A result map contains an entry for a given id only if a data set for that id has been
     * found and that data set can be accessed by the user.
     * <p>
     * By default the returned data sets contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code DataSetFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions);

    /**
     * Gets data set types for the provided {@code IEntityTypeId} ids. A result map contains an entry for a given id only if a data set type for that
     * id has been found and that data set type can be accessed by the user.
     * <p>
     * By default the returned data set types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code DataSetTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IEntityTypeId, DataSetType> getDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds,
            DataSetTypeFetchOptions fetchOptions);

    /**
     * Gets materials for the provided {@code IMaterialId} ids. A result map contains an entry for a given id only if a material for that id has been
     * found and that material can be accessed by the user.
     * <p>
     * By default the returned materials contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code MaterialFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions);

    /**
     * Gets material types for the provided {@code IEntityTypeId} ids. A result map contains an entry for a given id only if a material type for that
     * id has been found and that material type can be accessed by the user.
     * <p>
     * By default the returned material types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code MaterialTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IEntityTypeId, MaterialType> getMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds,
            MaterialTypeFetchOptions fetchOptions);

    /**
     * Gets property types for the provided {@code IPropertyTypeId} ids. A result map contains an entry for a given id only if a property type for
     * that id has been found and that property type can be accessed by the user.
     * <p>
     * By default the returned property types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code PropertyTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(String sessionToken, List<? extends IPropertyTypeId> typeIds,
            PropertyTypeFetchOptions fetchOptions);

    /**
     * Gets plugins (i.e. dynamic property evaluators, managed property handlers, entity validators) for the provided {@code IPluginId} ids. A result
     * map contains an entry for a given id only if a plugin for that id has been found and that plugin can be accessed by the user.
     * <p>
     * By default the returned plugins contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code PluginFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IPluginId, Plugin> getPlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginFetchOptions fetchOptions);

    /**
     * Gets vocabularies for the provided {@code IVocabularyId} ids. A result map contains an entry for a given id only if a vocabulary for that id
     * has been found and that vocabulary can be accessed by the user.
     * <p>
     * By default the returned vocabularies contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code VocabularyFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IVocabularyId, Vocabulary> getVocabularies(String sessionToken, List<? extends IVocabularyId> vocabularyIds,
            VocabularyFetchOptions fetchOptions);

    /**
     * Gets vocabulary terms for the provided {@code IVocabularyTermId} ids. A result map contains an entry for a given id only if a vocabulary term
     * for that id has been found and that vocabulary term can be accessed by the user.
     * <p>
     * By default the returned vocabulary terms contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code VocabularyTermFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions);

    /**
     * Gets tags for the provided {@code ITagId} ids. A result map contains an entry for a given id only if a tag for that id has been found and that
     * tag can be accessed by the user. A user can get own tags only (i.e. tags a user has created).
     * <p>
     * By default the returned tags contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code TagFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions);

    /**
     * Gets authorization groups for the provided {@code IAuthorizationGroupId} ids. A result map contains an entry for a given id only if an
     * authorization group for that id has been found and that authorization group can be accessed by the user.
     * <p>
     * By default the returned authorization groups contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code AuthorizationGroupFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupFetchOptions fetchOptions);

    /**
     * Gets role assignments for the provided {@code IRoleAssignmentId} ids. A result map contains an entry for a given id only if a role assignment
     * for that id has been found and that role assignment can be accessed by the user.
     * <p>
     * By default the returned role assignments contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code RoleAssignmentFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> ids,
            RoleAssignmentFetchOptions fetchOptions);

    /**
     * Gets persons for the provided {@code IPersonId} ids. A result map contains an entry for a given id only if a person for that id has been found
     * and that person can be accessed by the user.
     * <p>
     * By default the returned persons contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code PersonFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IPersonId, Person> getPersons(String sessionToken, List<? extends IPersonId> ids, PersonFetchOptions fetchOptions);

    /**
     * Gets external data management systems for the provided {@code IExternalDmsId} ids. A result map contains an entry for a given id only if an
     * external data management system for that id has been found and that external data management system can be accessed by the user.
     * <p>
     * By default the returned external data management systems contain only basic information. Any additional information to be fetched has to be
     * explicitly requested via {@code ExternalDmsFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions);

    /**
     * Gets semantic annotations for the provided {@code ISemanticAnnotationId} ids. A result map contains an entry for a given id only if a semantic
     * annotation for that id has been found and that semantic annotation can be accessed by the user.
     * <p>
     * By default the returned semantic annotations contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code SemanticAnnotationFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(String sessionToken,
            List<? extends ISemanticAnnotationId> annotationIds, SemanticAnnotationFetchOptions fetchOptions);

    /**
     * Gets operation executions for the provided {@code IOperationExecutionId} ids. A result map contains an entry for a given id only if an
     * operation execution for that id has been found and that operation execution can be accessed by the user.
     * <p>
     * By default the returned operation executions contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code OperationExecutionFetchOptions}.
     * </p>
     * <p>
     * Required access rights: user who created the operation execution / {@code INSTANCE_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds, OperationExecutionFetchOptions fetchOptions);

    /**
     * Gets queries for the provided {@code IQueryId} ids. A result map contains an entry for a given id only if a query for that id has been found
     * and that query can be accessed by the user.
     * <p>
     * By default the returned queries contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code QueryFetchOptions}.
     * </p>
     * <p>
     * Required access rights: depends on a query and a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IQueryId, Query> getQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryFetchOptions fetchOptions);

    /**
     * Gets query databases for the provided {@code IQueryDatabaseId} ids. A result map contains an entry for a given id only if a query database for
     * that id has been found and that query database can be accessed by the user.
     * <p>
     * By default the returned query databases contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code QueryDatabaseFetchOptions}.
     * </p>
     * <p>
     * Required access rights: depends on a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(String sessionToken, List<? extends IQueryDatabaseId> queryDatabaseIds,
            QueryDatabaseFetchOptions fetchOptions);

    /**
     * Gets personal access tokens for the provided {@code IPersonalAccessTokenId} ids. A result map contains an entry for a given id only if a personal access token for
     * that id has been found and that personal access token can be accessed by the user.
     * <p>
     * By default the returned personal access tokens contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code PersonalAccessTokenFetchOptions}.
     * </p>
     * <ul>
     * Required access rights:
     * <li>own tokens - {@code PROJECT_OBSERVER} or stronger</li>
     * <li>all tokens - {@code INSTANCE_ADMIN}</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(String sessionToken,
            List<? extends IPersonalAccessTokenId> personalAccessTokenIds, PersonalAccessTokenFetchOptions fetchOptions);

    /**
     * Searches for spaces basing on the provided {@code SpaceSearchCriteria}.
     * <p>
     * By default the returned spaces contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code SpaceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger (a project user can see a space if it has access to any of the projects in that
     * space)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions);

    /**
     * Searches for projects basing on the provided {@code ProjectSearchCriteria}.
     * <p>
     * By default the returned projects contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code ProjectFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions);

    /**
     * Searches for experiments basing on the provided {@code ExperimentSearchCriteria}.
     * <p>
     * By default the returned experiments contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code ExperimentFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions);

    /**
     * Searches for experiment types basing on the provided {@code ExperimentTypeSearchCriteria}.
     * <p>
     * By default the returned experiment types contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code ExperimentTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions);

    /**
     * Searches for samples basing on the provided {@code SampleSearchCriteria}.
     * <p>
     * By default the returned samples contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code SampleFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions);

    /**
     * Searches for sample types basing on the provided {@code SampleTypeSearchCriteria}.
     * <p>
     * By default the returned sample types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code SampleTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions);

    /**
     * Searches for data sets basing on the provided {@code DataSetSearchCriteria}.
     * <p>
     * By default the returned data sets contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code DataSetFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions);

    /**
     * Searches for data set types basing on the provided {@code DataSetTypeSearchCriteria}.
     * <p>
     * By default the returned data set types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code DataSetTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions);

    /**
     * Searches for materials basing on the provided {@code MaterialSearchCriteria}.
     * <p>
     * By default the returned materials contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code MaterialFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions);

    /**
     * Searches for external data management systems basing on the provided {@code ExternalDmsSearchCriteria}.
     * <p>
     * By default the returned external data management systems contain only basic information. Any additional information to be fetched has to be
     * explicitly requested via {@code ExternalDmsFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String sessionToken, ExternalDmsSearchCriteria searchCriteria,
            ExternalDmsFetchOptions fetchOptions);

    /**
     * Searches for material types basing on the provided {@code MaterialTypeSearchCriteria}.
     * <p>
     * By default the returned material types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code MaterialTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions);

    /**
     * Searches for plugins (i.e. dynamic property evaluators, managed property handlers, entity validators) basing on the provided
     * {@code PluginSearchCriteria}.
     * <p>
     * By default the returned plugins contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code PluginFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Plugin> searchPlugins(String sessionToken, PluginSearchCriteria searchCriteria,
            PluginFetchOptions fetchOptions);

    /**
     * Searches for vocabularies basing on the provided {@code VocabularySearchCriteria}.
     * <p>
     * By default the returned vocabularies contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code VocabularyFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Vocabulary> searchVocabularies(String sessionToken, VocabularySearchCriteria searchCriteria,
            VocabularyFetchOptions fetchOptions);

    /**
     * Searches for vocabulary terms basing on the provided {@code VocabularyTermSearchCriteria}.
     * <p>
     * By default the returned vocabulary terms contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code VocabularyTermFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions);

    /**
     * Searches for tags basing on the provided {@code TagSearchCriteria}. A user can find own tags only (i.e. tags a user has created).
     * <p>
     * By default the returned tags contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code TagFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions);

    /**
     * Searches for authorization groups basing on the provided {@code AuthorizationGroupSearchCriteria}.
     * <p>
     * By default the returned authorization groups contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code AuthorizationGroupFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(String sessionToken, AuthorizationGroupSearchCriteria searchCriteria,
            AuthorizationGroupFetchOptions fetchOptions);

    /**
     * Searches for role assignments basing on the provided {@code RoleAssignmentSearchCriteria}.
     * <p>
     * By default the returned role assignments contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code RoleAssignmentFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<RoleAssignment> searchRoleAssignments(String sessionToken, RoleAssignmentSearchCriteria searchCriteria,
            RoleAssignmentFetchOptions fetchOptions);

    /**
     * Searches for persons basing on the provided {@code PersonSearchCriteria}.
     * <p>
     * By default the returned persons contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code PersonFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Person> searchPersons(String sessionToken, PersonSearchCriteria searchCriteria, PersonFetchOptions fetchOptions);

    /**
     * Searches for custom application server services basing on the provided {@code CustomASServiceSearchCriteria}. More details on the custom
     * application server services can be found at "Custom Application Server Services" openBIS WIKI page.
     * <p>
     * By default the returned custom application server services contain only basic information. Any additional information to be fetched has to be
     * explicitly requested via {@code CustomASServiceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions);

    /**
     * Searches for search domain services basing on the provided {@code SearchDomainServiceSearchCriteria}. More details on the search domain
     * services can be found at "Search Domain Services" openBIS WIKI page.
     * <p>
     * By default the returned search domain services contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code SearchDomainServiceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<SearchDomainService> searchSearchDomainServices(String sessionToken, SearchDomainServiceSearchCriteria searchCriteria,
            SearchDomainServiceFetchOptions fetchOptions);

    /**
     * Searches for aggregation services basing on the provided {@code AggregationServiceSearchCriteria}. More details on the aggregation services can
     * be found at "Reporting Plugins" openBIS WIKI page (type: AGGREGATION_TABLE_MODEL).
     * <p>
     * By default the returned aggregation services contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code AggregationServiceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<AggregationService> searchAggregationServices(String sessionToken, AggregationServiceSearchCriteria searchCriteria,
            AggregationServiceFetchOptions fetchOptions);

    /**
     * Searches for reporting services basing on the provided {@code ReportingServiceSearchCriteria}. More details on the reporting services can be
     * found at "Reporting Plugins" openBIS WIKI page (type: TABLE_MODEL).
     * <p>
     * By default the returned reporting services contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code ReportingServiceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<ReportingService> searchReportingServices(String sessionToken, ReportingServiceSearchCriteria searchCriteria,
            ReportingServiceFetchOptions fetchOptions);

    /**
     * Searches for processing services basing on the provided {@code ProcessingServiceSearchCriteria}. More details on the processing services can be
     * found at "Processing Plugins" openBIS WIKI page.
     * <p>
     * By default the returned processing services contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code ProcessingServiceFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<ProcessingService> searchProcessingServices(String sessionToken, ProcessingServiceSearchCriteria searchCriteria,
            ProcessingServiceFetchOptions fetchOptions);

    /**
     * Searches for object kind modifications basing on the provided {@code ObjectKindModificationSearchCriteria}. An object kind modification
     * contains information on when a given kind of operation was last performed for a given kind of object, e.g. when was the last sample update or
     * when was the last property type creation etc.
     * <p>
     * By default the returned object kind modifications contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code ObjectKindModificationFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions);

    /**
     * Searches for experiments, samples, data sets and materials at once basing on the provided {@code GlobalSearchCriteria}.
     * <p>
     * By default the returned objects contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code GlobalSearchObjectFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions);

    /**
     * Searches for operation executions basing on the provided {@code OperationExecutionSearchCriteria}.
     * <p>
     * By default the returned operation executions contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code OperationExecutionFetchOptions}.
     * </p>
     * <p>
     * Required access rights: user who created the operation execution / {@code INSTANCE_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions);

    /**
     * Searches for data stores basing on the provided {@code DataStoreSearchCriteria}.
     * <p>
     * By default the returned data stores contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code DataStoreFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<DataStore> searchDataStores(String sessionToken, DataStoreSearchCriteria searchCriteria, DataStoreFetchOptions fetchOptions);

    /**
     * Searches for semantic annotations basing on the provided {@code SemanticAnnotationSearchCriteria}.
     * <p>
     * By default the returned semantic annotations contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code SemanticAnnotationFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(String sessionToken, SemanticAnnotationSearchCriteria searchCriteria,
            SemanticAnnotationFetchOptions fetchOptions);

    /**
     * Searches for property types basing on the provided {@code PropertyTypeSearchCriteria}.
     * <p>
     * By default the returned property types contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code PropertyTypeFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<PropertyType> searchPropertyTypes(String sessionToken, PropertyTypeSearchCriteria searchCriteria,
            PropertyTypeFetchOptions fetchOptions);

    /**
     * Searches for property assignments basing on the provided {@code PropertyAssignmentSearchCriteria}.
     * <p>
     * By default the returned property assignments contain only basic information. Any additional information to be fetched has to be explicitly
     * requested via {@code PropertyAssignmentFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<PropertyAssignment> searchPropertyAssignments(String sessionToken, PropertyAssignmentSearchCriteria searchCriteria,
            PropertyAssignmentFetchOptions fetchOptions);

    /**
     * Searches for queries basing on the provided {@code QuerySearchCriteria}.
     * <p>
     * By default the returned queries contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code QueryFetchOptions}.
     * </p>
     * <p>
     * Required access rights: depends on a query and a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Query> searchQueries(String sessionToken, QuerySearchCriteria searchCriteria, QueryFetchOptions fetchOptions);

    /**
     * Searches for query databases basing on the provided {@code QueryDatabaseSearchCriteria}.
     * <p>
     * By default the returned query databases contain only basic information. Any additional information to be fetched has to be explicitly requested
     * via {@code QueryDatabaseFetchOptions}.
     * </p>
     * <p>
     * Required access rights: depends on a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<QueryDatabase> searchQueryDatabases(String sessionToken, QueryDatabaseSearchCriteria searchCriteria,
            QueryDatabaseFetchOptions fetchOptions);

    /**
     * Permanently deletes spaces with the provided {@code ISpaceId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code SpaceDeletionOptions}.
     * <p>
     * Required access rights: {@code SPACE_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions);

    /**
     * Permanently deletes projects with the provided {@code IProjectId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code ProjectDeletionOptions}.
     * <p>
     * Required access rights: {@code SPACE_POWER_USER} / {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions);

    /**
     * Moves experiments with the provided {@code IExperimentId} ids to trash. Returns {@code IDeletionId} object which can be used either in
     * {@code confirmDeletions} call to delete the experiments permanently or in {@code revertDeletions} call to bring the trashed experiments back to
     * life. Additional deletion options (e.g. deletion reason) can be set via {@code ExperimentDeletionOptions}.
     * <p>
     * Required access rights: {@code PROJECT_POWER_USER} or stronger
     * </p>
     *
     * @see #confirmDeletions(String, List)
     * @see #revertDeletions(String, List)
     * @throws UserFailureException in case of any problems
     */
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions);

    /**
     * Moves samples with the provided {@code ISampleId} ids to trash. Returns {@code IDeletionId} object which can be used either in
     * {@code confirmDeletions} call to delete the samples permanently or in {@code revertDeletions} call to bring the trashed samples back to life.
     * Additional deletion options (e.g. deletion reason) can be set via {@code SampleDeletionOptions}.
     * <p>
     * Required access rights: {@code PROJECT_POWER_USER} or stronger
     * </p>
     *
     * @see #confirmDeletions(String, List)
     * @see #revertDeletions(String, List)
     * @throws UserFailureException in case of any problems
     */
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions);

    /**
     * Moves data sets with the provided {@code IDataSetId} ids to trash. Returns {@code IDeletionId} object which can be used either in
     * {@code confirmDeletions} call to delete the data sets permanently or in {@code revertDeletions} call to bring the trashed data sets back to
     * life. Additional deletion options (e.g. deletion reason) can be set via {@code DataSetDeletionOptions}.
     * <p>
     * Required access rights: {@code PROJECT_POWER_USER} or stronger
     * </p>
     *
     * @see #confirmDeletions(String, List)
     * @see #revertDeletions(String, List)
     * @throws UserFailureException in case of any problems
     */
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions);

    /**
     * Permanently deletes materials with the provided {@code IMaterialId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code MaterialDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions);

    /**
     * Permanently deletes plugins (i.e. dynamic property evaluators, managed property handlers, entity validators) with the provided
     * {@code IPluginId} ids. Additional deletion options (e.g. deletion reason) can be set via {@code PluginDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deletePlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginDeletionOptions deletionOptions);

    /**
     * Permanently deletes property types with the provided {@code IPropertyTypeId} ids. Additional deletion options (e.g. deletion reason) can be set
     * via {@code PropertyTypeDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deletePropertyTypes(String sessionToken, List<? extends IPropertyTypeId> propertyTypeIds,
            PropertyTypeDeletionOptions deletionOptions);

    /**
     * Permanently deletes vocabularies with the provided {@code IVocabularyId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code VocabularyDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteVocabularies(String sessionToken, List<? extends IVocabularyId> ids, VocabularyDeletionOptions deletionOptions);

    /**
     * Permanently deletes vocabulary terms with the provided {@code IVocabularyTermId} ids. Additional deletion options (e.g. deletion reason) can be
     * set via {@code VocabularyTermDeletionOptions}.
     * <ul>
     * Required access rights:
     * <li>unofficial and official terms - {@code PROJECT_POWER_USER} or stronger</li>
     * <li>internally managed - {@code INSTANCE_ADMIN}</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions);

    /**
     * Permanently deletes experiment types with the provided {@code IEntityTypeId} ids. Additional deletion options (e.g. deletion reason) can be set
     * via {@code ExperimentTypeDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeDeletionOptions deletionOptions);

    /**
     * Permanently deletes sample types with the provided {@code IEntityTypeId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code SampleTypeDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds, SampleTypeDeletionOptions deletionOptions);

    /**
     * Permanently deletes data set types with the provided {@code IEntityTypeId} ids. Additional deletion options (e.g. deletion reason) can be set
     * via {@code DataSetTypeDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds, DataSetTypeDeletionOptions deletionOptions);

    /**
     * Permanently deletes material types with the provided {@code IEntityTypeId} ids. Additional deletion options (e.g. deletion reason) can be set
     * via {@code MaterialTypeDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds, MaterialTypeDeletionOptions deletionOptions);

    /**
     * Permanently deletes external data management systems with the provided {@code IExternalDmsId} ids. Additional deletion options (e.g. deletion
     * reason) can be set via {@code ExternalDmsDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsDeletionOptions deletionOptions);

    /**
     * Permanently deletes tags with the provided {@code ITagId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code TagDeletionOptions}. A user can only delete own tags (i.e. tags a user has created).
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions);

    /**
     * Permanently deletes authorization groups with the provided {@code IAuthorizationGroupId} ids. Additional deletion options (e.g. deletion
     * reason) can be set via {@code AuthorizationGroupDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupDeletionOptions deletionOptions);

    /**
     * Permanently deletes role assignments with the provided {@code IRoleAssignmentId} ids. Additional deletion options (e.g. deletion reason) can be
     * set via {@code RoleAssignmentDeletionOptions}.
     * <ul>
     * Required access rights:
     * <li>instance roles - {@code INSTANCE_ADMIN}</li>
     * <li>space roles - {@code SPACE_ADMIN} or stronger</li>
     * <li>project roles - {@code PROJECT_ADMIN} or stronger</li>
     * </ul>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> assignmentIds,
            RoleAssignmentDeletionOptions deletionOptions);

    /**
     * Permanently deletes operation executions with the provided {@code IOperationExecutionId} ids. Additional deletion options (e.g. deletion
     * reason) can be set via {@code OperationExecutionDeletionOptions}.
     * <p>
     * Required access rights: user who created the operation execution / {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions);

    /**
     * Permanently deletes semantic annotations with the provided {@code ISemanticAnnotationId} ids. Additional deletion options (e.g. deletion
     * reason) can be set via {@code SemanticAnnotationDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteSemanticAnnotations(String sessionToken, List<? extends ISemanticAnnotationId> annotationIds,
            SemanticAnnotationDeletionOptions deletionOptions);

    /**
     * Permanently deletes queries with the provided {@code IQueryId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code QueryDeletionOptions}.
     * <p>
     * Required access rights: depends on a query and a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deleteQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryDeletionOptions deletionOptions);

    /**
     * Permanently deletes persons with the provided {@code IPersonId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code PersonDeletionOptions}.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deletePersons(String sessionToken, List<? extends IPersonId> personIds, PersonDeletionOptions deletionOptions);

    /**
     * Permanently deletes personal access tokens with the provided {@code IPersonalAccessTokenId} ids. Additional deletion options (e.g. deletion reason) can be set via
     * {@code PersonalAccessTokenDeletionOptions}.
     * <p>
     * Required access rights:
     * <li>own tokens - {@code PROJECT_OBSERVER} or stronger</li>
     * <li>all tokens - {@code INSTANCE_ADMIN}</li>
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void deletePersonalAccessTokens(String sessionToken, List<? extends IPersonalAccessTokenId> personalAccessTokenIds,
            PersonalAccessTokenDeletionOptions deletionOptions);

    /**
     * Searches for deletions basing on the provided {@code DeletionSearchCriteria}.
     * <p>
     * By default the returned deletions contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code DeletionFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code PROJECT_USER}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions);

    /**
     * Searches for events basing on the provided {@code EventSearchCriteria}.
     * <p>
     * By default the returned events contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code EventFetchOptions}.
     * </p>
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<Event> searchEvents(String sessionToken, EventSearchCriteria searchCriteria, EventFetchOptions fetchOptions);

    /**
     * Searches for personal access tokens basing on the provided {@code PersonalAccessTokenSearchCriteria}.
     * <p>
     * By default the returned personal access tokens contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code PersonalAccessTokenFetchOptions}.
     * </p>
     * <p>
     * Required access rights:
     * <li>own tokens - {@code PROJECT_OBSERVER} or stronger</li>
     * <li>all tokens - {@code INSTANCE_ADMIN}</li>
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<PersonalAccessToken> searchPersonalAccessTokens(String sessionToken, PersonalAccessTokenSearchCriteria searchCriteria,
            PersonalAccessTokenFetchOptions fetchOptions);

    /**
     * Searches for sessions of users basing on the provided {@code SessionInformationSearchCriteria}.
     * <p>
     * By default the returned sessions contain only basic information. Any additional information to be fetched has to be explicitly requested via
     * {@code SessionInformationFetchOptions}.
     * </p>
     * <p>
     * Required access rights:
     * <li>own tokens - {@code PROJECT_OBSERVER} or stronger</li>
     * <li>all tokens - {@code INSTANCE_ADMIN}</li>
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<SessionInformation> searchSessionInformation(String sessionToken, SessionInformationSearchCriteria searchCriteria,
            SessionInformationFetchOptions fetchOptions);

    /**
     * Reverts deletions with the provided {@code IDeletionId} ids (i.e. takes the entities out of trash and brings them back to life).
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @see #deleteExperiments(String, List, ExperimentDeletionOptions)
     * @see #deleteSamples(String, List, SampleDeletionOptions)
     * @see #deleteDataSets(String, List, DataSetDeletionOptions)
     * @throws UserFailureException in case of any problems
     */
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    /**
     * Confirms deletions with the provided {@code IDeletionId} ids (i.e. permanently deletes the entities).
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @see #deleteExperiments(String, List, ExperimentDeletionOptions)
     * @see #deleteSamples(String, List, SampleDeletionOptions)
     * @see #deleteDataSets(String, List, DataSetDeletionOptions)
     * @throws UserFailureException in case of any problems
     */
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    /**
     * Executes a custom application server service with the provided {@code ICustomASServiceId} id. Additional execution options (e.g. parameters)
     * can be set via {@code CustomASServiceExecutionOptions}. More details on the custom application server services can be found at "Custom
     * Application Server Services" openBIS WIKI page.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options);

    /**
     * Executes a search domain service. Execution options (e.g. preferred search domain, search string, parameters) can be set via
     * {@code SearchDomainServiceExecutionOptions}. More details on the search domain services can be found at "Search Domain Services" openBIS WIKI
     * page.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(String sessionToken,
            SearchDomainServiceExecutionOptions options);

    /**
     * Executes an aggregation service with the provided {@code IDssServiceId} id. Additional execution options (e.g. parameters) can be set via
     * {@code AggregationServiceExecutionOptions}. More details on the aggregation services can be found at "Reporting Plugins" openBIS WIKI page
     * (type: AGGREGATION_TABLE_MODEL).
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public TableModel executeAggregationService(String sessionToken, IDssServiceId serviceId, AggregationServiceExecutionOptions options);

    /**
     * Executes a reporting service with the provided {@code IDssServiceId} id. Additional execution options (e.g. data set codes) can be set via
     * {@code ReportingServiceExecutionOptions}. More details on the reporting services can be found at "Reporting Plugins" openBIS WIKI page (type:
     * TABLE_MODEL).
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public TableModel executeReportingService(String sessionToken, IDssServiceId serviceId, ReportingServiceExecutionOptions options);

    /**
     * Executes a processing service with the provided {@code IDssServiceId} id. Additional execution options (e.g. data set codes, parameters) can be
     * set via {@code ProcessingServiceExecutionOptions}. More details on the processing services can be found at "Processing Plugins" openBIS WIKI
     * page.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void executeProcessingService(String sessionToken, IDssServiceId serviceId, ProcessingServiceExecutionOptions options);

    /**
     * Executes a query with the provided {@code IQueryId} id. Additional execution options (e.g. parameters) can be set via
     * {@code QueryExecutionOptions}.
     * <p>
     * Required access rights: depends on a query and a query database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public TableModel executeQuery(String sessionToken, IQueryId queryId, QueryExecutionOptions options);

    /**
     * Executes the provided SQL. Only SELECT statements are allowed. Additional execution options (e.g. databaseId, parameters) can be set via
     * {@code SqlExecutionOptions}.
     * <p>
     * Required access rights: depends on a database (more details at "Custom Database Queries" openBIS WIKI page)
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public TableModel executeSql(String sessionToken, String sql, SqlExecutionOptions options);

    /**
     * Evaluates the provided plugin (e.g. a dynamic property plugin or an entity validation plugin). Parameters to be passed to the plugin can be set
     * via {@code PluginEvaluationOptions} subclasses. The method returns appropriate subclasses of {@code PluginEvaluationResult} depending on the
     * plugin type.
     * <p>
     * Required access rights: {@code INSTANCE_ADMIN}
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public PluginEvaluationResult evaluatePlugin(String sessionToken, PluginEvaluationOptions options);

    /**
     * Archives data sets with the provided {@code IDataSetId} ids. Additional archiving options can be set via {@code DataSetArchiveOptions}.
     * <p>
     * Required access rights: {@code PROJECT_POWER_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options);

    /**
     * Unarchives data sets with the provided {@code IDataSetId} ids. Additional unarchiving options can be set via {@code DataSetUnarchiveOptions}.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options);

    /**
     * Locks data sets with the provided {@code IDataSetId} ids. Additional locking options can be set via {@code DataSetLockOptions}.
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void lockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetLockOptions options);

    /**
     * Unlocks data sets with the provided {@code IDataSetId} ids. Additional unlocking options can be set via {@code DataSetUnlockOptions}.
     * <p>
     * Required access rights: {@code PROJECT_ADMIN} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public void unlockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnlockOptions options);

    /**
     * Executes all provided operations in one transaction. Depending on the chosen execution options ({@code SynchronousOperationExecutionOptions} or
     * {@code AsynchronousOperationExecutionOptions}) the operations are executed synchronously (i.e. in the same thread) or asynchronously (i.e. are
     * scheduled for later execution in a separate thread). Synchronous execution returns {@code SynchronousOperationExecutionResults} object.
     * Asynchronous execution returns {@code AsynchronousOperationExecutionResults}.
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @see #getOperationExecutions(String, List, OperationExecutionFetchOptions)
     * @see #searchOperationExecutions(String, OperationExecutionSearchCriteria, OperationExecutionFetchOptions)
     * @see #updateOperationExecutions(String, List)
     * @see #deleteOperationExecutions(String, List, OperationExecutionDeletionOptions)
     * @throws UserFailureException in case of any problems
     */
    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options);

    /**
     * Returns a map with additional server information:
     * <ul>
     * <li>api-version : major and minor version of the API (e.g. "3.4")</li>
     * <li>project-samples-enabled : true/false</li>
     * <li>archiving-configured : true/false</li>
     * <li>enabled-technologies : comma-separated list of enabled technologies (core-plugins modules)</li>
     * <li>authentication-service : currently used authenticated service (e.g. "ldap-authentication-service")</li>
     * <li>authentication-service.switch-aai.link : link for SSO authentication via SWITCHaai</li>
     * <li>authentication-service.switch-aai.label : label for SSO authentication via SWITCHaai</li>
     * </ul>
     * <p>
     * Required access rights: {@code PROJECT_OBSERVER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<String, String> getServerInformation(String sessionToken);

    /**
     * Returns a map with additional server public information:
     * <ul>
     * <li>authentication-service : currently used authenticated service (e.g. "ldap-authentication-service")</li>
     * <li>authentication-service.switch-aai.link : link for SSO authentication via SWITCHaai</li>
     * <li>authentication-service.switch-aai.label : label for SSO authentication via SWITCHaai</li>
     * </ul>
     * <p>
     * Required access rights: none
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public Map<String, String> getServerPublicInformation();

    /**
     * Generates globally unique identifiers that consist of a timestamp and a sequence generated number (e.g. "20180531170854641-944"). This method
     * uses one global sequence.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public List<String> createPermIdStrings(String sessionToken, int count);

    /**
     * Generates identifiers that are unique for a given entity kind and consist of a prefix and a sequence generated number (e.g. "MY-PREFIX-147");
     * this method uses a dedicated sequence for each entity kind.
     * <p>
     * Required access rights: {@code PROJECT_USER} or stronger
     * </p>
     *
     * @throws UserFailureException in case of any problems
     */
    public List<String> createCodes(String sessionToken, String prefix, EntityKind entityKind, int count);

    public void executeImport(String sessionToken, IImportData importData, ImportOptions importOptions);

//    public ExportResult executeExport(String sessionToken, ExportData exportData, ExportOptions exportOptions);

}
