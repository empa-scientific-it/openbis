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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.DeleteAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.get.GetAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.get.GetAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.SearchAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.SearchAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.ArchiveDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.LockDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.UnarchiveDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.UnlockDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.SearchDataStoresOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.SearchDataStoresOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.confirm.ConfirmDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.revert.RevertDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreateCodesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreateCodesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreatePermIdsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreatePermIdsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.SearchEventsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.SearchEventsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.DeleteExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.DeleteExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.DeleteExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.CreateExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.CreateExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.DeleteExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.get.GetExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.get.GetExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.SearchExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.SearchExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.UpdateExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.ImportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.DeleteMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.DeleteMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.DeleteOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.UpdateOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.CreatePersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.CreatePersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.DeletePersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.get.GetPersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.get.GetPersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.SearchPersonalAccessTokensOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.UpdatePersonalAccessTokensOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.DeletePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.PersonDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.get.GetPersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.get.GetPersonsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.SearchPersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.SearchPersonsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.UpdatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.CreatePluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.CreatePluginsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.DeletePluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EvaluatePluginOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.EvaluatePluginOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.get.GetPluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.get.GetPluginsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.SearchPluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.SearchPluginsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.UpdatePluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.DeleteProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.UpdateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.CreatePropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.CreatePropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.DeletePropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.get.GetPropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.get.GetPropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.UpdatePropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.CreateQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.CreateQueriesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.DeleteQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteQueryOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteQueryOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteSqlOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.ExecuteSqlOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueriesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueryDatabasesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueryDatabasesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueriesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueryDatabasesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueryDatabasesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.UpdateQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.get.GetRightsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.get.GetRightsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.DeleteRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.get.GetRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.get.GetRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.SearchRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.SearchRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.CreateSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.CreateSemanticAnnotationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.DeleteSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.get.GetSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.get.GetSemanticAnnotationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SearchSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SearchSemanticAnnotationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.UpdateSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.AggregationService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ProcessingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteAggregationServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteAggregationServiceOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteProcessingServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteReportingServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteReportingServiceOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteSearchDomainServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteSearchDomainServiceOperationResult;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchAggregationServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchAggregationServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchProcessingServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchProcessingServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchReportingServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchReportingServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchSearchDomainServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchSearchDomainServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.get.GetSessionInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.get.GetSessionInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SearchSessionInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SearchSessionInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.DeleteSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.DeleteTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.UpdateTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabulariesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.DeleteVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.DeleteVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabulariesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabulariesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.UpdateVocabulariesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.UpdateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IExecuteOperationExecutor;
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenAware;
import ch.systemsx.cisd.openbis.common.pat.IPersonalAccessTokenInvocation;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
@Component(ApplicationServerApi.INTERNAL_SERVICE_NAME)
public class ApplicationServerApi extends AbstractServer<IApplicationServerApi> implements
        IApplicationServerInternalApi, IPersonalAccessTokenAware
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "application-server_INTERNAL";

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    @Autowired
    private IExecuteOperationExecutor executeOperationsExecutor;

    // Default constructor needed by Spring
    public ApplicationServerApi()
    {
    }

    ApplicationServerApi(IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
    }

    @Override
    @Transactional
    public String login(String userId, String password)
    {
        SessionContextDTO session = tryAuthenticate(userId, password);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    public String loginAsSystem()
    {
        return tryToAuthenticateAsSystem().getSessionToken();
    }

    @Override
    public void registerUser(String sessionToken)
    {
        tryToAuthenticate(sessionToken);
    }

    @Override
    @Transactional
    public String loginAsAnonymousUser()
    {
        SessionContextDTO session = tryAuthenticateAnonymously();
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    public String loginAs(String userId, String password, String asUserId)
    {
        SessionContextDTO session = tryAuthenticateAs(userId, password, asUserId);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> creations)
    {
        CreateSpacesOperationResult result = executeOperation(sessionToken, new CreateSpacesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> creations)
    {
        CreateProjectsOperationResult result = executeOperation(sessionToken, new CreateProjectsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<ExperimentPermId> createExperiments(String sessionToken,
            List<ExperimentCreation> creations)
    {
        CreateExperimentsOperationResult result = executeOperation(sessionToken, new CreateExperimentsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<EntityTypePermId> createExperimentTypes(String sessionToken,
            List<ExperimentTypeCreation> creations)
    {
        CreateExperimentTypesOperationResult result = executeOperation(sessionToken, new CreateExperimentTypesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<SamplePermId> createSamples(String sessionToken,
            List<SampleCreation> creations)
    {
        CreateSamplesOperationResult result = executeOperation(sessionToken, new CreateSamplesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<EntityTypePermId> createSampleTypes(String sessionToken,
            List<SampleTypeCreation> creations)
    {
        CreateSampleTypesOperationResult result = executeOperation(sessionToken, new CreateSampleTypesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> creations)
    {
        CreateDataSetsOperationResult result = executeOperation(sessionToken, new CreateDataSetsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<EntityTypePermId> createDataSetTypes(String sessionToken, List<DataSetTypeCreation> creations)
    {
        CreateDataSetTypesOperationResult result = executeOperation(sessionToken, new CreateDataSetTypesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> creations)
    {
        CreateMaterialsOperationResult result = executeOperation(sessionToken, new CreateMaterialsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<EntityTypePermId> createMaterialTypes(String sessionToken, List<MaterialTypeCreation> creations)
    {
        CreateMaterialTypesOperationResult result = executeOperation(sessionToken, new CreateMaterialTypesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<PropertyTypePermId> createPropertyTypes(String sessionToken, List<PropertyTypeCreation> newPropertyTypes)
    {
        CreatePropertyTypesOperationResult result = executeOperation(sessionToken, new CreatePropertyTypesOperation(newPropertyTypes));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<PluginPermId> createPlugins(String sessionToken, List<PluginCreation> newPlugins)
    {
        CreatePluginsOperationResult result = executeOperation(sessionToken, new CreatePluginsOperation(newPlugins));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<VocabularyPermId> createVocabularies(String sessionToken, List<VocabularyCreation> creations)
    {
        CreateVocabulariesOperationResult result = executeOperation(sessionToken, new CreateVocabulariesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> creations)
    {
        CreateVocabularyTermsOperationResult result = executeOperation(sessionToken, new CreateVocabularyTermsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> creations)
    {
        CreateTagsOperationResult result = executeOperation(sessionToken, new CreateTagsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<AuthorizationGroupPermId> createAuthorizationGroups(String sessionToken, List<AuthorizationGroupCreation> creations)
    {
        CreateAuthorizationGroupsOperationResult result = executeOperation(sessionToken, new CreateAuthorizationGroupsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<RoleAssignmentTechId> createRoleAssignments(String sessionToken, List<RoleAssignmentCreation> newRoleAssignments)
    {
        CreateRoleAssignmentsOperationResult result = executeOperation(sessionToken, new CreateRoleAssignmentsOperation(newRoleAssignments));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<PersonPermId> createPersons(String sessionToken, List<PersonCreation> newPersons)
    {
        CreatePersonsOperationResult result = executeOperation(sessionToken, new CreatePersonsOperation(newPersons));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String sessionToken,
            List<ExternalDmsCreation> creations)
    {
        CreateExternalDmsOperationResult result = executeOperation(sessionToken, new CreateExternalDmsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<SemanticAnnotationPermId> createSemanticAnnotations(String sessionToken, List<SemanticAnnotationCreation> creations)
    {
        CreateSemanticAnnotationsOperationResult result = executeOperation(sessionToken, new CreateSemanticAnnotationsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<QueryTechId> createQueries(String sessionToken, List<QueryCreation> creations)
    {
        CreateQueriesOperationResult result = executeOperation(sessionToken, new CreateQueriesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional public List<PersonalAccessTokenPermId> createPersonalAccessTokens(final String sessionToken,
            final List<PersonalAccessTokenCreation> creations)
    {
        CreatePersonalAccessTokensOperationResult result = executeOperation(sessionToken, new CreatePersonalAccessTokensOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public void updateSpaces(String sessionToken, List<SpaceUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateSpacesOperation(updates));
    }

    @Override
    @Transactional
    public void updateProjects(String sessionToken, List<ProjectUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateProjectsOperation(updates));
    }

    @Override
    @Transactional
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateExperimentsOperation(updates));
    }

    @Override
    @Transactional
    public void updateExperimentTypes(String sessionToken, List<ExperimentTypeUpdate> experimentTypeUpdates)
    {
        executeOperation(sessionToken, new UpdateExperimentTypesOperation(experimentTypeUpdates));
    }

    @Override
    @Transactional
    public void updateSamples(String sessionToken, List<SampleUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateSamplesOperation(updates));
    }

    @Override
    @Transactional
    public void updateSampleTypes(String sessionToken, List<SampleTypeUpdate> sampleTypeUpdates)
    {
        executeOperation(sessionToken, new UpdateSampleTypesOperation(sampleTypeUpdates));
    }

    @Override
    @Transactional
    public void updateMaterials(String sessionToken, List<MaterialUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateMaterialsOperation(updates));
    }

    @Override
    @Transactional
    public void updateMaterialTypes(String sessionToken, List<MaterialTypeUpdate> materialTypeUpdates)
    {
        executeOperation(sessionToken, new UpdateMaterialTypesOperation(materialTypeUpdates));
    }

    @Override
    @Transactional
    public void updateDataSets(String sessionToken, List<DataSetUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateDataSetsOperation(updates));
    }

    @Override
    @Transactional
    public void updateDataSetTypes(String sessionToken, List<DataSetTypeUpdate> dataSetTypeUpdates)
    {
        executeOperation(sessionToken, new UpdateDataSetTypesOperation(dataSetTypeUpdates));
    }

    @Override
    @Transactional
    public void updatePropertyTypes(String sessionToken, List<PropertyTypeUpdate> propertyTypeUpdates)
    {
        executeOperation(sessionToken, new UpdatePropertyTypesOperation(propertyTypeUpdates));
    }

    @Override
    @Transactional
    public void updatePlugins(String sessionToken, List<PluginUpdate> pluginUpdates)
    {
        executeOperation(sessionToken, new UpdatePluginsOperation(pluginUpdates));
    }

    @Override
    @Transactional
    public void updateVocabularies(String sessionToken, List<VocabularyUpdate> vocabularyUpdates)
    {
        executeOperation(sessionToken, new UpdateVocabulariesOperation(vocabularyUpdates));
    }

    @Override
    @Transactional
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateVocabularyTermsOperation(updates));
    }

    @Override
    @Transactional
    public void updateTags(String sessionToken, List<TagUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateTagsOperation(updates));
    }

    @Override
    @Transactional
    public void updateAuthorizationGroups(String sessionToken, List<AuthorizationGroupUpdate> authorizationGroupUpdates)
    {
        executeOperation(sessionToken, new UpdateAuthorizationGroupsOperation(authorizationGroupUpdates));
    }

    @Override
    @Transactional
    public void updatePersons(String sessionToken, List<PersonUpdate> personUpdates)
    {
        executeOperation(sessionToken, new UpdatePersonsOperation(personUpdates));
    }

    @Override
    @Transactional
    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateOperationExecutionsOperation(updates));
    }

    @Override
    @Transactional
    public void updateSemanticAnnotations(String sessionToken, List<SemanticAnnotationUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateSemanticAnnotationsOperation(updates));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IObjectId, Rights> getRights(String sessionToken, List<? extends IObjectId> ids, RightsFetchOptions fetchOptions)
    {
        GetRightsOperationResult result = executeOperation(sessionToken, new GetRightsOperation(ids, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        GetSpacesOperationResult result = executeOperation(sessionToken, new GetSpacesOperation(spaceIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        GetProjectsOperationResult result = executeOperation(sessionToken, new GetProjectsOperation(projectIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        GetExperimentsOperationResult result = executeOperation(sessionToken, new GetExperimentsOperation(experimentIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeFetchOptions fetchOptions)
    {
        GetExperimentTypesOperationResult result = executeOperation(sessionToken, new GetExperimentTypesOperation(experimentTypeIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ISampleId, Sample> getSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        GetSamplesOperationResult result = executeOperation(sessionToken, new GetSamplesOperation(sampleIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IEntityTypeId, SampleType> getSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds,
            SampleTypeFetchOptions fetchOptions)
    {
        GetSampleTypesOperationResult result = executeOperation(sessionToken, new GetSampleTypesOperation(sampleTypeIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        GetDataSetsOperationResult result = executeOperation(sessionToken, new GetDataSetsOperation(dataSetIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IEntityTypeId, DataSetType> getDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds,
            DataSetTypeFetchOptions fetchOptions)
    {
        GetDataSetTypesOperationResult result = executeOperation(sessionToken, new GetDataSetTypesOperation(dataSetTypeIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        GetMaterialsOperationResult result = executeOperation(sessionToken, new GetMaterialsOperation(materialIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IEntityTypeId, MaterialType> getMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds,
            MaterialTypeFetchOptions fetchOptions)
    {
        GetMaterialTypesOperationResult result = executeOperation(sessionToken, new GetMaterialTypesOperation(materialTypeIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(String sessionToken, List<? extends IPropertyTypeId> typeIds,
            PropertyTypeFetchOptions fetchOptions)
    {
        GetPropertyTypesOperationResult result = executeOperation(sessionToken, new GetPropertyTypesOperation(typeIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IPluginId, Plugin> getPlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginFetchOptions fetchOptions)
    {
        GetPluginsOperationResult result = executeOperation(sessionToken, new GetPluginsOperation(pluginIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IVocabularyId, Vocabulary> getVocabularies(String sessionToken, List<? extends IVocabularyId> vocabularyIds,
            VocabularyFetchOptions fetchOptions)
    {
        GetVocabulariesOperationResult result = executeOperation(sessionToken, new GetVocabulariesOperation(vocabularyIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions)
    {
        GetVocabularyTermsOperationResult result = executeOperation(sessionToken, new GetVocabularyTermsOperation(vocabularyTermIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions)
    {
        GetTagsOperationResult result = executeOperation(sessionToken, new GetTagsOperation(tagIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        GetAuthorizationGroupsOperationResult result = executeOperation(sessionToken, new GetAuthorizationGroupsOperation(groupIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> ids,
            RoleAssignmentFetchOptions fetchOptions)
    {
        GetRoleAssignmentsOperationResult result = executeOperation(sessionToken, new GetRoleAssignmentsOperation(ids, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IPersonId, Person> getPersons(String sessionToken, List<? extends IPersonId> ids, PersonFetchOptions fetchOptions)
    {
        GetPersonsOperationResult result = executeOperation(sessionToken, new GetPersonsOperation(ids, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions)
    {
        GetExternalDmsOperationResult result = executeOperation(sessionToken, new GetExternalDmsOperation(externalDmsIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(String sessionToken,
            List<? extends ISemanticAnnotationId> annotationIds, SemanticAnnotationFetchOptions fetchOptions)
    {
        GetSemanticAnnotationsOperationResult result =
                executeOperation(sessionToken, new GetSemanticAnnotationsOperation(annotationIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    public Map<IQueryId, Query> getQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryFetchOptions fetchOptions)
    {
        GetQueriesOperationResult result = executeOperation(sessionToken, new GetQueriesOperation(queryIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(String sessionToken, List<? extends IQueryDatabaseId> queryDatabaseIds,
            QueryDatabaseFetchOptions fetchOptions)
    {
        GetQueryDatabasesOperationResult result = executeOperation(sessionToken, new GetQueryDatabasesOperation(queryDatabaseIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(final String sessionToken,
            final List<? extends IPersonalAccessTokenId> personalAccessTokenIds, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        GetPersonalAccessTokensOperationResult result =
                executeOperation(sessionToken, new GetPersonalAccessTokensOperation(personalAccessTokenIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        SearchSpacesOperationResult result = executeOperation(sessionToken, new SearchSpacesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        SearchProjectsOperationResult result = executeOperation(sessionToken, new SearchProjectsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        SearchExperimentsOperationResult result = executeOperation(sessionToken, new SearchExperimentsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions)
    {
        SearchExperimentTypesOperationResult result =
                executeOperation(sessionToken, new SearchExperimentTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        SearchSamplesOperationResult result = executeOperation(sessionToken, new SearchSamplesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions)
    {
        SearchSampleTypesOperationResult result = executeOperation(sessionToken, new SearchSampleTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        SearchDataSetsOperationResult result = executeOperation(sessionToken, new SearchDataSetsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions)
    {
        SearchDataSetTypesOperationResult result = executeOperation(sessionToken, new SearchDataSetTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        SearchMaterialsOperationResult result = executeOperation(sessionToken, new SearchMaterialsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions)
    {
        SearchMaterialTypesOperationResult result = executeOperation(sessionToken, new SearchMaterialTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Plugin> searchPlugins(String sessionToken, PluginSearchCriteria searchCriteria, PluginFetchOptions fetchOptions)
    {
        SearchPluginsOperationResult result = executeOperation(sessionToken, new SearchPluginsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Vocabulary> searchVocabularies(String sessionToken, VocabularySearchCriteria searchCriteria,
            VocabularyFetchOptions fetchOptions)
    {
        SearchVocabulariesOperationResult result = executeOperation(sessionToken, new SearchVocabulariesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions)
    {
        SearchVocabularyTermsOperationResult result =
                executeOperation(sessionToken, new SearchVocabularyTermsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions)
    {
        SearchTagsOperationResult result = executeOperation(sessionToken, new SearchTagsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(String sessionToken, AuthorizationGroupSearchCriteria searchCriteria,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        SearchAuthorizationGroupsOperationResult result =
                executeOperation(sessionToken, new SearchAuthorizationGroupsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<RoleAssignment> searchRoleAssignments(String sessionToken, RoleAssignmentSearchCriteria searchCriteria,
            RoleAssignmentFetchOptions fetchOptions)
    {
        SearchRoleAssignmentsOperationResult result =
                executeOperation(sessionToken, new SearchRoleAssignmentsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Person> searchPersons(String sessionToken, PersonSearchCriteria searchCriteria, PersonFetchOptions fetchOptions)
    {
        SearchPersonsOperationResult result = executeOperation(sessionToken, new SearchPersonsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<DataStore> searchDataStores(String sessionToken, DataStoreSearchCriteria searchCriteria, DataStoreFetchOptions fetchOptions)
    {
        SearchDataStoresOperationResult result = executeOperation(sessionToken, new SearchDataStoresOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(String sessionToken, SemanticAnnotationSearchCriteria searchCriteria,
            SemanticAnnotationFetchOptions fetchOptions)
    {
        SearchSemanticAnnotationsOperationResult result =
                executeOperation(sessionToken, new SearchSemanticAnnotationsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<PropertyType> searchPropertyTypes(String sessionToken, PropertyTypeSearchCriteria searchCriteria,
            PropertyTypeFetchOptions fetchOptions)
    {
        SearchPropertyTypesOperationResult result =
                executeOperation(sessionToken, new SearchPropertyTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<PropertyAssignment> searchPropertyAssignments(String sessionToken, PropertyAssignmentSearchCriteria searchCriteria,
            PropertyAssignmentFetchOptions fetchOptions)
    {
        SearchPropertyAssignmentsOperationResult result =
                executeOperation(sessionToken, new SearchPropertyAssignmentsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteSpacesOperation(spaceIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteProjectsOperation(projectIds, deletionOptions));
    }

    @Override
    @Transactional
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        DeleteExperimentsOperationResult result = executeOperation(sessionToken, new DeleteExperimentsOperation(experimentIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        DeleteSamplesOperationResult result = executeOperation(sessionToken, new DeleteSamplesOperation(sampleIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        DeleteDataSetsOperationResult result = executeOperation(sessionToken, new DeleteDataSetsOperation(dataSetIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteMaterialsOperation(materialIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deletePlugins(String sessionToken, List<? extends IPluginId> pluginIds, PluginDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeletePluginsOperation(pluginIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deletePropertyTypes(String sessionToken, List<? extends IPropertyTypeId> propertyTypeIds, PropertyTypeDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeletePropertyTypesOperation(propertyTypeIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteVocabularies(String sessionToken, List<? extends IVocabularyId> ids, VocabularyDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteVocabulariesOperation(ids, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteVocabularyTermsOperation(termIds, deletionOptions));
    }

    @Override
    public void deleteExperimentTypes(String sessionToken, List<? extends IEntityTypeId> experimentTypeIds,
            ExperimentTypeDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteExperimentTypesOperation(experimentTypeIds, deletionOptions));
    }

    @Override
    public void deleteSampleTypes(String sessionToken, List<? extends IEntityTypeId> sampleTypeIds, SampleTypeDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteSampleTypesOperation(sampleTypeIds, deletionOptions));
    }

    @Override
    public void deleteDataSetTypes(String sessionToken, List<? extends IEntityTypeId> dataSetTypeIds, DataSetTypeDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteDataSetTypesOperation(dataSetTypeIds, deletionOptions));
    }

    @Override
    public void deleteMaterialTypes(String sessionToken, List<? extends IEntityTypeId> materialTypeIds, MaterialTypeDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteMaterialTypesOperation(materialTypeIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteTagsOperation(tagIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteAuthorizationGroups(String sessionToken, List<? extends IAuthorizationGroupId> groupIds,
            AuthorizationGroupDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteAuthorizationGroupsOperation(groupIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteRoleAssignments(String sessionToken, List<? extends IRoleAssignmentId> assignmentIds,
            RoleAssignmentDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteRoleAssignmentsOperation(assignmentIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteOperationExecutionsOperation(executionIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteSemanticAnnotations(String sessionToken, List<? extends ISemanticAnnotationId> annotationIds,
            SemanticAnnotationDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteSemanticAnnotationsOperation(annotationIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteQueries(String sessionToken, List<? extends IQueryId> queryIds, QueryDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteQueriesOperation(queryIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deletePersons(String sessionToken, List<? extends IPersonId> personIds, PersonDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeletePersonsOperation(personIds, deletionOptions));
    }

    @Override
    @Transactional public void deletePersonalAccessTokens(final String sessionToken,
            final List<? extends IPersonalAccessTokenId> personalAccessTokenIds,
            final PersonalAccessTokenDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeletePersonalAccessTokensOperation(personalAccessTokenIds, deletionOptions));
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions)
    {
        SearchDeletionsOperationResult result = executeOperation(sessionToken, new SearchDeletionsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override public SearchResult<Event> searchEvents(final String sessionToken, final EventSearchCriteria searchCriteria,
            final EventFetchOptions fetchOptions)
    {
        SearchEventsOperationResult result = executeOperation(sessionToken, new SearchEventsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        executeOperation(sessionToken, new RevertDeletionsOperation(deletionIds));
    }

    @Override
    @Transactional
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        executeOperation(sessionToken, new ConfirmDeletionsOperation(deletionIds));
    }

    @Override
    @Transactional
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        SearchCustomASServicesOperationResult result =
                executeOperation(sessionToken, new SearchCustomASServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    public SearchResult<SearchDomainService> searchSearchDomainServices(String sessionToken, SearchDomainServiceSearchCriteria searchCriteria,
            SearchDomainServiceFetchOptions fetchOptions)
    {
        SearchSearchDomainServicesOperationResult result =
                executeOperation(sessionToken, new SearchSearchDomainServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    public SearchResult<AggregationService> searchAggregationServices(String sessionToken, AggregationServiceSearchCriteria searchCriteria,
            AggregationServiceFetchOptions fetchOptions)
    {
        SearchAggregationServicesOperationResult result =
                executeOperation(sessionToken, new SearchAggregationServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    public SearchResult<ReportingService> searchReportingServices(String sessionToken, ReportingServiceSearchCriteria searchCriteria,
            ReportingServiceFetchOptions fetchOptions)
    {
        SearchReportingServicesOperationResult result =
                executeOperation(sessionToken, new SearchReportingServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    public SearchResult<ProcessingService> searchProcessingServices(String sessionToken, ProcessingServiceSearchCriteria searchCriteria,
            ProcessingServiceFetchOptions fetchOptions)
    {
        SearchProcessingServicesOperationResult result =
                executeOperation(sessionToken, new SearchProcessingServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions)
    {
        SearchObjectKindModificationsOperationResult result =
                executeOperation(sessionToken, new SearchObjectKindModificationsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        ExecuteCustomASServiceOperationResult result = executeOperation(sessionToken, new ExecuteCustomASServiceOperation(serviceId, options));
        return result.getResult();
    }

    @Override
    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(String sessionToken,
            SearchDomainServiceExecutionOptions options)
    {
        ExecuteSearchDomainServiceOperationResult result = executeOperation(sessionToken, new ExecuteSearchDomainServiceOperation(options));
        return result.getResult();
    }

    @Override
    public TableModel executeAggregationService(String sessionToken, IDssServiceId serviceId, AggregationServiceExecutionOptions options)
    {
        ExecuteAggregationServiceOperationResult result = executeOperation(sessionToken, new ExecuteAggregationServiceOperation(serviceId, options));
        return result.getResult();
    }

    @Override
    public TableModel executeReportingService(String sessionToken, IDssServiceId serviceId, ReportingServiceExecutionOptions options)
    {
        ExecuteReportingServiceOperationResult result = executeOperation(sessionToken, new ExecuteReportingServiceOperation(serviceId, options));
        return result.getResult();
    }

    @Override
    public void executeProcessingService(String sessionToken, IDssServiceId serviceId, ProcessingServiceExecutionOptions options)
    {
        executeOperation(sessionToken, new ExecuteProcessingServiceOperation(serviceId, options));
    }

    @Override
    public TableModel executeQuery(String sessionToken, IQueryId queryId, QueryExecutionOptions options)
    {
        ExecuteQueryOperationResult result = executeOperation(sessionToken, new ExecuteQueryOperation(queryId, options));
        return result.getResult();
    }

    @Override
    public TableModel executeSql(String sessionToken, String sql, SqlExecutionOptions options)
    {
        ExecuteSqlOperationResult result = executeOperation(sessionToken, new ExecuteSqlOperation(sql, options));
        return result.getResult();
    }

    @Override
    public PluginEvaluationResult evaluatePlugin(String sessionToken, PluginEvaluationOptions options)
    {
        EvaluatePluginOperationResult result = executeOperation(sessionToken, new EvaluatePluginOperation(options));
        return result.getResult();
    }

    @Override
    @Transactional
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        SearchGloballyOperationResult result = executeOperation(sessionToken, new SearchGloballyOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options)
    {
        executeOperation(sessionToken, new ArchiveDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional
    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options)
    {
        executeOperation(sessionToken, new UnarchiveDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional
    public void lockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetLockOptions options)
    {
        executeOperation(sessionToken, new LockDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional
    public void unlockDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnlockOptions options)
    {
        executeOperation(sessionToken, new UnlockDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionInformation getSessionInformation(String sessionToken)
    {
        GetSessionInformationOperationResult result = executeOperation(sessionToken, new GetSessionInformationOperation());
        return result.getSessionInformation();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionToken)
    {
        return sessionManager.isSessionActive(sessionToken);
    }

    @Override
    @Transactional
    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options)
    {
        Session session = sessionManager.getSession(sessionToken);
        return executeOperationsExecutor.execute(session, operations, options);
    }

    @Override
    @Transactional
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds,
            OperationExecutionFetchOptions fetchOptions)
    {
        GetOperationExecutionsOperationResult result =
                executeOperation(sessionToken, new GetOperationExecutionsOperation(executionIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional
    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions)
    {
        SearchOperationExecutionsOperationResult result =
                executeOperation(sessionToken, new SearchOperationExecutionsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @SuppressWarnings("unchecked")
    private <T extends IOperationResult> T executeOperation(String sessionToken, IOperation operation)
    {
        Session session = sessionManager.getSession(sessionToken);
        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) executeOperationsExecutor
                .execute(session, Collections.singletonList(operation), new SynchronousOperationExecutionOptions());
        return (T) results.getResults().get(0);
    }

    @SuppressWarnings("unchecked")
    private <T extends IOperationResult> T executeOperation(IOperation operation)
    {
        SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) executeOperationsExecutor
                .execute(null, Collections.singletonList(operation), new SynchronousOperationExecutionOptions());
        return (T) results.getResults().get(0);
    }

    @Override
    @Transactional
    public void updateExternalDataManagementSystems(String sessionToken, List<ExternalDmsUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateExternalDmsOperation(updates));
    }

    @Override
    @Transactional
    public void updateQueries(String sessionToken, List<QueryUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateQueriesOperation(updates));
    }

    @Override @Transactional public void updatePersonalAccessTokens(final String sessionToken,
            final List<PersonalAccessTokenUpdate> updates)
    {
        executeOperation(sessionToken, new UpdatePersonalAccessTokensOperation(updates));
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String sessionToken, ExternalDmsSearchCriteria searchCriteria,
            ExternalDmsFetchOptions fetchOptions)
    {
        SearchExternalDmsOperationResult result = executeOperation(sessionToken, new SearchExternalDmsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Query> searchQueries(String sessionToken, QuerySearchCriteria searchCriteria, QueryFetchOptions fetchOptions)
    {
        SearchQueriesOperationResult result = executeOperation(sessionToken, new SearchQueriesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<QueryDatabase> searchQueryDatabases(String sessionToken, QueryDatabaseSearchCriteria searchCriteria,
            QueryDatabaseFetchOptions fetchOptions)
    {
        SearchQueryDatabasesOperationResult result = executeOperation(sessionToken, new SearchQueryDatabasesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Transactional(readOnly = true)
    @Override public SearchResult<PersonalAccessToken> searchPersonalAccessTokens(final String sessionToken,
            final PersonalAccessTokenSearchCriteria searchCriteria, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        SearchPersonalAccessTokensOperationResult
                result = executeOperation(sessionToken, new SearchPersonalAccessTokensOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Transactional(readOnly = true)
    @Override public SearchResult<SessionInformation> searchSessionInformation(final String sessionToken,
            final SessionInformationSearchCriteria searchCriteria, final SessionInformationFetchOptions fetchOptions)
    {
        SearchSessionInformationOperationResult
                result = executeOperation(sessionToken, new SearchSessionInformationOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void deleteExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteExternalDmsOperation(externalDmsIds, deletionOptions));
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        GetServerInformationOperationResult result = executeOperation(sessionToken, new GetServerInformationOperation());
        return result.getServerInformation();
    }

    @Override
    public Map<String, String> getServerPublicInformation()
    {
        GetServerPublicInformationOperationResult result = executeOperation(new GetServerPublicInformationOperation());
        return result.getServerInformation();
    }

    @Override
    @Transactional
    public List<String> createPermIdStrings(String sessionToken, int count)
    {
        CreatePermIdsOperationResult result = executeOperation(sessionToken, new CreatePermIdsOperation(count));
        return result.getPermIds();
    }

    @Override
    @Transactional
    public List<String> createCodes(String sessionToken, String prefix, EntityKind entityKind, int count)
    {
        CreateCodesOperationResult result = executeOperation(sessionToken, new CreateCodesOperation(prefix, entityKind, count));
        return result.getCodes();
    }

    @Override
    public void executeImport(final String sessionToken, final IImportData importData, final ImportOptions importOptions)
    {
        executeOperation(sessionToken, new ImportOperation(importData, importOptions));
    }

    @Override public IApplicationServerApi createPersonalAccessTokenInvocationHandler(final IPersonalAccessTokenInvocation invocation)
    {
        return new ApplicationServerApiPersonalAccessTokenInvocationHandler(invocation);
    }

    @Override
    public IApplicationServerApi createLogger(IInvocationLoggerContext context)
    {
        return new ApplicationServerApiLogger(sessionManager, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 6;
    }

}
