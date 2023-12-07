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
package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
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
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class ApplicationServerApiLogger extends AbstractServerLogger implements
        IApplicationServerInternalApi
{
    public ApplicationServerApiLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    /**
     * This version is a dummy and should not be used. See {@link ApplicationServerApi}.
     */
    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    /**
     * This version is a dummy and should not be used. See {@link ApplicationServerApi}.
     */
    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public String login(String userId, String password)
    {
        return null;
    }

    @Override
    public String loginAsAnonymousUser()
    {
        return null;
    }

    @Override
    public String loginAs(String userId, String password, String asUser)
    {
        return null;
    }

    @Override public String loginAsSystem()
    {
        return null;
    }

    @Override
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces)
    {
        logAccess(sessionToken, "create-spaces", "NEW_SPACES(%s)", abbreviate(newSpaces));
        return null;
    }

    @Override
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects)
    {
        logAccess(sessionToken, "create-projects", "NEW_PROJECTS(%s)", abbreviate(newProjects));
        return null;
    }

    @Override
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials)
    {
        logAccess(sessionToken, "create-materials", "NEW_MATERIALS(%s)", abbreviate(newMaterials));
        return null;
    }

    @Override
    public List<EntityTypePermId> createMaterialTypes(String sessionToken, List<MaterialTypeCreation> newMaterialTypes)
    {
        logAccess(sessionToken, "create-material-types", "NEW_MATERIAL_TYPES(%s)", abbreviate(newMaterialTypes));
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments)
    {
        logAccess(sessionToken, "create-experiments", "NEW_EXPERIMENTS(%s)", abbreviate(newExperiments));
        return null;
    }

    @Override
    public List<EntityTypePermId> createExperimentTypes(String sessionToken, List<ExperimentTypeCreation> newExperimentTypes)
    {
        logAccess(sessionToken, "create-experiment-types", "NEW_EXPERIMENT_TYPES(%s)", abbreviate(newExperimentTypes));
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples)
    {
        logAccess(sessionToken, "create-samples", "NEW_SAMPLES(%s)", abbreviate(newSamples));
        return null;
    }

    @Override
    public List<EntityTypePermId> createSampleTypes(String sessionToken, List<SampleTypeCreation> newSampleTypes)
    {
        logAccess(sessionToken, "create-sample-types", "NEW_SAMPLE_TYPES(%s)", abbreviate(newSampleTypes));
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> newDataSets)
    {
        logAccess(sessionToken, "create-data-sets", "NEW_DATA_SETS(%s)", abbreviate(newDataSets));
        return null;
    }

    @Override
    public List<EntityTypePermId> createDataSetTypes(String sessionToken, List<DataSetTypeCreation> newDataSetTypes)
    {
        logAccess(sessionToken, "create-data-set-types", "NEW_DATA_SET_TYPES(%s)", abbreviate(newDataSetTypes));
        return null;
    }

    @Override
    public List<PropertyTypePermId> createPropertyTypes(String sessionToken, List<PropertyTypeCreation> newPropertyTypes)
    {
        logAccess(sessionToken, "create-property-types", "NEW_PROPERTY_TYPES(%s)", abbreviate(newPropertyTypes));
        return null;
    }

    @Override
    public List<PluginPermId> createPlugins(String sessionToken, List<PluginCreation> newPlugins)
    {
        logAccess(sessionToken, "create-plugins", "NEW_PLUGINS(%s)", abbreviate(newPlugins));
        return null;
    }

    @Override
    public List<VocabularyPermId> createVocabularies(String sessionToken, List<VocabularyCreation> newVocabularies)
    {
        logAccess(sessionToken, "create-vocabularies", "NEW_VOCABULARIES(%s)", abbreviate(newVocabularies));
        return null;
    }

    @Override
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> newVocabularyTerms)
    {
        logAccess(sessionToken, "create-vocabulary-terms", "NEW_VOCABULARY_TERMS(%s)", abbreviate(newVocabularyTerms));
        return null;
    }

    @Override
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> newTags)
    {
        logAccess(sessionToken, "create-tags", "NEW_TAGS(%s)", abbreviate(newTags));
        return null;
    }

    @Override
    public List<AuthorizationGroupPermId> createAuthorizationGroups(String sessionToken, List<AuthorizationGroupCreation> newAuthorizationGroups)
    {
        logAccess(sessionToken, "create-authorization-groups", "NEW_AUTHORIZATION_GROUPS(%s)", abbreviate(newAuthorizationGroups));
        return null;
    }

    @Override
    public List<RoleAssignmentTechId> createRoleAssignments(String sessionToken, List<RoleAssignmentCreation> newRoleAssignments)
    {
        logAccess(sessionToken, "create-role-assignments", "NEW_ROLE_ASSIGNMENTS(%s)", abbreviate(newRoleAssignments));
        return null;
    }

    @Override
    public List<PersonPermId> createPersons(String sessionToken, List<PersonCreation> newPersons)
    {
        logAccess(sessionToken, "create-persons", "NEW_PERSONS(%s)", abbreviate(newPersons));
        return null;
    }

    @Override
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String sessionToken,
            List<ExternalDmsCreation> newExternalDataManagementSystems)
    {
        logAccess(sessionToken, "create-external-data-management-systems", "NEW_EXTERNAL_DATA_MANAGEMENT_SYSTEMS(%s)",
                abbreviate(newExternalDataManagementSystems));
        return null;
    }

    @Override
    public List<SemanticAnnotationPermId> createSemanticAnnotations(String sessionToken, List<SemanticAnnotationCreation> newAnnotations)
    {
        logAccess(sessionToken, "create-semantic-annotations", "NEW_SEMANTIC_ANNOTATIONS(%s)", abbreviate(newAnnotations));
        return null;
    }

    @Override
    public List<QueryTechId> createQueries(String sessionToken, List<QueryCreation> newQueries)
    {
        logAccess(sessionToken, "create-queries", "NEW_QUERIES(%s)", abbreviate(newQueries));
        return null;
    }

    @Override public List<PersonalAccessTokenPermId> createPersonalAccessTokens(final String sessionToken,
            final List<PersonalAccessTokenCreation> newPersonalAccessTokens)
    {
        logAccess(sessionToken, "create-personal-access-tokens", "NEW_PERSONAL_ACCESS_TOKENS(%s)", abbreviate(newPersonalAccessTokens));
        return null;
    }

    @Override
    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates)
    {
        logAccess(sessionToken, "update-spaces", "SPACE_UPDATES(%s)", abbreviate(spaceUpdates));
    }

    @Override
    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates)
    {
        logAccess(sessionToken, "update-projects", "PROJECT_UPDATES(%s)", abbreviate(projectUpdates));
    }

    @Override
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates)
    {
        logAccess(sessionToken, "update-experiments", "EXPERIMENT_UPDATES(%s)", abbreviate(experimentUpdates));
    }

    @Override
    public void updateExperimentTypes(String sessionToken, List<ExperimentTypeUpdate> experimentTypeUpdates)
    {
        logAccess(sessionToken, "update-experiment-types", "EXPERIMENT_TYPE_UPDATES(%s)", abbreviate(experimentTypeUpdates));
    }

    @Override
    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates)
    {
        logAccess(sessionToken, "update-samples", "SAMPLE_UPDATES(%s)", abbreviate(sampleUpdates));
    }

    @Override
    public void updateSampleTypes(String sessionToken, List<SampleTypeUpdate> sampleTypeUpdates)
    {
        logAccess(sessionToken, "update-sample-types", "SAMPLE_TYPE_UPDATES(%s)", abbreviate(sampleTypeUpdates));
    }

    @Override
    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates)
    {
        logAccess(sessionToken, "update-data-sets", "DATA_SET_UPDATES(%s)", abbreviate(dataSetUpdates));
    }

    @Override
    public void updateDataSetTypes(String sessionToken, List<DataSetTypeUpdate> dataSetTypeUpdates)
    {
        logAccess(sessionToken, "update-data-set-types", "DATA_SET_TYPE_UPDATES(%s)", abbreviate(dataSetTypeUpdates));
    }

    @Override
    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates)
    {
        logAccess(sessionToken, "update-materials", "MATERIAL_UPDATES(%s)", abbreviate(materialUpdates));
    }

    @Override
    public void updateMaterialTypes(String sessionToken, List<MaterialTypeUpdate> materialTypeUpdates)
    {
        logAccess(sessionToken, "update-material-types", "MATERIAL_TYPE_UPDATES(%s)", abbreviate(materialTypeUpdates));
    }

    @Override
    public void updatePropertyTypes(String sessionToken, List<PropertyTypeUpdate> propertyTypeUpdates)
    {
        logAccess(sessionToken, "update-property-types", "PROPERTY_TYPE_UPDATES(%s)", abbreviate(propertyTypeUpdates));
    }

    @Override
    public void updatePlugins(String sessionToken, List<PluginUpdate> pluginUpdates)
    {
        logAccess(sessionToken, "update-plugins", "PLUGIN_UPDATES(%s)", abbreviate(pluginUpdates));
    }

    @Override
    public void updateVocabularies(String sessionToken, List<VocabularyUpdate> vocabularyUpdates)
    {
        logAccess(sessionToken, "update-vocabularies", "VOCABULARY_UPDATES(%s)", abbreviate(vocabularyUpdates));
    }

    @Override
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> vocabularyTermUpdates)
    {
        logAccess(sessionToken, "update-vocabulary-terms", "VOCABULARY_TERM_UPDATES(%s)", abbreviate(vocabularyTermUpdates));
    }

    @Override
    public void updateTags(String sessionToken, List<TagUpdate> tagUpdates)
    {
        logAccess(sessionToken, "update-tags", "TAG_UPDATES(%s)", abbreviate(tagUpdates));
    }

    @Override
    public void updateAuthorizationGroups(String sessionToken, List<AuthorizationGroupUpdate> authorizationGroupUpdates)
    {
        logAccess(sessionToken, "update-authorization-groups", "AUTHORIZATION_GROUP_UPDATES(%s)", abbreviate(authorizationGroupUpdates));
    }

    @Override
    public void updatePersons(String sessionToken, List<PersonUpdate> personUpdates)
    {
        logAccess(sessionToken, "update-persons", "PERSON_UPDATES(%s)", abbreviate(personUpdates));
    }

    @Override
    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> executionUpdates)
    {
        logAccess(sessionToken, "update-operation-executions", "OPERATION_EXECUTION_UPDATES(%s)", abbreviate(executionUpdates));
    }

    @Override
    public void updateSemanticAnnotations(String sessionToken, List<SemanticAnnotationUpdate> annotationUpdates)
    {
        logAccess(sessionToken, "update-semantic-annotations", "SEMANTIC_ANNOTATION_UPDATES(%s)", abbreviate(annotationUpdates));
    }

    @Override
    public Map<IObjectId, Rights> getRights(String sessionToken, List<? extends IObjectId> ids, RightsFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-rights", "IDS(%s) FETCH_OPTIONS(%s)", abbreviate(ids), fetchOptions);
        return null;
    }

    @Override
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-spaces", "SPACE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(spaceIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-projects", "PROJECT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(projectIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-experiments", "EXPERIMENT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(experimentIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-experiment-types", "EXPERIMENT_TYPE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(experimentTypeIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ISampleId, Sample> getSamples(String sessionToken,
            List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-samples", "SAMPLE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(sampleIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IEntityTypeId, SampleType> getSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds,
            SampleTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-sample-types", "SAMPLE_TYPE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(sampleTypeIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-materials", "MATERIAL_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(materialIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IEntityTypeId, MaterialType> getMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds,
            MaterialTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-material-types", "MATERIAL_TYPE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(materialTypeIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(String sessionToken, List<? extends IPropertyTypeId> typeIds,
            PropertyTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-property-types", "PROPERTY_TYPE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(typeIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IPluginId, Plugin> getPlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-plugins", "PLUGIN_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(pluginIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IVocabularyId, Vocabulary> getVocabularies(String sessionToken, List<? extends IVocabularyId> vocabularyIds,
            VocabularyFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-vocabularies", "VOCABULARY_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(vocabularyIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-vocabulary-terms", "VOCABULARY_TERM_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(vocabularyTermIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-tags", "TAG_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(tagIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-authorization-groups", "GROUP_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(groupIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> ids,
            RoleAssignmentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-role-assignments", "IDS(%s) FETCH_OPTIONS(%s)", abbreviate(ids), fetchOptions);
        return null;
    }

    @Override
    public Map<IPersonId, Person> getPersons(String sessionToken, List<? extends IPersonId> ids, PersonFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-persons", "IDS(%s) FETCH_OPTIONS(%s)", abbreviate(ids), fetchOptions);
        return null;
    }

    @Override
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-external-data-management-systems", "EXTERNAL_DMS_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(externalDmsIds),
                fetchOptions);
        return null;
    }

    @Override
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-data-sets", "DATA_SET_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(dataSetIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IEntityTypeId, DataSetType> getDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds,
            DataSetTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-data-set-types", "DATA_SET_TYPE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(dataSetTypeIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(String sessionToken,
            List<? extends ISemanticAnnotationId> annotationIds, SemanticAnnotationFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-semantic-annotations", "SEMANTIC_ANNOTATION_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(annotationIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IQueryId, Query> getQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-queries", "QUERY_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(queryIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(String sessionToken, List<? extends IQueryDatabaseId> queryDatabaseIds,
            QueryDatabaseFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-query-databases", "QUERY_DATABASE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(queryDatabaseIds), fetchOptions);
        return null;
    }

    @Override public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(final String sessionToken,
            final List<? extends IPersonalAccessTokenId> personalAccessTokenIds, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-personal-access-tokens", "PERSONAL_ACCESS_TOKEN_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(personalAccessTokenIds),
                fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-spaces", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-projects", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-experiments", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-experiment-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-samples", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-sample-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-data-sets", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-data-set-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-materials", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-material-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Plugin> searchPlugins(String sessionToken, PluginSearchCriteria searchCriteria, PluginFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-plugins", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Vocabulary> searchVocabularies(String sessionToken, VocabularySearchCriteria searchCriteria,
            VocabularyFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-vocabularies", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-vocabulary-terms", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-tags", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(String sessionToken, AuthorizationGroupSearchCriteria searchCriteria,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-authorization-groups", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<RoleAssignment> searchRoleAssignments(String sessionToken, RoleAssignmentSearchCriteria searchCriteria,
            RoleAssignmentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-role-assignments", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Person> searchPersons(String sessionToken, PersonSearchCriteria searchCriteria, PersonFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-persons", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Query> searchQueries(String sessionToken, QuerySearchCriteria searchCriteria, QueryFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-queries", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<QueryDatabase> searchQueryDatabases(String sessionToken, QueryDatabaseSearchCriteria searchCriteria,
            QueryDatabaseFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-query-databases", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override public SearchResult<PersonalAccessToken> searchPersonalAccessTokens(final String sessionToken,
            final PersonalAccessTokenSearchCriteria searchCriteria, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-personal-access-tokens", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-spaces", "SPACE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(spaceIds), deletionOptions);
    }

    @Override
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-projects", "PROJECT_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(projectIds), deletionOptions);
    }

    @Override
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-experiments", "EXPERIMENT_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(experimentIds), deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-samples", "SAMPLE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(sampleIds), deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-data-sets", "DATA_SET_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(dataSetIds), deletionOptions);
        return null;
    }

    @Override
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-materials", "MATERIAL_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(materialIds), deletionOptions);
    }

    @Override
    public void deletePlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-plugins", "PLUGIN_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(pluginIds), deletionOptions);
    }

    @Override
    public void deletePropertyTypes(String sessionToken, List<? extends IPropertyTypeId> propertyTypeIds, PropertyTypeDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-property-types", "PROPERTY_TYPES_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(propertyTypeIds), deletionOptions);
    }

    @Override
    public void deleteVocabularies(String sessionToken, List<? extends IVocabularyId> ids, VocabularyDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-vocabularies", "VOCABULARY_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(ids), deletionOptions);
    }

    @Override
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-vocabulary-terms", "VOCABULARY_TERM_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(termIds), deletionOptions);
    }

    @Override
    public void deleteExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-experiment-types", "EXPERIMENT_TYPE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(experimentTypeIds),
                deletionOptions);
    }

    @Override
    public void deleteSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds, SampleTypeDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-sample-types", "SAMPLE_TYPE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(sampleTypeIds),
                deletionOptions);
    }

    @Override
    public void deleteDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds, DataSetTypeDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-data-set-types", "DATA_SET_TYPE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(dataSetTypeIds),
                deletionOptions);
    }

    @Override
    public void deleteMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds, MaterialTypeDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-material-types", "MATERIAL_TYPE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(materialTypeIds),
                deletionOptions);
    }

    @Override
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-tags", "TAG_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(tagIds), deletionOptions);
    }

    @Override
    public void deleteAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-authorization-groups", "AUTHORIZATION_GROUP_IDS(%s) DELETION_OPTIONS(%s)",
                abbreviate(groupIds), deletionOptions);
    }

    @Override
    public void deleteRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> assignmentIds,
            RoleAssignmentDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-role-assignments", "ROLE_ASSIGNMENT_IDS(%s) DELETION_OPTIONS(%s)",
                abbreviate(assignmentIds), deletionOptions);
    }

    @Override
    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-operation-executions", "EXECUTION_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(executionIds), deletionOptions);
    }

    @Override
    public void deleteSemanticAnnotations(String sessionToken, List<? extends ISemanticAnnotationId> annotationIds,
            SemanticAnnotationDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-semantic-annotations", "SEMANTIC_ANNOTATION_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(annotationIds),
                deletionOptions);
    }

    @Override
    public void deleteQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-queries", "QUERY_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(queryIds), deletionOptions);
    }

    @Override
    public void deletePersons(String sessionToken, List<? extends IPersonId> personIds, PersonDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-persons", "PERSON_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(personIds), deletionOptions);
    }

    @Override public void deletePersonalAccessTokens(final String sessionToken, final List<? extends IPersonalAccessTokenId> personalAccessTokenIds,
            final PersonalAccessTokenDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-personal-access-tokens", "PERSONAL_ACCESS_TOKEN_IDS(%s) DELETION_OPTIONS(%s)",
                abbreviate(personalAccessTokenIds), deletionOptions);
    }

    @Override
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-deletions", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override public SearchResult<Event> searchEvents(final String sessionToken, final EventSearchCriteria searchCriteria,
            final EventFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-events", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        logAccess(sessionToken, "revert-deletions", "DELETION_IDS(%s)", abbreviate(deletionIds));
    }

    @Override
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        logAccess(sessionToken, "confirm-deletions", "DELETION_IDS(%s)", abbreviate(deletionIds));
    }

    @Override
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-custom-as-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<SearchDomainService> searchSearchDomainServices(String sessionToken, SearchDomainServiceSearchCriteria searchCriteria,
            SearchDomainServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-search-domain-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<AggregationService> searchAggregationServices(String sessionToken, AggregationServiceSearchCriteria searchCriteria,
            AggregationServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-aggregation-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ReportingService> searchReportingServices(String sessionToken, ReportingServiceSearchCriteria searchCriteria,
            ReportingServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-reporting-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ProcessingService> searchProcessingServices(String sessionToken, ProcessingServiceSearchCriteria searchCriteria,
            ProcessingServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-processing-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-object-kind-modifications", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataStore> searchDataStores(String sessionToken, DataStoreSearchCriteria searchCriteria, DataStoreFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-data-stores", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(String sessionToken, SemanticAnnotationSearchCriteria searchCriteria,
            SemanticAnnotationFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-semantic-annotations", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<PropertyType> searchPropertyTypes(String sessionToken, PropertyTypeSearchCriteria searchCriteria,
            PropertyTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-property-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<PropertyAssignment> searchPropertyAssignments(String sessionToken, PropertyAssignmentSearchCriteria searchCriteria,
            PropertyAssignmentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-property-assignments", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-custom-as-service", "SERVICE_ID(%s) EXECUTION_OPTIONS(%s)", serviceId, options);
        return null;
    }

    @Override
    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(String sessionToken,
            SearchDomainServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-search-domain-service", "EXECUTION_OPTIONS(%s)", options);
        return null;
    }

    @Override
    public TableModel executeAggregationService(String sessionToken, IDssServiceId serviceId, AggregationServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-aggregation-service", "SERVICE_ID(%s) EXECUTION_OPTIONS(%s)", serviceId, options);
        return null;
    }

    @Override
    public TableModel executeReportingService(String sessionToken, IDssServiceId serviceId, ReportingServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-reporting-service", "SERVICE_ID(%s) EXECUTION_OPTIONS(%s)", serviceId, options);
        return null;
    }

    @Override
    public void executeProcessingService(String sessionToken, IDssServiceId serviceId, ProcessingServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-processing-service", "SERVICE_ID(%s) EXECUTION_OPTIONS(%s)", serviceId, options);
    }

    @Override
    public TableModel executeQuery(String sessionToken, IQueryId queryId, QueryExecutionOptions options)
    {
        logAccess(sessionToken, "execute-query", "QUERY_ID(%s) EXECUTION_OPTIONS(%s)", queryId, options);
        return null;
    }

    @Override
    public TableModel executeSql(String sessionToken, String sql, SqlExecutionOptions options)
    {
        logAccess(sessionToken, "execute-sql", "SQL(%s) EXECUTION_OPTIONS(%s)", sql, options);
        return null;
    }

    @Override
    public PluginEvaluationResult evaluatePlugin(String sessionToken, PluginEvaluationOptions options)
    {
        logAccess(sessionToken, "evaluate-plugin", "EVALUATION_OPTIONS(%s)", options);
        return null;
    }

    @Override
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-globally", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options)
    {
        logAccess(sessionToken, "archive-data-sets", "DATA_SET_IDS(%s) ARCHIVE_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options)
    {
        logAccess(sessionToken, "unarchive-data-sets", "DATA_SET_IDS(%s) UNARCHIVE_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public void lockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetLockOptions options)
    {
        logAccess(sessionToken, "lock-data-sets", "DATA_SET_IDS(%s) LOCK_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public void unlockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnlockOptions options)
    {
        logAccess(sessionToken, "unlock-data-sets", "DATA_SET_IDS(%s) UNLOCK_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public SessionInformation getSessionInformation(String sessionToken)
    {
        logAccess(sessionToken, "get-session-information");
        return null;
    }

    @Override
    public boolean isSessionActive(String sessionToken)
    {
        logAccess(sessionToken, "is-session-active");
        return false;
    }

    @Override
    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options)
    {
        logAccess(sessionToken, "execute-operations", "OPERATIONS(%s) EXECUTION_OPTIONS(%s)", operations, options);
        return null;
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        logAccess(sessionToken, "server-info");
        return null;
    }

    @Override
    public Map<String, String> getServerPublicInformation()
    {
        logAccess(null, "server-public-info");
        return null;
    }

    @Override
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds, OperationExecutionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-operation-executions", "EXECUTION_IDS(%s) FETCH_OPTIONS(%s)", executionIds, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-operation-executions", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void updateExternalDataManagementSystems(String sessionToken, List<ExternalDmsUpdate> externalDmsUpdates)
    {
        logAccess(sessionToken, "update-external-dms", "EXTERNAL_DMS_UPDATES(%s)", abbreviate(externalDmsUpdates));
    }

    @Override
    public void updateQueries(String sessionToken, List<QueryUpdate> queryUpdates)
    {
        logAccess(sessionToken, "update-queries", "QUERY_UPDATES(%s)", abbreviate(queryUpdates));
    }

    @Override public void updatePersonalAccessTokens(final String sessionToken, final List<PersonalAccessTokenUpdate> personalAccessTokenUpdates)
    {
        logAccess(sessionToken, "update-personal-access-tokens", "PERSONAL_ACCESS_TOKEN_UPDATES(%s)", abbreviate(personalAccessTokenUpdates));
    }

    @Override
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String sessionToken, ExternalDmsSearchCriteria searchCriteria,
            ExternalDmsFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-external-dms", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override public SearchResult<SessionInformation> searchSessionInformation(final String sessionToken,
            final SessionInformationSearchCriteria searchCriteria, final SessionInformationFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-session-information", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void deleteExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-external-dms", "EXTERNAL_DMS_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(externalDmsIds), deletionOptions);
    }

    @Override
    public List<String> createPermIdStrings(String sessionToken, int count)
    {
        logAccess(sessionToken, "create-perm-id-strings", "COUNT(%s)", Integer.toString(count));
        return null;
    }

    @Override
    public List<String> createCodes(String sessionToken, String prefix, EntityKind entityKind, int count)
    {
        logAccess(sessionToken, "create-codes", "PREFIX(%s) ENTITY_KIND(%s) COUNT(%s)", prefix, entityKind, Integer.toString(count));
        return null;
    }

    @Override
    public void executeImport(final String sessionToken, final IImportData importData, final ImportOptions importOptions)
    {
        logAccess(sessionToken, "execute-import", "IImportData(%s) ImportOptions(%s)", importData, importOptions);
    }

    @Override
    public ExportResult executeExport(final String sessionToken, final ExportData exportData, final ExportOptions exportOptions)
    {
        logAccess(sessionToken, "execute-export", "ExportData(%s) ExportOptions(%s)", exportData, exportOptions);
        return null;
    }

    @Override public void registerUser(final String sessionToken)
    {
    }
}
