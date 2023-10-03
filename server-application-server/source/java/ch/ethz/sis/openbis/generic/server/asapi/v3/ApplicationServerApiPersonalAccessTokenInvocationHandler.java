/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.HashMap;
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
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenInvocation;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;

public class ApplicationServerApiPersonalAccessTokenInvocationHandler implements IApplicationServerInternalApi
{

    private final IPersonalAccessTokenInvocation invocation;

    private final IPersonalAccessTokenConfig config;

    private final IPersonalAccessTokenConverter converter;

    public ApplicationServerApiPersonalAccessTokenInvocationHandler(final IPersonalAccessTokenInvocation invocation)
    {
        this.invocation = invocation;
        this.config = CommonServiceProvider.getPersonalAccessTokenConfig();
        this.converter = CommonServiceProvider.getPersonalAccessTokenConverter();
    }

    @Override public int getMajorVersion()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public int getMinorVersion()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public String loginAsSystem()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public void registerUser(final String sessionToken)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public String login(final String userId, final String password)
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public String loginAs(final String userId, final String password, final String asUserId)
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public String loginAsAnonymousUser()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public void logout(final String sessionToken)
    {
        invocation.proceedWithOriginalArguments();
    }

    @Override public SessionInformation getSessionInformation(final String sessionToken)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public boolean isSessionActive(final String sessionToken)
    {
        try
        {
            return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
        } catch (InvalidSessionException e)
        {
            return false;
        }
    }

    @Override public List<SpacePermId> createSpaces(final String sessionToken, final List<SpaceCreation> newSpaces)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<ProjectPermId> createProjects(final String sessionToken, final List<ProjectCreation> newProjects)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<ExperimentPermId> createExperiments(final String sessionToken, final List<ExperimentCreation> newExperiments)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<EntityTypePermId> createExperimentTypes(final String sessionToken, final List<ExperimentTypeCreation> newExperimentTypes)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<SamplePermId> createSamples(final String sessionToken, final List<SampleCreation> newSamples)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<EntityTypePermId> createSampleTypes(final String sessionToken, final List<SampleTypeCreation> newSampleTypes)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<DataSetPermId> createDataSets(final String sessionToken, final List<DataSetCreation> newDataSets)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<EntityTypePermId> createDataSetTypes(final String sessionToken, final List<DataSetTypeCreation> newDataSetTypes)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<MaterialPermId> createMaterials(final String sessionToken, final List<MaterialCreation> newMaterials)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<EntityTypePermId> createMaterialTypes(final String sessionToken, final List<MaterialTypeCreation> newMaterialTypes)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<PropertyTypePermId> createPropertyTypes(final String sessionToken, final List<PropertyTypeCreation> newPropertyTypes)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<PluginPermId> createPlugins(final String sessionToken, final List<PluginCreation> newPlugins)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<VocabularyPermId> createVocabularies(final String sessionToken, final List<VocabularyCreation> newVocabularies)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<VocabularyTermPermId> createVocabularyTerms(final String sessionToken,
            final List<VocabularyTermCreation> newVocabularyTerms)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<TagPermId> createTags(final String sessionToken, final List<TagCreation> newTags)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<AuthorizationGroupPermId> createAuthorizationGroups(final String sessionToken,
            final List<AuthorizationGroupCreation> newAuthorizationGroups)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<RoleAssignmentTechId> createRoleAssignments(final String sessionToken,
            final List<RoleAssignmentCreation> newRoleAssignments)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<PersonPermId> createPersons(final String sessionToken, final List<PersonCreation> newPersons)
    {
        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage persons.");
        }
        return invocation.proceedWithOriginalArguments();
    }

    @Override public List<ExternalDmsPermId> createExternalDataManagementSystems(final String sessionToken,
            final List<ExternalDmsCreation> newExternalDataManagementSystems)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<QueryTechId> createQueries(final String sessionToken, final List<QueryCreation> newQueries)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<SemanticAnnotationPermId> createSemanticAnnotations(final String sessionToken,
            final List<SemanticAnnotationCreation> newAnnotations)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<PersonalAccessTokenPermId> createPersonalAccessTokens(final String sessionToken,
            final List<PersonalAccessTokenCreation> newPersonalAccessTokens)
    {
        checkPersonalAccessTokensEnabled();

        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage personal access tokens.");
        }
        return invocation.proceedWithOriginalArguments();
    }

    @Override public void updateSpaces(final String sessionToken, final List<SpaceUpdate> spaceUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateProjects(final String sessionToken, final List<ProjectUpdate> projectUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateExperiments(final String sessionToken, final List<ExperimentUpdate> experimentUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateExperimentTypes(final String sessionToken, final List<ExperimentTypeUpdate> experimentTypeUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateSamples(final String sessionToken, final List<SampleUpdate> sampleUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateSampleTypes(final String sessionToken, final List<SampleTypeUpdate> sampleTypeUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateDataSets(final String sessionToken, final List<DataSetUpdate> dataSetUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateDataSetTypes(final String sessionToken, final List<DataSetTypeUpdate> dataSetTypeUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateMaterials(final String sessionToken, final List<MaterialUpdate> materialUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateMaterialTypes(final String sessionToken, final List<MaterialTypeUpdate> materialTypeUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateExternalDataManagementSystems(final String sessionToken, final List<ExternalDmsUpdate> externalDmsUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updatePropertyTypes(final String sessionToken, final List<PropertyTypeUpdate> propertyTypeUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updatePlugins(final String sessionToken, final List<PluginUpdate> pluginUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateVocabularies(final String sessionToken, final List<VocabularyUpdate> vocabularyUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateVocabularyTerms(final String sessionToken, final List<VocabularyTermUpdate> vocabularyTermUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateTags(final String sessionToken, final List<TagUpdate> tagUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateAuthorizationGroups(final String sessionToken, final List<AuthorizationGroupUpdate> authorizationGroupUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updatePersons(final String sessionToken, final List<PersonUpdate> personUpdates)
    {
        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage persons.");
        }
        invocation.proceedWithOriginalArguments();
    }

    @Override public void updateOperationExecutions(final String sessionToken, final List<OperationExecutionUpdate> executionUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateSemanticAnnotations(final String sessionToken, final List<SemanticAnnotationUpdate> annotationUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updateQueries(final String sessionToken, final List<QueryUpdate> queryUpdates)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void updatePersonalAccessTokens(final String sessionToken, final List<PersonalAccessTokenUpdate> personalAccessTokenUpdates)
    {
        checkPersonalAccessTokensEnabled();

        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage personal access tokens.");
        }
        invocation.proceedWithOriginalArguments();
    }

    @Override public Map<IObjectId, Rights> getRights(final String sessionToken, final List<? extends IObjectId> ids,
            final RightsFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<ISpaceId, Space> getSpaces(final String sessionToken, final List<? extends ISpaceId> spaceIds,
            final SpaceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IProjectId, Project> getProjects(final String sessionToken, final List<? extends IProjectId> projectIds,
            final ProjectFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IExperimentId, Experiment> getExperiments(final String sessionToken, final List<? extends IExperimentId> experimentIds,
            final ExperimentFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IEntityTypeId, ExperimentType> getExperimentTypes(final String sessionToken,
            final List<? extends IEntityTypeId> experimentTypeIds,
            final ExperimentTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<ISampleId, Sample> getSamples(final String sessionToken, final List<? extends ISampleId> sampleIds,
            final SampleFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IEntityTypeId, SampleType> getSampleTypes(final String sessionToken, final List<? extends IEntityTypeId> sampleTypeIds,
            final SampleTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IDataSetId, DataSet> getDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds,
            final DataSetFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IEntityTypeId, DataSetType> getDataSetTypes(final String sessionToken, final List<? extends IEntityTypeId> dataSetTypeIds,
            final DataSetTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IMaterialId, Material> getMaterials(final String sessionToken, final List<? extends IMaterialId> materialIds,
            final MaterialFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IEntityTypeId, MaterialType> getMaterialTypes(final String sessionToken, final List<? extends IEntityTypeId> materialTypeIds,
            final MaterialTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IPropertyTypeId, PropertyType> getPropertyTypes(final String sessionToken, final List<? extends IPropertyTypeId> typeIds,
            final PropertyTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IPluginId, Plugin> getPlugins(final String sessionToken, final List<? extends IPluginId> pluginIds,
            final PluginFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IVocabularyId, Vocabulary> getVocabularies(final String sessionToken, final List<? extends IVocabularyId> vocabularyIds,
            final VocabularyFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(final String sessionToken,
            final List<? extends IVocabularyTermId> vocabularyTermIds, final VocabularyTermFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<ITagId, Tag> getTags(final String sessionToken, final List<? extends ITagId> tagIds, final TagFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(final String sessionToken,
            final List<? extends IAuthorizationGroupId> groupIds, final AuthorizationGroupFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(final String sessionToken, final List<? extends IRoleAssignmentId> ids,
            final RoleAssignmentFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IPersonId, Person> getPersons(final String sessionToken, final List<? extends IPersonId> ids,
            final PersonFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(final String sessionToken,
            final List<? extends IExternalDmsId> externalDmsIds, final ExternalDmsFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(final String sessionToken,
            final List<? extends ISemanticAnnotationId> annotationIds, final SemanticAnnotationFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(final String sessionToken,
            final List<? extends IOperationExecutionId> executionIds, final OperationExecutionFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IQueryId, Query> getQueries(final String sessionToken, final List<? extends IQueryId> queryIds,
            final QueryFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(final String sessionToken,
            final List<? extends IQueryDatabaseId> queryDatabaseIds,
            final QueryDatabaseFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(final String sessionToken,
            final List<? extends IPersonalAccessTokenId> personalAccessTokenIds, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        checkPersonalAccessTokensEnabled();

        Map<IPersonalAccessTokenId, PersonalAccessToken> originalResult = invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));

        if (converter.shouldConvert(sessionToken))
        {
            PersonalAccessTokenPermId currentPATId = new PersonalAccessTokenPermId(sessionToken);

            if (originalResult.containsKey(currentPATId))
            {
                Map<IPersonalAccessTokenId, PersonalAccessToken> resultWithCurrentPAT = new HashMap<>();
                resultWithCurrentPAT.put(currentPATId, originalResult.get(currentPATId));
                return resultWithCurrentPAT;
            } else
            {
                return new HashMap<>();
            }
        } else
        {
            return originalResult;
        }
    }

    @Override public SearchResult<Space> searchSpaces(final String sessionToken, final SpaceSearchCriteria searchCriteria,
            final SpaceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Project> searchProjects(final String sessionToken, final ProjectSearchCriteria searchCriteria,
            final ProjectFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Experiment> searchExperiments(final String sessionToken, final ExperimentSearchCriteria searchCriteria,
            final ExperimentFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<ExperimentType> searchExperimentTypes(final String sessionToken, final ExperimentTypeSearchCriteria searchCriteria,
            final ExperimentTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Sample> searchSamples(final String sessionToken, final SampleSearchCriteria searchCriteria,
            final SampleFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<SampleType> searchSampleTypes(final String sessionToken, final SampleTypeSearchCriteria searchCriteria,
            final SampleTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<DataSet> searchDataSets(final String sessionToken, final DataSetSearchCriteria searchCriteria,
            final DataSetFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<DataSetType> searchDataSetTypes(final String sessionToken, final DataSetTypeSearchCriteria searchCriteria,
            final DataSetTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Material> searchMaterials(final String sessionToken, final MaterialSearchCriteria searchCriteria,
            final MaterialFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<ExternalDms> searchExternalDataManagementSystems(final String sessionToken,
            final ExternalDmsSearchCriteria searchCriteria,
            final ExternalDmsFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<MaterialType> searchMaterialTypes(final String sessionToken, final MaterialTypeSearchCriteria searchCriteria,
            final MaterialTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Plugin> searchPlugins(final String sessionToken, final PluginSearchCriteria searchCriteria,
            final PluginFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Vocabulary> searchVocabularies(final String sessionToken, final VocabularySearchCriteria searchCriteria,
            final VocabularyFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<VocabularyTerm> searchVocabularyTerms(final String sessionToken, final VocabularyTermSearchCriteria searchCriteria,
            final VocabularyTermFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Tag> searchTags(final String sessionToken, final TagSearchCriteria searchCriteria,
            final TagFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<AuthorizationGroup> searchAuthorizationGroups(final String sessionToken,
            final AuthorizationGroupSearchCriteria searchCriteria,
            final AuthorizationGroupFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<RoleAssignment> searchRoleAssignments(final String sessionToken, final RoleAssignmentSearchCriteria searchCriteria,
            final RoleAssignmentFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Person> searchPersons(final String sessionToken, final PersonSearchCriteria searchCriteria,
            final PersonFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<CustomASService> searchCustomASServices(final String sessionToken,
            final CustomASServiceSearchCriteria searchCriteria,
            final CustomASServiceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<SearchDomainService> searchSearchDomainServices(final String sessionToken,
            final SearchDomainServiceSearchCriteria searchCriteria, final SearchDomainServiceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<AggregationService> searchAggregationServices(final String sessionToken,
            final AggregationServiceSearchCriteria searchCriteria,
            final AggregationServiceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<ReportingService> searchReportingServices(final String sessionToken,
            final ReportingServiceSearchCriteria searchCriteria,
            final ReportingServiceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<ProcessingService> searchProcessingServices(final String sessionToken,
            final ProcessingServiceSearchCriteria searchCriteria,
            final ProcessingServiceFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<ObjectKindModification> searchObjectKindModifications(final String sessionToken,
            final ObjectKindModificationSearchCriteria searchCriteria, final ObjectKindModificationFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<GlobalSearchObject> searchGlobally(final String sessionToken, final GlobalSearchCriteria searchCriteria,
            final GlobalSearchObjectFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<OperationExecution> searchOperationExecutions(final String sessionToken,
            final OperationExecutionSearchCriteria searchCriteria,
            final OperationExecutionFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<DataStore> searchDataStores(final String sessionToken, final DataStoreSearchCriteria searchCriteria,
            final DataStoreFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<SemanticAnnotation> searchSemanticAnnotations(final String sessionToken,
            final SemanticAnnotationSearchCriteria searchCriteria,
            final SemanticAnnotationFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<PropertyType> searchPropertyTypes(final String sessionToken, final PropertyTypeSearchCriteria searchCriteria,
            final PropertyTypeFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<PropertyAssignment> searchPropertyAssignments(final String sessionToken,
            final PropertyAssignmentSearchCriteria searchCriteria,
            final PropertyAssignmentFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Query> searchQueries(final String sessionToken, final QuerySearchCriteria searchCriteria,
            final QueryFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<QueryDatabase> searchQueryDatabases(final String sessionToken, final QueryDatabaseSearchCriteria searchCriteria,
            final QueryDatabaseFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<SessionInformation> searchSessionInformation(final String sessionToken,
            final SessionInformationSearchCriteria searchCriteria, final SessionInformationFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteSpaces(final String sessionToken, final List<? extends ISpaceId> spaceIds, final SpaceDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteProjects(final String sessionToken, final List<? extends IProjectId> projectIds,
            final ProjectDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public IDeletionId deleteExperiments(final String sessionToken, final List<? extends IExperimentId> experimentIds,
            final ExperimentDeletionOptions deletionOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public IDeletionId deleteSamples(final String sessionToken, final List<? extends ISampleId> sampleIds,
            final SampleDeletionOptions deletionOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public IDeletionId deleteDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds,
            final DataSetDeletionOptions deletionOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteMaterials(final String sessionToken, final List<? extends IMaterialId> materialIds,
            final MaterialDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deletePlugins(final String sessionToken, final List<? extends IPluginId> pluginIds,
            final PluginDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deletePropertyTypes(final String sessionToken, final List<? extends IPropertyTypeId> propertyTypeIds,
            final PropertyTypeDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteVocabularies(final String sessionToken, final List<? extends IVocabularyId> ids,
            final VocabularyDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteVocabularyTerms(final String sessionToken, final List<? extends IVocabularyTermId> termIds,
            final VocabularyTermDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteExperimentTypes(final String sessionToken, final List<? extends IEntityTypeId> experimentTypeIds,
            final ExperimentTypeDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteSampleTypes(final String sessionToken, final List<? extends IEntityTypeId> sampleTypeIds,
            final SampleTypeDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteDataSetTypes(final String sessionToken, final List<? extends IEntityTypeId> dataSetTypeIds,
            final DataSetTypeDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteMaterialTypes(final String sessionToken, final List<? extends IEntityTypeId> materialTypeIds,
            final MaterialTypeDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteExternalDataManagementSystems(final String sessionToken, final List<? extends IExternalDmsId> externalDmsIds,
            final ExternalDmsDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteTags(final String sessionToken, final List<? extends ITagId> tagIds, final TagDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteAuthorizationGroups(final String sessionToken, final List<? extends IAuthorizationGroupId> groupIds,
            final AuthorizationGroupDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteRoleAssignments(final String sessionToken, final List<? extends IRoleAssignmentId> assignmentIds,
            final RoleAssignmentDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteOperationExecutions(final String sessionToken, final List<? extends IOperationExecutionId> executionIds,
            final OperationExecutionDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteSemanticAnnotations(final String sessionToken, final List<? extends ISemanticAnnotationId> annotationIds,
            final SemanticAnnotationDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deleteQueries(final String sessionToken, final List<? extends IQueryId> queryIds,
            final QueryDeletionOptions deletionOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void deletePersons(final String sessionToken, final List<? extends IPersonId> personIds,
            final PersonDeletionOptions deletionOptions)
    {
        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage persons.");
        }
        invocation.proceedWithOriginalArguments();
    }

    @Override public void deletePersonalAccessTokens(final String sessionToken, final List<? extends IPersonalAccessTokenId> personalAccessTokenIds,
            final PersonalAccessTokenDeletionOptions deletionOptions)
    {
        checkPersonalAccessTokensEnabled();

        if (converter.shouldConvert(sessionToken))
        {
            throw new UserFailureException("Personal access tokens cannot be used to manage personal access tokens.");
        }
        invocation.proceedWithOriginalArguments();
    }

    @Override public SearchResult<Deletion> searchDeletions(final String sessionToken, final DeletionSearchCriteria searchCriteria,
            final DeletionFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<Event> searchEvents(final String sessionToken, final EventSearchCriteria searchCriteria,
            final EventFetchOptions fetchOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<PersonalAccessToken> searchPersonalAccessTokens(final String sessionToken,
            final PersonalAccessTokenSearchCriteria searchCriteria, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        checkPersonalAccessTokensEnabled();

        SearchResult<PersonalAccessToken> originalResult = invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));

        if (converter.shouldConvert(sessionToken))
        {
            PersonalAccessTokenPermId currentPATId = new PersonalAccessTokenPermId(sessionToken);

            for (PersonalAccessToken token : originalResult.getObjects())
            {
                if (token.getPermId().equals(currentPATId))
                {
                    List<PersonalAccessToken> resultWithCurrentPAT = new ArrayList<>();
                    resultWithCurrentPAT.add(token);
                    return new SearchResult<>(resultWithCurrentPAT, 1);
                }
            }

            return new SearchResult<>(new ArrayList<>(), 0);
        } else
        {
            return originalResult;
        }
    }

    @Override public void revertDeletions(final String sessionToken, final List<? extends IDeletionId> deletionIds)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void confirmDeletions(final String sessionToken, final List<? extends IDeletionId> deletionIds)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Object executeCustomASService(final String sessionToken, final ICustomASServiceId serviceId,
            final CustomASServiceExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(final String sessionToken,
            final SearchDomainServiceExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public TableModel executeAggregationService(final String sessionToken, final IDssServiceId serviceId,
            final AggregationServiceExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public TableModel executeReportingService(final String sessionToken, final IDssServiceId serviceId,
            final ReportingServiceExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void executeProcessingService(final String sessionToken, final IDssServiceId serviceId,
            final ProcessingServiceExecutionOptions options)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public TableModel executeQuery(final String sessionToken, final IQueryId queryId, final QueryExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public TableModel executeSql(final String sessionToken, final String sql, final SqlExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public PluginEvaluationResult evaluatePlugin(final String sessionToken, final PluginEvaluationOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void archiveDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds, final DataSetArchiveOptions options)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void unarchiveDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds,
            final DataSetUnarchiveOptions options)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void lockDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds, final DataSetLockOptions options)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public void unlockDataSets(final String sessionToken, final List<? extends IDataSetId> dataSetIds, final DataSetUnlockOptions options)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public IOperationExecutionResults executeOperations(final String sessionToken, final List<? extends IOperation> operations,
            final IOperationExecutionOptions options)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<String, String> getServerInformation(final String sessionToken)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public Map<String, String> getServerPublicInformation()
    {
        return invocation.proceedWithOriginalArguments();
    }

    @Override public List<String> createPermIdStrings(final String sessionToken, final int count)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override public List<String> createCodes(final String sessionToken, final String prefix, final EntityKind entityKind, final int count)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override
    public void executeImport(final String sessionToken, final IImportData importData, final ImportOptions importOptions)
    {
        invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    @Override
    public ExportResult executeExport(final String sessionToken, final ExportData exportData, final ExportOptions exportOptions)
    {
        return invocation.proceedWithNewFirstArgument(converter.convert(sessionToken));
    }

    private void checkPersonalAccessTokensEnabled()
    {
        if (!config.arePersonalAccessTokensEnabled())
        {
            throw new UserFailureException("Personal access tokens are disabled");
        }
    }

}
