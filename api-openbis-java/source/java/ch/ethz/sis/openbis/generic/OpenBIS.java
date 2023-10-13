/*
 *  Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ch.ethz.sis.openbis.generic;

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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.server.ServerInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.*;
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
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;

public class OpenBIS {

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000; //30 seconds

    private static final int CHUNK_SIZE = 1048576; // 1 MiB

    private final IApplicationServerApi asFacade;

    private final IDataStoreServerApi dssFacade;

    private String sessionToken;

    private final String asURL;

    private final String dssURL;

    private final int timeout;

    public OpenBIS(final String url) {
        this(url, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public OpenBIS(final String url, final int timeout) {
        this(url + "/openbis/openbis", url + "/datastore_server", 30000);
    }

    public OpenBIS(final String asURL, final String dssURL)
    {
        this(asURL, dssURL, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public OpenBIS(final String asURL, final String dssURL, final int timeout)
    {
        this.asFacade = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, asURL + IApplicationServerApi.SERVICE_URL, timeout);
        this.dssFacade = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, dssURL + IDataStoreServerApi.SERVICE_URL, timeout);
        this.asURL = asURL;
        this.dssURL = dssURL;
        this.timeout = timeout;
    }

    //
    // AS Facade methods
    //

    public String login(String userId, String password) {
        String sessionToken = asFacade.login(userId, password);
        setSessionToken(sessionToken);
        return sessionToken;
    }

    public String loginAs(String userId, String password, String asUserId) {
        String sessionToken = asFacade.loginAs(userId, password, asUserId);
        setSessionToken(sessionToken);
        return sessionToken;
    }

    public String loginAsAnonymousUser() {
        String sessionToken = asFacade.loginAsAnonymousUser();
        setSessionToken(sessionToken);
        return sessionToken;
    }

    public void logout() {
        asFacade.logout(sessionToken);
    }

    public SessionInformation getSessionInformation() {
        return asFacade.getSessionInformation(sessionToken);
    }

    public boolean isSessionActive() {
        return asFacade.isSessionActive(sessionToken);
    }

    public List<SpacePermId> createSpaces(List<SpaceCreation> newSpaces) {
        return asFacade.createSpaces(sessionToken, newSpaces);
    }

    public List<ProjectPermId> createProjects(List<ProjectCreation> newProjects) {
        return asFacade.createProjects(sessionToken, newProjects);
    }

    public List<ExperimentPermId> createExperiments(List<ExperimentCreation> newExperiments) {
        return asFacade.createExperiments(sessionToken, newExperiments);
    }

    public List<EntityTypePermId> createExperimentTypes(List<ExperimentTypeCreation> newExperimentTypes) {
        return asFacade.createExperimentTypes(sessionToken, newExperimentTypes);
    }

    public List<SamplePermId> createSamples(List<SampleCreation> newSamples) {
        return asFacade.createSamples(sessionToken, newSamples);
    }

    public List<EntityTypePermId> createSampleTypes(List<SampleTypeCreation> newSampleTypes) {
        return asFacade.createSampleTypes(sessionToken, newSampleTypes);
    }

    public List<DataSetPermId> createDataSetsAS(List<DataSetCreation> newDataSets) {
        return asFacade.createDataSets(sessionToken, newDataSets);
    }

    public List<EntityTypePermId> createDataSetTypes(List<DataSetTypeCreation> newDataSetTypes) {
        return asFacade.createDataSetTypes(sessionToken, newDataSetTypes);
    }

    public List<MaterialPermId> createMaterials(List<MaterialCreation> newMaterials) {
        return asFacade.createMaterials(sessionToken, newMaterials);
    }

    public List<EntityTypePermId> createMaterialTypes(List<MaterialTypeCreation> newMaterialTypes) {
        return asFacade.createMaterialTypes(sessionToken, newMaterialTypes);
    }

    public List<PropertyTypePermId> createPropertyTypes(List<PropertyTypeCreation> newPropertyTypes) {
        return asFacade.createPropertyTypes(sessionToken, newPropertyTypes);
    }

    public List<PluginPermId> createPlugins(List<PluginCreation> newPlugins) {
        return asFacade.createPlugins(sessionToken, newPlugins);
    }

    public List<VocabularyPermId> createVocabularies(List<VocabularyCreation> newVocabularies) {
        return asFacade.createVocabularies(sessionToken, newVocabularies);
    }

    public List<VocabularyTermPermId> createVocabularyTerms(List<VocabularyTermCreation> newVocabularyTerms) {
        return asFacade.createVocabularyTerms(sessionToken, newVocabularyTerms);
    }

    public List<TagPermId> createTags(List<TagCreation> newTags) {
        return asFacade.createTags(sessionToken, newTags);
    }

    public List<AuthorizationGroupPermId> createAuthorizationGroups(List<AuthorizationGroupCreation> newAuthorizationGroups) {
        return asFacade.createAuthorizationGroups(sessionToken, newAuthorizationGroups);
    }

    public List<RoleAssignmentTechId> createRoleAssignments(List<RoleAssignmentCreation> newRoleAssignments) {
        return asFacade.createRoleAssignments(sessionToken, newRoleAssignments);
    }

    public List<PersonPermId> createPersons(List<PersonCreation> newPersons) {
        return asFacade.createPersons(sessionToken, newPersons);
    }

    public List<ExternalDmsPermId> createExternalDataManagementSystems(List<ExternalDmsCreation> newExternalDataManagementSystems) {
        return asFacade.createExternalDataManagementSystems(sessionToken, newExternalDataManagementSystems);
    }

    public List<QueryTechId> createQueries(List<QueryCreation> newQueries) {
        return asFacade.createQueries(sessionToken, newQueries);
    }

    public List<SemanticAnnotationPermId> createSemanticAnnotations(List<SemanticAnnotationCreation> newAnnotations) {
        return asFacade.createSemanticAnnotations(sessionToken, newAnnotations);
    }

    public List<PersonalAccessTokenPermId> createPersonalAccessTokens(List<PersonalAccessTokenCreation> newPersonalAccessTokens) {
        return asFacade.createPersonalAccessTokens(sessionToken, newPersonalAccessTokens);
    }

    public void updateSpaces(List<SpaceUpdate> spaceUpdates) {
        asFacade.updateSpaces(sessionToken, spaceUpdates);
    }

    public void updateProjects(List<ProjectUpdate> projectUpdates) {
        asFacade.updateProjects(sessionToken, projectUpdates);
    }

    public void updateExperiments(List<ExperimentUpdate> experimentUpdates) {
        asFacade.updateExperiments(sessionToken, experimentUpdates);
    }

    public void updateExperimentTypes(List<ExperimentTypeUpdate> experimentTypeUpdates) {
        asFacade.updateExperimentTypes(sessionToken, experimentTypeUpdates);
    }

    public void updateSamples(List<SampleUpdate> sampleUpdates) {
        asFacade.updateSamples(sessionToken, sampleUpdates);
    }

    public void updateSampleTypes(List<SampleTypeUpdate> sampleTypeUpdates) {
        asFacade.updateSampleTypes(sessionToken, sampleTypeUpdates);
    }

    public void updateDataSets(List<DataSetUpdate> dataSetUpdates) {
        asFacade.updateDataSets(sessionToken, dataSetUpdates);
    }

    public void updateDataSetTypes(List<DataSetTypeUpdate> dataSetTypeUpdates) {
        asFacade.updateDataSetTypes(sessionToken, dataSetTypeUpdates);
    }

    public void updateMaterials(List<MaterialUpdate> materialUpdates) {
        asFacade.updateMaterials(sessionToken, materialUpdates);
    }

    public void updateMaterialTypes(List<MaterialTypeUpdate> materialTypeUpdates) {
        asFacade.updateMaterialTypes(sessionToken, materialTypeUpdates);
    }

    public void updateExternalDataManagementSystems(List<ExternalDmsUpdate> externalDmsUpdates) {
        asFacade.updateExternalDataManagementSystems(sessionToken, externalDmsUpdates);
    }

    public void updatePropertyTypes(List<PropertyTypeUpdate> propertyTypeUpdates) {
        asFacade.updatePropertyTypes(sessionToken, propertyTypeUpdates);
    }

    public void updatePlugins(List<PluginUpdate> pluginUpdates) {
        asFacade.updatePlugins(sessionToken, pluginUpdates);
    }

    public void updateVocabularies(List<VocabularyUpdate> vocabularyUpdates) {
        asFacade.updateVocabularies(sessionToken, vocabularyUpdates);
    }

    public void updateVocabularyTerms(List<VocabularyTermUpdate> vocabularyTermUpdates) {
        asFacade.updateVocabularyTerms(sessionToken, vocabularyTermUpdates);
    }

    public void updateTags(List<TagUpdate> tagUpdates) {
        asFacade.updateTags(sessionToken, tagUpdates);
    }

    public void updateAuthorizationGroups(List<AuthorizationGroupUpdate> authorizationGroupUpdates) {
        asFacade.updateAuthorizationGroups(sessionToken, authorizationGroupUpdates);
    }

    public void updatePersons(List<PersonUpdate> personUpdates) {
        asFacade.updatePersons(sessionToken, personUpdates);
    }

    public void updateOperationExecutions(List<OperationExecutionUpdate> executionUpdates) {
        asFacade.updateOperationExecutions(sessionToken, executionUpdates);
    }

    public void updateSemanticAnnotations(List<SemanticAnnotationUpdate> annotationUpdates) {
        asFacade.updateSemanticAnnotations(sessionToken, annotationUpdates);
    }

    public void updateQueries(List<QueryUpdate> queryUpdates) {
        asFacade.updateQueries(sessionToken, queryUpdates);
    }

    public void updatePersonalAccessTokens(List<PersonalAccessTokenUpdate> personalAccessTokenUpdates) {
        asFacade.updatePersonalAccessTokens(sessionToken, personalAccessTokenUpdates);
    }

    public Map<IObjectId, Rights> getRights(List<? extends IObjectId> ids, RightsFetchOptions fetchOptions) {
        return asFacade.getRights(sessionToken, ids, fetchOptions);
    }

    public Map<ISpaceId, Space> getSpaces(List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions) {
        return asFacade.getSpaces(sessionToken, spaceIds, fetchOptions);
    }

    public Map<IProjectId, Project> getProjects(List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions) {
        return asFacade.getProjects(sessionToken, projectIds, fetchOptions);
    }

    public Map<IExperimentId, Experiment> getExperiments(List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions) {
        return asFacade.getExperiments(sessionToken, experimentIds, fetchOptions);
    }

    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(List<? extends IEntityTypeId> experimentTypeIds, ExperimentTypeFetchOptions fetchOptions) {
        return asFacade.getExperimentTypes(sessionToken, experimentTypeIds, fetchOptions);
    }

    public Map<ISampleId, Sample> getSamples(List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions) {
        return asFacade.getSamples(sessionToken, sampleIds, fetchOptions);
    }

    public Map<IEntityTypeId, SampleType> getSampleTypes(List<? extends IEntityTypeId> sampleTypeIds, SampleTypeFetchOptions fetchOptions) {
        return asFacade.getSampleTypes(sessionToken, sampleTypeIds, fetchOptions);
    }

    public Map<IDataSetId, DataSet> getDataSets(List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions) {
        return asFacade.getDataSets(sessionToken, dataSetIds, fetchOptions);
    }

    public Map<IEntityTypeId, DataSetType> getDataSetTypes(List<? extends IEntityTypeId> dataSetTypeIds, DataSetTypeFetchOptions fetchOptions) {
        return asFacade.getDataSetTypes(sessionToken, dataSetTypeIds, fetchOptions);
    }

    public Map<IMaterialId, Material> getMaterials(List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions) {
        return asFacade.getMaterials(sessionToken, materialIds, fetchOptions);
    }

    public Map<IEntityTypeId, MaterialType> getMaterialTypes(List<? extends IEntityTypeId> materialTypeIds, MaterialTypeFetchOptions fetchOptions) {
        return asFacade.getMaterialTypes(sessionToken, materialTypeIds, fetchOptions);
    }

    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(List<? extends IPropertyTypeId> typeIds, PropertyTypeFetchOptions fetchOptions) {
        return asFacade.getPropertyTypes(sessionToken, typeIds, fetchOptions);
    }

    public Map<IPluginId, Plugin> getPlugins(List<? extends IPluginId> pluginIds, PluginFetchOptions fetchOptions) {
        return asFacade.getPlugins(sessionToken, pluginIds, fetchOptions);
    }

    public Map<IVocabularyId, Vocabulary> getVocabularies(List<? extends IVocabularyId> vocabularyIds, VocabularyFetchOptions fetchOptions) {
        return asFacade.getVocabularies(sessionToken, vocabularyIds, fetchOptions);
    }

    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(List<? extends IVocabularyTermId> vocabularyTermIds, VocabularyTermFetchOptions fetchOptions) {
        return asFacade.getVocabularyTerms(sessionToken, vocabularyTermIds, fetchOptions);
    }

    public Map<ITagId, Tag> getTags(List<? extends ITagId> tagIds, TagFetchOptions fetchOptions) {
        return asFacade.getTags(sessionToken, tagIds, fetchOptions);
    }

    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(List<? extends IAuthorizationGroupId> groupIds, AuthorizationGroupFetchOptions fetchOptions) {
        return asFacade.getAuthorizationGroups(sessionToken, groupIds, fetchOptions);
    }

    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(List<? extends IRoleAssignmentId> ids, RoleAssignmentFetchOptions fetchOptions) {
        return asFacade.getRoleAssignments(sessionToken, ids, fetchOptions);
    }

    public Map<IPersonId, Person> getPersons(List<? extends IPersonId> ids, PersonFetchOptions fetchOptions) {
        return asFacade.getPersons(sessionToken, ids, fetchOptions);
    }

    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(List<? extends IExternalDmsId> externalDmsIds, ExternalDmsFetchOptions fetchOptions) {
        return asFacade.getExternalDataManagementSystems(sessionToken, externalDmsIds, fetchOptions);
    }

    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(List<? extends ISemanticAnnotationId> annotationIds, SemanticAnnotationFetchOptions fetchOptions) {
        return asFacade.getSemanticAnnotations(sessionToken, annotationIds, fetchOptions);
    }

    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(List<? extends IOperationExecutionId> executionIds, OperationExecutionFetchOptions fetchOptions) {
        return asFacade.getOperationExecutions(sessionToken, executionIds, fetchOptions);
    }

    public Map<IQueryId, Query> getQueries(List<? extends IQueryId> queryIds, QueryFetchOptions fetchOptions) {
        return asFacade.getQueries(sessionToken, queryIds, fetchOptions);
    }

    public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(List<? extends IQueryDatabaseId> queryDatabaseIds, QueryDatabaseFetchOptions fetchOptions) {
        return asFacade.getQueryDatabases(sessionToken, queryDatabaseIds, fetchOptions);
    }

    public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(List<? extends IPersonalAccessTokenId> personalAccessTokenIds, PersonalAccessTokenFetchOptions fetchOptions) {
        return asFacade.getPersonalAccessTokens(sessionToken, personalAccessTokenIds, fetchOptions);
    }

    public SearchResult<Space> searchSpaces(SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions) {
        return asFacade.searchSpaces(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Project> searchProjects(ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions) {
        return asFacade.searchProjects(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Experiment> searchExperiments(ExperimentSearchCriteria searchCriteria, ExperimentFetchOptions fetchOptions) {
        return asFacade.searchExperiments(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<ExperimentType> searchExperimentTypes(ExperimentTypeSearchCriteria searchCriteria, ExperimentTypeFetchOptions fetchOptions) {
        return asFacade.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Sample> searchSamples(SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions) {
        return asFacade.searchSamples(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<SampleType> searchSampleTypes(SampleTypeSearchCriteria searchCriteria, SampleTypeFetchOptions fetchOptions) {
        return asFacade.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<DataSet> searchDataSets(DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions) {
        return asFacade.searchDataSets(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<DataSetType> searchDataSetTypes(DataSetTypeSearchCriteria searchCriteria, DataSetTypeFetchOptions fetchOptions) {
        return asFacade.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Material> searchMaterials(MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions) {
        return asFacade.searchMaterials(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<ExternalDms> searchExternalDataManagementSystems(ExternalDmsSearchCriteria searchCriteria, ExternalDmsFetchOptions fetchOptions) {
        return asFacade.searchExternalDataManagementSystems(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<MaterialType> searchMaterialTypes(MaterialTypeSearchCriteria searchCriteria, MaterialTypeFetchOptions fetchOptions) {
        return asFacade.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Plugin> searchPlugins(PluginSearchCriteria searchCriteria, PluginFetchOptions fetchOptions) {
        return asFacade.searchPlugins(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Vocabulary> searchVocabularies(VocabularySearchCriteria searchCriteria, VocabularyFetchOptions fetchOptions) {
        return asFacade.searchVocabularies(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<VocabularyTerm> searchVocabularyTerms(VocabularyTermSearchCriteria searchCriteria, VocabularyTermFetchOptions fetchOptions) {
        return asFacade.searchVocabularyTerms(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Tag> searchTags(TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions) {
        return asFacade.searchTags(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(AuthorizationGroupSearchCriteria searchCriteria, AuthorizationGroupFetchOptions fetchOptions) {
        return asFacade.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<RoleAssignment> searchRoleAssignments(RoleAssignmentSearchCriteria searchCriteria, RoleAssignmentFetchOptions fetchOptions) {
        return asFacade.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Person> searchPersons(PersonSearchCriteria searchCriteria, PersonFetchOptions fetchOptions) {
        return asFacade.searchPersons(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<CustomASService> searchCustomASServices(CustomASServiceSearchCriteria searchCriteria, CustomASServiceFetchOptions fetchOptions) {
        return asFacade.searchCustomASServices(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<SearchDomainService> searchSearchDomainServices(SearchDomainServiceSearchCriteria searchCriteria, SearchDomainServiceFetchOptions fetchOptions) {
        return asFacade.searchSearchDomainServices(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<AggregationService> searchAggregationServices(AggregationServiceSearchCriteria searchCriteria, AggregationServiceFetchOptions fetchOptions) {
        return asFacade.searchAggregationServices(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<ReportingService> searchReportingServices(ReportingServiceSearchCriteria searchCriteria, ReportingServiceFetchOptions fetchOptions) {
        return asFacade.searchReportingServices(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<ProcessingService> searchProcessingServices(ProcessingServiceSearchCriteria searchCriteria, ProcessingServiceFetchOptions fetchOptions) {
        return asFacade.searchProcessingServices(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<ObjectKindModification> searchObjectKindModifications(ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions) {
        return asFacade.searchObjectKindModifications(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<GlobalSearchObject> searchGlobally(GlobalSearchCriteria searchCriteria, GlobalSearchObjectFetchOptions fetchOptions) {
        return asFacade.searchGlobally(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<OperationExecution> searchOperationExecutions(OperationExecutionSearchCriteria searchCriteria, OperationExecutionFetchOptions fetchOptions) {
        return asFacade.searchOperationExecutions(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<DataStore> searchDataStores(DataStoreSearchCriteria searchCriteria, DataStoreFetchOptions fetchOptions) {
        return asFacade.searchDataStores(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(SemanticAnnotationSearchCriteria searchCriteria, SemanticAnnotationFetchOptions fetchOptions) {
        return asFacade.searchSemanticAnnotations(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<PropertyType> searchPropertyTypes(PropertyTypeSearchCriteria searchCriteria, PropertyTypeFetchOptions fetchOptions) {
        return asFacade.searchPropertyTypes(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<PropertyAssignment> searchPropertyAssignments(PropertyAssignmentSearchCriteria searchCriteria, PropertyAssignmentFetchOptions fetchOptions) {
        return asFacade.searchPropertyAssignments(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Query> searchQueries(QuerySearchCriteria searchCriteria, QueryFetchOptions fetchOptions) {
        return asFacade.searchQueries(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<QueryDatabase> searchQueryDatabases(QueryDatabaseSearchCriteria searchCriteria, QueryDatabaseFetchOptions fetchOptions) {
        return asFacade.searchQueryDatabases(sessionToken, searchCriteria, fetchOptions);
    }

    public void deleteSpaces(List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions) {
        asFacade.deleteSpaces(sessionToken, spaceIds, deletionOptions);
    }

    public void deleteProjects(List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions) {
        asFacade.deleteProjects(sessionToken, projectIds, deletionOptions);
    }

    public IDeletionId deleteExperiments(List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions) {
        return asFacade.deleteExperiments(sessionToken, experimentIds, deletionOptions);
    }

    public IDeletionId deleteSamples(List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions) {
        return asFacade.deleteSamples(sessionToken, sampleIds, deletionOptions);
    }

    public IDeletionId deleteDataSets(List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions) {
        return asFacade.deleteDataSets(sessionToken, dataSetIds, deletionOptions);
    }

    public void deleteMaterials(List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions) {
        asFacade.deleteMaterials(sessionToken, materialIds, deletionOptions);
    }

    public void deletePlugins(List<? extends IPluginId> pluginIds, PluginDeletionOptions deletionOptions) {
        asFacade.deletePlugins(sessionToken, pluginIds, deletionOptions);
    }

    public void deletePropertyTypes(List<? extends IPropertyTypeId> propertyTypeIds, PropertyTypeDeletionOptions deletionOptions) {
        asFacade.deletePropertyTypes(sessionToken, propertyTypeIds, deletionOptions);
    }

    public void deleteVocabularies(List<? extends IVocabularyId> ids, VocabularyDeletionOptions deletionOptions) {
        asFacade.deleteVocabularies(sessionToken, ids, deletionOptions);
    }

    public void deleteVocabularyTerms(List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions) {
        asFacade.deleteVocabularyTerms(sessionToken, termIds, deletionOptions);
    }

    public void deleteExperimentTypes(List<? extends IEntityTypeId> experimentTypeIds, ExperimentTypeDeletionOptions deletionOptions) {
        asFacade.deleteExperimentTypes(sessionToken, experimentTypeIds, deletionOptions);
    }

    public void deleteSampleTypes(List<? extends IEntityTypeId> sampleTypeIds, SampleTypeDeletionOptions deletionOptions) {
        asFacade.deleteSampleTypes(sessionToken, sampleTypeIds, deletionOptions);
    }

    public void deleteDataSetTypes(List<? extends IEntityTypeId> dataSetTypeIds, DataSetTypeDeletionOptions deletionOptions) {
        asFacade.deleteDataSetTypes(sessionToken, dataSetTypeIds, deletionOptions);
    }

    public void deleteMaterialTypes(List<? extends IEntityTypeId> materialTypeIds, MaterialTypeDeletionOptions deletionOptions) {
        asFacade.deleteMaterialTypes(sessionToken, materialTypeIds, deletionOptions);
    }

    public void deleteExternalDataManagementSystems(List<? extends IExternalDmsId> externalDmsIds, ExternalDmsDeletionOptions deletionOptions) {
        asFacade.deleteExternalDataManagementSystems(sessionToken, externalDmsIds, deletionOptions);
    }

    public void deleteTags(List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions) {
        asFacade.deleteTags(sessionToken, tagIds, deletionOptions);
    }

    public void deleteAuthorizationGroups(List<? extends IAuthorizationGroupId> groupIds, AuthorizationGroupDeletionOptions deletionOptions) {
        asFacade.deleteAuthorizationGroups(sessionToken, groupIds, deletionOptions);
    }

    public void deleteRoleAssignments(List<? extends IRoleAssignmentId> assignmentIds, RoleAssignmentDeletionOptions deletionOptions) {
        asFacade.deleteRoleAssignments(sessionToken, assignmentIds, deletionOptions);
    }

    public void deleteOperationExecutions(List<? extends IOperationExecutionId> executionIds, OperationExecutionDeletionOptions deletionOptions) {
        asFacade.deleteOperationExecutions(sessionToken, executionIds, deletionOptions);
    }

    public void deleteSemanticAnnotations(List<? extends ISemanticAnnotationId> annotationIds, SemanticAnnotationDeletionOptions deletionOptions) {
        asFacade.deleteSemanticAnnotations(sessionToken, annotationIds, deletionOptions);
    }

    public void deleteQueries(List<? extends IQueryId> queryIds, QueryDeletionOptions deletionOptions) {
        asFacade.deleteQueries(sessionToken, queryIds, deletionOptions);
    }

    public void deletePersons(List<? extends IPersonId> personIds, PersonDeletionOptions deletionOptions) {
        asFacade.deletePersons(sessionToken, personIds, deletionOptions);
    }

    public void deletePersonalAccessTokens(List<? extends IPersonalAccessTokenId> personalAccessTokenIds, PersonalAccessTokenDeletionOptions deletionOptions) {
        asFacade.deletePersonalAccessTokens(sessionToken, personalAccessTokenIds, deletionOptions);
    }

    public SearchResult<Deletion> searchDeletions(DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions) {
        return asFacade.searchDeletions(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<Event> searchEvents(EventSearchCriteria searchCriteria, EventFetchOptions fetchOptions) {
        return asFacade.searchEvents(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<PersonalAccessToken> searchPersonalAccessTokens(PersonalAccessTokenSearchCriteria searchCriteria, PersonalAccessTokenFetchOptions fetchOptions) {
        return asFacade.searchPersonalAccessTokens(sessionToken, searchCriteria, fetchOptions);
    }

    public SearchResult<SessionInformation> searchSessionInformation(SessionInformationSearchCriteria searchCriteria, SessionInformationFetchOptions fetchOptions) {
        return asFacade.searchSessionInformation(sessionToken, searchCriteria, fetchOptions);
    }

    public void revertDeletions(List<? extends IDeletionId> deletionIds) {
        asFacade.revertDeletions(sessionToken, deletionIds);
    }

    public void confirmDeletions(List<? extends IDeletionId> deletionIds) {
        asFacade.confirmDeletions(sessionToken, deletionIds);
    }

    public Object executeCustomASService(ICustomASServiceId serviceId, CustomASServiceExecutionOptions options) {
        return asFacade.executeCustomASService(sessionToken, serviceId, options);
    }

    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(SearchDomainServiceExecutionOptions options) {
        return asFacade.executeSearchDomainService(sessionToken, options);
    }

    public TableModel executeAggregationService(IDssServiceId serviceId, AggregationServiceExecutionOptions options) {
        return asFacade.executeAggregationService(sessionToken, serviceId, options);
    }

    public TableModel executeReportingService(IDssServiceId serviceId, ReportingServiceExecutionOptions options) {
        return asFacade.executeReportingService(sessionToken, serviceId, options);
    }

    public void executeProcessingService(IDssServiceId serviceId, ProcessingServiceExecutionOptions options) {
        asFacade.executeProcessingService(sessionToken, serviceId, options);
    }

    public TableModel executeQuery(IQueryId queryId, QueryExecutionOptions options) {
        return asFacade.executeQuery(sessionToken, queryId, options);
    }

    public TableModel executeSql(String sql, SqlExecutionOptions options) {
        return asFacade.executeSql(sessionToken, sql, options);
    }

    public PluginEvaluationResult evaluatePlugin(PluginEvaluationOptions options) {
        return asFacade.evaluatePlugin(sessionToken, options);
    }

    public void archiveDataSets(List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options) {
        asFacade.archiveDataSets(sessionToken, dataSetIds, options);
    }

    public void unarchiveDataSets(List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options) {
        asFacade.unarchiveDataSets(sessionToken, dataSetIds, options);
    }

    public void lockDataSets(List<? extends IDataSetId> dataSetIds, DataSetLockOptions options) {
        asFacade.lockDataSets(sessionToken, dataSetIds, options);
    }

    public void unlockDataSets(List<? extends IDataSetId> dataSetIds, DataSetUnlockOptions options) {
        asFacade.unlockDataSets(sessionToken, dataSetIds, options);
    }

    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations, IOperationExecutionOptions options) {
        return asFacade.executeOperations(sessionToken, operations, options);
    }

    public Map<String, String> getServerInformation() {
        return asFacade.getServerInformation(sessionToken);
    }

    public Map<String, String> getServerPublicInformation() {
        return asFacade.getServerPublicInformation();
    }

    public List<String> createPermIdStrings(int count) {
        return asFacade.createPermIdStrings(sessionToken, count);
    }

    public List<String> createCodes(String prefix, EntityKind entityKind, int count) {
        return asFacade.createCodes(sessionToken, prefix, entityKind, count);
    }

    //
    // DSS Facade methods
    //

    public SearchResult<DataSetFile> searchFiles(DataSetFileSearchCriteria searchCriteria, DataSetFileFetchOptions fetchOptions) {
        return dssFacade.searchFiles(sessionToken, searchCriteria, fetchOptions);
    }

    public InputStream downloadFiles(List<? extends IDataSetFileId> fileIds, DataSetFileDownloadOptions downloadOptions) {
        return dssFacade.downloadFiles(sessionToken, fileIds, downloadOptions);
    }

    public FastDownloadSession createFastDownloadSession(List<? extends IDataSetFileId> fileIds, FastDownloadSessionOptions options) {
        return dssFacade.createFastDownloadSession(sessionToken, fileIds, options);
    }

    public DataSetPermId createUploadedDataSet(final UploadedDataSetCreation newDataSet)
    {
        return dssFacade.createUploadedDataSet(sessionToken, newDataSet);
    }

    public List<DataSetPermId> createDataSetsDSS(List<FullDataSetCreation> newDataSets) {
        return dssFacade.createDataSets(sessionToken, newDataSets);
    }

    public Object executeCustomDSSService(ICustomDSSServiceId serviceId, CustomDSSServiceExecutionOptions options) {
        return dssFacade.executeCustomDSSService(sessionToken, serviceId, options);
    }

    //
    // Facade only methods
    //

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public String uploadFileWorkspaceDSS(final Path fileOrFolder)
    {
        String uploadId = uploadFileWorkspaceDSSEmptyDir(UUID.randomUUID().toString());
        uploadFileWorkspaceDSS(fileOrFolder.toFile(), uploadId);
        return uploadId;
    }

    /**
     * This utility method returns a well managed personal access token, creating one if no one is found and renews it if is close to expiration.
     * Requires are real session token since it uses other methods.
     *
     * @throws UserFailureException in case of any problems
     */
    public PersonalAccessTokenPermId getManagedPersonalAccessToken(String sessionName)
    {
        final int SECONDS_PER_DAY = 24 * 60 * 60;

        // Obtain servers renewal information
        Map<String, String> information = asFacade.getServerInformation(sessionToken);
        int personalAccessTokensRenewalPeriodInSeconds = Integer.parseInt(information.get(ServerInformation.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD));
        int personalAccessTokensRenewalPeriodInDays = personalAccessTokensRenewalPeriodInSeconds / SECONDS_PER_DAY;
        int personalAccessTokensMaxValidityPeriodInSeconds = Integer.parseInt(information.get(ServerInformation.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD));
        int personalAccessTokensMaxValidityPeriodInDays = personalAccessTokensMaxValidityPeriodInSeconds / SECONDS_PER_DAY;

        // Obtain user id
        SessionInformation sessionInformation = asFacade.getSessionInformation(sessionToken);

        // Search for PAT for this user and application
        // NOTE: Standard users only get their PAT but admins get all, filtering with the user solves this corner case
        PersonalAccessTokenSearchCriteria personalAccessTokenSearchCriteria = new PersonalAccessTokenSearchCriteria();
        personalAccessTokenSearchCriteria.withSessionName().thatEquals(sessionName);
        personalAccessTokenSearchCriteria.withOwner().withUserId().thatEquals(sessionInformation.getPerson().getUserId());

        SearchResult<PersonalAccessToken> personalAccessTokenSearchResult = asFacade.searchPersonalAccessTokens(sessionToken, personalAccessTokenSearchCriteria, new PersonalAccessTokenFetchOptions());
        PersonalAccessToken bestTokenFound = null;
        PersonalAccessTokenPermId bestTokenFoundPermId = null;

        // Obtain longer lasting application token
        for (PersonalAccessToken personalAccessToken : personalAccessTokenSearchResult.getObjects())
        {
            if (personalAccessToken.getValidToDate().after(new Date()))
            {
                if (bestTokenFound == null)
                {
                    bestTokenFound = personalAccessToken;
                } else if (personalAccessToken.getValidToDate().after(bestTokenFound.getValidToDate()))
                {
                    bestTokenFound = personalAccessToken;
                }
            }
        }

        // If best token doesn't exist, create
        if (bestTokenFound == null)
        {
            bestTokenFoundPermId = createManagedPersonalAccessToken(sessionName, personalAccessTokensMaxValidityPeriodInDays);
        }

        // If best token is going to expire in less than the warning period, renew
        Calendar renewalDate = Calendar.getInstance();
        renewalDate.add(Calendar.DAY_OF_MONTH, personalAccessTokensRenewalPeriodInDays);
        if (bestTokenFound != null && bestTokenFound.getValidToDate().before(renewalDate.getTime()))
        {
            bestTokenFoundPermId = createManagedPersonalAccessToken(sessionName, personalAccessTokensMaxValidityPeriodInDays);
        }

        // If we have not created or renewed, return current
        if (bestTokenFoundPermId == null)
        {
            bestTokenFoundPermId = bestTokenFound.getPermId();
        }

        return bestTokenFoundPermId;
    }

    /**
     * This utility method provides a simplified API to create subject semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticSubjectCreation(    EntityKind subjectEntityKind,
                                                                            String subjectClass,
                                                                            String subjectClassOntologyId,
                                                                            String subjectClassOntologyVersion,
                                                                            String subjectClassId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        semanticAnnotationCreation.setEntityTypeId(new EntityTypePermId(subjectClass, subjectEntityKind));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(subjectClassOntologyId);
        semanticAnnotationCreation.setDescriptorOntologyId(subjectClassOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(subjectClassOntologyVersion);
        semanticAnnotationCreation.setDescriptorOntologyVersion(subjectClassOntologyVersion);
        // Ontology Class URL
        semanticAnnotationCreation.setPredicateAccessionId(subjectClassId);
        semanticAnnotationCreation.setDescriptorAccessionId(subjectClassId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to create predicate semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticPredicateWithSubjectCreation( EntityKind subjectEntityKind,
                                                                                      String subjectClass,
                                                                                      String predicateProperty,
                                                                                      String predicatePropertyOntologyId,
                                                                                      String predicatePropertyOntologyVersion,
                                                                                      String predicatePropertyId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        // Predicate: Property matching an ontology class property
        semanticAnnotationCreation.setPropertyAssignmentId(new PropertyAssignmentPermId(
                new EntityTypePermId(subjectClass, subjectEntityKind),
                new PropertyTypePermId(predicateProperty)));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(predicatePropertyOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(predicatePropertyOntologyVersion);
        // Ontology Property URL
        semanticAnnotationCreation.setPredicateAccessionId(predicatePropertyId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to create predicate semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticPredicateCreation( String predicateProperty,
                                                                           String predicatePropertyOntologyId,
                                                                           String predicatePropertyOntologyVersion,
                                                                           String predicatePropertyId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Predicate: Property matching an ontology class property
        semanticAnnotationCreation.setPropertyTypeId(new PropertyTypePermId(predicateProperty));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(predicatePropertyOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(predicatePropertyOntologyVersion);
        // Ontology Property URL
        semanticAnnotationCreation.setPredicateAccessionId(predicatePropertyId);
        return semanticAnnotationCreation;
    }

    //
    // Internal Helper methods to create personal access tokens
    //

    private PersonalAccessTokenPermId createManagedPersonalAccessToken(String applicationName,
                                                                       int personalAccessTokensMaxValidityPeriodInDays)
    {
        final long SECONDS_PER_DAY = 24 * 60 * 60;
        final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;

        PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
        creation.setSessionName(applicationName);
        creation.setValidFromDate(new Date(System.currentTimeMillis() - MILLIS_PER_DAY));
        creation.setValidToDate(new Date(System.currentTimeMillis() + MILLIS_PER_DAY * personalAccessTokensMaxValidityPeriodInDays));
        List<PersonalAccessTokenPermId> personalAccessTokens = asFacade.createPersonalAccessTokens(sessionToken, List.of(creation));
        return personalAccessTokens.get(0);
    }

    //
    // Internal Helper Methods to upload files to DSS Session Workspace
    //

    /**
     * Upload file or folder to the DSS SessionWorkspaceFileUploadServlet and return the ID to be used by createUploadedDataSet
     * This method hides the complexities of uploading a folder with many files and does the uploads in chunks.
     */
    private String uploadFileWorkspaceDSS(final File fileOrFolder, final String parentsOrNull)
    {
        if (fileOrFolder.exists() == false)
        {
            throw new UserFailureException("Path doesn't exist: " + fileOrFolder);
        }
        String fileNameOrFolderName = "";
        if (parentsOrNull != null)
        {
            fileNameOrFolderName = parentsOrNull + "/";
        }
        fileNameOrFolderName += fileOrFolder.getName();

        if (fileOrFolder.isDirectory())
        {
            uploadFileWorkspaceDSSEmptyDir(fileNameOrFolderName);
            for (File file:fileOrFolder.listFiles())
            {
                uploadFileWorkspaceDSS(file, fileNameOrFolderName);
            }
        } else {
            uploadFileWorkspaceDSSFile(fileNameOrFolderName, fileOrFolder);
        }
        return fileNameOrFolderName;
    }

    private String uploadFileWorkspaceDSSEmptyDir(String pathToDir) {
        final org.eclipse.jetty.client.HttpClient client = JettyHttpClientFactory.getHttpClient();
        final Request httpRequest = client.newRequest(dssURL + "/session_workspace_file_upload")
                .method(HttpMethod.POST);
        httpRequest.param("sessionID", sessionToken);
        httpRequest.param("id", "1");
        httpRequest.param("filename", pathToDir);
        httpRequest.param("startByte", Long.toString(0));
        httpRequest.param("endByte", Long.toString(0));
        httpRequest.param("size", Long.toString(0));
        httpRequest.param("emptyFolder", Boolean.TRUE.toString());

        try {
            final ContentResponse response = httpRequest.send();
            final int status = response.getStatus();
            if (status != 200)
            {
                throw new IOException(response.getContentAsString());
            }
        } catch (final IOException | TimeoutException | InterruptedException | ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        return pathToDir;
    }

    private String uploadFileWorkspaceDSSFile(String pathToFile, File file) {
        try {
            long start = 0;
            for (byte[] chunk : streamFile(file, CHUNK_SIZE)) {
                final long end = start + chunk.length;

                final org.eclipse.jetty.client.HttpClient client = JettyHttpClientFactory.getHttpClient();
                final Request httpRequest = client.newRequest(dssURL + "/session_workspace_file_upload")
                        .method(HttpMethod.POST);
                httpRequest.param("sessionID", sessionToken);
                httpRequest.param("id", "1");
                httpRequest.param("filename", pathToFile);
                httpRequest.param("startByte", Long.toString(start));
                httpRequest.param("endByte", Long.toString(end));
                httpRequest.param("size", Long.toString(file.length()));
                httpRequest.content(new BytesContentProvider(chunk));
                final ContentResponse response = httpRequest.send();
                final int status = response.getStatus();
                if (status != 200) {
                    throw new IOException(response.getContentAsString());
                }
                start += CHUNK_SIZE;
            }
        } catch (final IOException | TimeoutException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return pathToFile;
    }

    private Iterable<byte[]> streamFile(final File file, final int chunkSize) throws FileNotFoundException
    {
        final InputStream inputStream = new FileInputStream(file);

        return new Iterable<byte[]>() {
            @Override
            public Iterator<byte[]> iterator() {
                return new Iterator<byte[]>() {
                    public boolean hasMore = true;

                    public boolean hasNext() {
                        return hasMore;
                    }

                    public byte[] next() {
                        try {
                            byte[] bytes = inputStream.readNBytes(chunkSize);
                            if (bytes.length < chunkSize) {
                                hasMore = false;
                                inputStream.close();
                            }
                            return bytes;
                        } catch (final IOException e) {
                            try {
                                inputStream.close();
                            } catch (final IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
}
