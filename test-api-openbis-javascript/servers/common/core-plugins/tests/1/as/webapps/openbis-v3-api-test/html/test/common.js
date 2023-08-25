define([ 'jquery', 'openbis', 'underscore', 'test/dtos' ], function($, defaultOpenbis, _, dtos) {

	/*
	 * These tests should be run against openBIS instance with screening sprint
	 * server database version
	 */

	var testProtocol = window.location.protocol;
	var testHost = window.location.hostname;
	var testPort = window.location.port;
	var testUrl = testProtocol + "//" + testHost + ":" + testPort;
	var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

	var testUserId = "openbis_test_js";
	var testUserPassword = "password";

	var Common = function(assert, openbis) {
		this.assert = assert;

		if (!openbis) {
			openbis = defaultOpenbis;
		}

		this.SpaceCreation = dtos.SpaceCreation;
		this.ProjectCreation = dtos.ProjectCreation;
		this.ExperimentCreation = dtos.ExperimentCreation;
		this.SampleCreation = dtos.SampleCreation;
		this.MaterialCreation = dtos.MaterialCreation;
		this.AttachmentCreation = dtos.AttachmentCreation;
		this.VocabularyCreation = dtos.VocabularyCreation;
		this.VocabularyTermCreation = dtos.VocabularyTermCreation;
		this.TagCreation = dtos.TagCreation;
		this.AuthorizationGroupCreation = dtos.AuthorizationGroupCreation;
		this.RoleAssignmentCreation = dtos.RoleAssignmentCreation;
		this.PersonCreation = dtos.PersonCreation;
		this.Role = require('as/dto/roleassignment/Role');
		this.RoleLevel = require('as/dto/roleassignment/RoleLevel');
		this.DataType = require('as/dto/property/DataType');
		this.EntityKind = require('as/dto/entitytype/EntityKind');
		this.DataSetKind = require('as/dto/dataset/DataSetKind');
		this.PluginKind = require('as/dto/plugin/PluginKind');
		this.PluginType = require('as/dto/plugin/PluginType');
		this.SemanticAnnotationCreation = dtos.SemanticAnnotationCreation;
		this.DataSetCreation = dtos.DataSetCreation;
		this.FullDataSetCreation = dtos.FullDataSetCreation;
		this.UploadedDataSetCreation = dtos.UploadedDataSetCreation;
		this.DataSetFileCreation = dtos.DataSetFileCreation;
		this.LinkedDataCreation = dtos.LinkedDataCreation;
		this.ContentCopyCreation = dtos.ContentCopyCreation;
		this.ExternalDmsCreation = dtos.ExternalDmsCreation;
		this.WebAppSettingCreation = dtos.WebAppSettingCreation;
		this.QueryCreation = dtos.QueryCreation;
		this.PersonalAccessTokenCreation = dtos.PersonalAccessTokenCreation;
		this.ExternalDmsAddressType = require('as/dto/externaldms/ExternalDmsAddressType');
		this.SpaceUpdate = dtos.SpaceUpdate;
		this.ProjectUpdate = dtos.ProjectUpdate;
		this.ExperimentTypeUpdate = dtos.ExperimentTypeUpdate;
		this.ExperimentUpdate = dtos.ExperimentUpdate;
		this.SampleTypeUpdate = dtos.SampleTypeUpdate;
		this.SampleUpdate = dtos.SampleUpdate;
		this.DataSetTypeUpdate = dtos.DataSetTypeUpdate;
		this.DataSetUpdate = dtos.DataSetUpdate;
		this.PhysicalDataUpdate = dtos.PhysicalDataUpdate;
		this.LinkedDataUpdate = dtos.LinkedDataUpdate;
		this.SemanticAnnotationUpdate = dtos.SemanticAnnotationUpdate;
		this.ContentCopyListUpdateValue = dtos.ContentCopyListUpdateValue;
		this.DataStorePermId = dtos.DataStorePermId;
		this.MaterialTypeUpdate = dtos.MaterialTypeUpdate;
		this.MaterialUpdate = dtos.MaterialUpdate;
		this.VocabularyUpdate = dtos.VocabularyUpdate;
		this.VocabularyTermUpdate = dtos.VocabularyTermUpdate;
		this.ExternalDmsUpdate = dtos.ExternalDmsUpdate;
		this.TagUpdate = dtos.TagUpdate;
		this.AuthorizationGroupUpdate = dtos.AuthorizationGroupUpdate;
		this.PersonUpdate = dtos.PersonUpdate;
		this.QueryUpdate = dtos.QueryUpdate;
		this.PersonalAccessTokenUpdate = dtos.PersonalAccessTokenUpdate;
		this.SpaceDeletionOptions = dtos.SpaceDeletionOptions;
		this.ProjectDeletionOptions = dtos.ProjectDeletionOptions;
		this.ExperimentDeletionOptions = dtos.ExperimentDeletionOptions;
		this.SampleDeletionOptions = dtos.SampleDeletionOptions;
		this.DataSetDeletionOptions = dtos.DataSetDeletionOptions;
		this.MaterialDeletionOptions = dtos.MaterialDeletionOptions;
		this.PluginDeletionOptions = dtos.PluginDeletionOptions;
		this.VocabularyTermDeletionOptions = dtos.VocabularyTermDeletionOptions;
		this.ExternalDmsDeletionOptions = dtos.ExternalDmsDeletionOptions;
		this.TagDeletionOptions = dtos.TagDeletionOptions;
		this.AuthorizationGroupDeletionOptions = dtos.AuthorizationGroupDeletionOptions;
		this.RoleAssignmentDeletionOptions = dtos.RoleAssignmentDeletionOptions;
		this.SemanticAnnotationDeletionOptions = dtos.SemanticAnnotationDeletionOptions;
		this.QueryDeletionOptions = dtos.QueryDeletionOptions;
		this.PersonalAccessTokenDeletionOptions = dtos.PersonalAccessTokenDeletionOptions;
		this.PersonDeletionOptions = dtos.PersonDeletionOptions;
		this.PersonPermId = dtos.PersonPermId;
		this.Me = dtos.Me;
		this.EntityTypePermId = dtos.EntityTypePermId;
		this.SpacePermId = dtos.SpacePermId;
		this.ProjectPermId = dtos.ProjectPermId;
		this.ProjectIdentifier = dtos.ProjectIdentifier;
		this.ExperimentPermId = dtos.ExperimentPermId;
		this.ExperimentIdentifier = dtos.ExperimentIdentifier;
		this.SamplePermId = dtos.SamplePermId;
		this.SampleIdentifier = dtos.SampleIdentifier;
		this.DataSetPermId = dtos.DataSetPermId;
		this.FileFormatTypePermId = dtos.FileFormatTypePermId;
		this.MaterialPermId = dtos.MaterialPermId;
		this.ContentCopyPermId = dtos.ContentCopyPermId;
		this.ExternalDmsPermId = dtos.ExternalDmsPermId;
		this.VocabularyPermId = dtos.VocabularyPermId;
		this.VocabularyTermPermId = dtos.VocabularyTermPermId;
		this.AuthorizationGroupPermId = dtos.AuthorizationGroupPermId;
		this.RoleAssignmentTechId = dtos.RoleAssignmentTechId;
		this.TagPermId = dtos.TagPermId;
		this.TagCode = dtos.TagCode;
		this.SemanticAnnotationsPermId = dtos.SemanticAnnotationsPermId;
		this.QueryName = dtos.QueryName;
		this.QueryType = require('as/dto/query/QueryType');
		this.QueryDatabaseName = dtos.QueryDatabaseName;
		this.PersonalAccessTokenPermId = dtos.PersonalAccessTokenPermId;
		this.SpaceSearchCriteria = dtos.SpaceSearchCriteria;
		this.ProjectSearchCriteria = dtos.ProjectSearchCriteria;
		this.ExperimentSearchCriteria = dtos.ExperimentSearchCriteria;
		this.ExperimentTypeSearchCriteria = dtos.ExperimentTypeSearchCriteria;
		this.SampleSearchCriteria = dtos.SampleSearchCriteria;
		this.SampleTypeSearchCriteria = dtos.SampleTypeSearchCriteria;
		this.DataSetSearchCriteria = dtos.DataSetSearchCriteria;
		this.DataSetTypeSearchCriteria = dtos.DataSetTypeSearchCriteria;
		this.MaterialSearchCriteria = dtos.MaterialSearchCriteria;
		this.MaterialTypeSearchCriteria = dtos.MaterialTypeSearchCriteria;
		this.ExternalDmsSearchCriteria = dtos.ExternalDmsSearchCriteria;
		this.PluginSearchCriteria = dtos.PluginSearchCriteria;
		this.VocabularySearchCriteria = dtos.VocabularySearchCriteria;
		this.VocabularyTermSearchCriteria = dtos.VocabularyTermSearchCriteria;
		this.DataSetFileSearchCriteria = dtos.DataSetFileSearchCriteria;
		this.TagSearchCriteria = dtos.TagSearchCriteria;
		this.AuthorizationGroupSearchCriteria = dtos.AuthorizationGroupSearchCriteria;
		this.RoleAssignmentSearchCriteria = dtos.RoleAssignmentSearchCriteria;
		this.PersonSearchCriteria = dtos.PersonSearchCriteria;
		this.DataStoreSearchCriteria = dtos.DataStoreSearchCriteria;
		this.PropertyTypeSearchCriteria = dtos.PropertyTypeSearchCriteria;
		this.PropertyAssignmentSearchCriteria = dtos.PropertyAssignmentSearchCriteria;
		this.SemanticAnnotationSearchCriteria = dtos.SemanticAnnotationSearchCriteria;
		this.QuerySearchCriteria = dtos.QuerySearchCriteria;
		this.QueryDatabaseSearchCriteria = dtos.QueryDatabaseSearchCriteria;
		this.PersonalAccessTokenSearchCriteria = dtos.PersonalAccessTokenSearchCriteria;
		this.SpaceFetchOptions = dtos.SpaceFetchOptions;
		this.ProjectFetchOptions = dtos.ProjectFetchOptions;
		this.ExperimentFetchOptions = dtos.ExperimentFetchOptions;
		this.ExperimentTypeFetchOptions = dtos.ExperimentTypeFetchOptions;
		this.SampleFetchOptions = dtos.SampleFetchOptions;
		this.SampleTypeFetchOptions = dtos.SampleTypeFetchOptions;
		this.DataSetFetchOptions = dtos.DataSetFetchOptions;
		this.DataSetTypeFetchOptions = dtos.DataSetTypeFetchOptions;
		this.MaterialFetchOptions = dtos.MaterialFetchOptions;
		this.MaterialTypeFetchOptions = dtos.MaterialTypeFetchOptions;
		this.ExternalDmsFetchOptions = dtos.ExternalDmsFetchOptions;
		this.VocabularyFetchOptions = dtos.VocabularyFetchOptions;
		this.VocabularyTermFetchOptions = dtos.VocabularyTermFetchOptions;
		this.TagFetchOptions = dtos.TagFetchOptions;
		this.AuthorizationGroupFetchOptions = dtos.AuthorizationGroupFetchOptions;
		this.RoleAssignmentFetchOptions = dtos.RoleAssignmentFetchOptions;
		this.PersonFetchOptions = dtos.PersonFetchOptions;
		this.PluginFetchOptions = dtos.PluginFetchOptions;
		this.PropertyTypeFetchOptions = dtos.PropertyTypeFetchOptions;
		this.PropertyAssignmentFetchOptions = dtos.PropertyAssignmentFetchOptions;
		this.SemanticAnnotationFetchOptions = dtos.SemanticAnnotationFetchOptions;
		this.QueryFetchOptions = dtos.QueryFetchOptions;
		this.QueryDatabaseFetchOptions = dtos.QueryDatabaseFetchOptions;
		this.PersonalAccessTokenFetchOptions = dtos.PersonalAccessTokenFetchOptions;
		this.DeletionFetchOptions = dtos.DeletionFetchOptions;
		this.DeletionSearchCriteria = dtos.DeletionSearchCriteria;
		this.EventFetchOptions = dtos.EventFetchOptions;
		this.EventSearchCriteria = dtos.EventSearchCriteria;
		this.EventType = require('as/dto/event/EventType');
		this.EntityType = require('as/dto/event/EntityType');
		this.CustomASServiceSearchCriteria = dtos.CustomASServiceSearchCriteria;
		this.CustomASServiceFetchOptions = dtos.CustomASServiceFetchOptions;
		this.CustomASServiceCode = dtos.CustomASServiceCode;
		this.DssServicePermId = dtos.DssServicePermId;
		this.CustomASServiceExecutionOptions = dtos.CustomASServiceExecutionOptions;
		this.SearchDomainServiceSearchCriteria = dtos.SearchDomainServiceSearchCriteria;
		this.SearchDomainServiceFetchOptions = dtos.SearchDomainServiceFetchOptions;
		this.SearchDomainServiceExecutionOptions = dtos.SearchDomainServiceExecutionOptions;
		this.AggregationServiceExecutionOptions = dtos.AggregationServiceExecutionOptions;
		this.AggregationServiceSearchCriteria = dtos.AggregationServiceSearchCriteria;
		this.AggregationServiceFetchOptions = dtos.AggregationServiceFetchOptions;
		this.ReportingServiceSearchCriteria = dtos.ReportingServiceSearchCriteria;
		this.ReportingServiceFetchOptions = dtos.ReportingServiceFetchOptions;
		this.ReportingServiceExecutionOptions = dtos.ReportingServiceExecutionOptions;
		this.Rights = dtos.Rights;
		this.RightsFetchOptions = dtos.RightsFetchOptions;
		this.ProcessingServiceSearchCriteria = dtos.ProcessingServiceSearchCriteria;
		this.ProcessingServiceFetchOptions = dtos.ProcessingServiceFetchOptions;
		this.ProcessingServiceExecutionOptions = dtos.ProcessingServiceExecutionOptions;
		this.QueryExecutionOptions = dtos.QueryExecutionOptions;
		this.SqlExecutionOptions = dtos.SqlExecutionOptions;
		this.EvaluatePluginOperation = dtos.EvaluatePluginOperation;
		this.DynamicPropertyPluginEvaluationOptions = dtos.DynamicPropertyPluginEvaluationOptions;
		this.EntityValidationPluginEvaluationOptions = dtos.EntityValidationPluginEvaluationOptions;
		this.GlobalSearchCriteria = dtos.GlobalSearchCriteria;
		this.GlobalSearchObjectFetchOptions = dtos.GlobalSearchObjectFetchOptions;
		this.ObjectKindModificationSearchCriteria = dtos.ObjectKindModificationSearchCriteria;
		this.ObjectKindModificationFetchOptions = dtos.ObjectKindModificationFetchOptions;
		this.DataSetArchiveOptions = dtos.DataSetArchiveOptions;
		this.DataSetUnarchiveOptions = dtos.DataSetUnarchiveOptions;
		this.DataSetLockOptions = dtos.DataSetLockOptions;
		this.DataSetUnlockOptions = dtos.DataSetUnlockOptions;
		this.PropertyAssignmentCreation = dtos.PropertyAssignmentCreation;
		this.PropertyTypePermId = dtos.PropertyTypePermId;
		this.PropertyAssignmentPermId = dtos.PropertyAssignmentPermId;
		this.PluginPermId = dtos.PluginPermId;
		this.Plugin = dtos.Plugin;
		this.PluginCreation = dtos.PluginCreation;
		this.ExperimentTypeCreation = dtos.ExperimentTypeCreation;
		this.SampleTypeCreation = dtos.SampleTypeCreation;
		this.DataSetTypeCreation = dtos.DataSetTypeCreation;
		this.MaterialTypeCreation = dtos.MaterialTypeCreation;
		this.PropertyTypeCreation = dtos.PropertyTypeCreation;
		this.PropertyTypeUpdate = dtos.PropertyTypeUpdate;
		this.PluginUpdate = dtos.PluginUpdate;
		this.WebAppSettings = dtos.WebAppSettings;

		this.VocabularyDeletionOptions = dtos.VocabularyDeletionOptions;
		this.VocabularyTermDeletionOptions = dtos.VocabularyTermDeletionOptions;

		// operations

		this.GetSessionInformationOperation = dtos.GetSessionInformationOperation;
		this.GetSpacesOperation = dtos.GetSpacesOperation;
		this.GetProjectsOperation = dtos.GetProjectsOperation;
		this.GetExperimentsOperation = dtos.GetExperimentsOperation;
		this.GetExperimentTypesOperation = dtos.GetExperimentTypesOperation;
		this.GetSamplesOperation = dtos.GetSamplesOperation;
		this.GetSampleTypesOperation = dtos.GetSampleTypesOperation;
		this.GetDataSetsOperation = dtos.GetDataSetsOperation;
		this.GetDataSetTypesOperation = dtos.GetDataSetTypesOperation;
		this.GetMaterialsOperation = dtos.GetMaterialsOperation;
		this.GetMaterialTypesOperation = dtos.GetMaterialTypesOperation;
		this.GetPluginsOperation = dtos.GetPluginsOperation;
		this.GetPropertyTypesOperation = dtos.GetPropertyTypesOperation;
		this.GetVocabulariesOperation = dtos.GetVocabulariesOperation;
		this.GetVocabularyTermsOperation = dtos.GetVocabularyTermsOperation;
		this.GetTagsOperation = dtos.GetTagsOperation;
		this.GetAuthorizationGroupsOperation = dtos.GetAuthorizationGroupsOperation;
		this.GetRoleAssignmentsOperation = dtos.GetRoleAssignmentsOperation;
		this.GetRightsOperation = dtos.GetRightsOperation;
		this.GetPersonsOperation = dtos.GetPersonsOperation;
		this.GetExternalDmsOperation = dtos.GetExternalDmsOperation;
		this.GetSemanticAnnotationsOperation = dtos.GetSemanticAnnotationsOperation;
		this.GetServerInformationOperation = dtos.GetServerInformationOperation;
		this.GetServerPublicInformationOperation = dtos.GetServerPublicInformationOperation;

		this.CreateSpacesOperation = dtos.CreateSpacesOperation;
		this.CreateProjectsOperation = dtos.CreateProjectsOperation;
		this.CreateExperimentsOperation = dtos.CreateExperimentsOperation;
		this.CreateExperimentTypesOperation = dtos.CreateExperimentTypesOperation;
		this.CreateSamplesOperation = dtos.CreateSamplesOperation;
		this.CreateSampleTypesOperation = dtos.CreateSampleTypesOperation;
		this.CreateDataSetsOperation = dtos.CreateDataSetsOperation;
		this.CreateDataSetTypesOperation = dtos.CreateDataSetTypesOperation;
		this.CreateMaterialsOperation = dtos.CreateMaterialsOperation;
		this.CreateMaterialTypesOperation = dtos.CreateMaterialTypesOperation;
		this.CreatePropertyTypesOperation = dtos.CreatePropertyTypesOperation;
		this.CreatePluginsOperation = dtos.CreatePluginsOperation;
		this.CreateVocabulariesOperation = dtos.CreateVocabulariesOperation;
		this.CreateVocabularyTermsOperation = dtos.CreateVocabularyTermsOperation;
		this.CreateTagsOperation = dtos.CreateTagsOperation;
		this.CreateAuthorizationGroupsOperation = dtos.CreateAuthorizationGroupsOperation;
		this.CreateRoleAssignmentsOperation = dtos.CreateRoleAssignmentsOperation;
		this.CreatePersonsOperation = dtos.CreatePersonsOperation;
		this.CreateSemanticAnnotationsOperation = dtos.CreateSemanticAnnotationsOperation;
		this.CreateExternalDmsOperation = dtos.CreateExternalDmsOperation;
		this.CreateQueriesOperation = dtos.CreateQueriesOperation;
		this.CreateCodesOperation = dtos.CreateCodesOperation;
		this.CreatePermIdsOperation = dtos.CreatePermIdsOperation;
		this.CreatePersonalAccessTokensOperation = dtos.CreatePersonalAccessTokensOperation;

		this.UpdateSpacesOperation = dtos.UpdateSpacesOperation;
		this.UpdateProjectsOperation = dtos.UpdateProjectsOperation;
		this.UpdateExperimentsOperation = dtos.UpdateExperimentsOperation;
		this.UpdateExperimentTypesOperation = dtos.UpdateExperimentTypesOperation;
		this.UpdateSamplesOperation = dtos.UpdateSamplesOperation;
		this.UpdateSampleTypesOperation = dtos.UpdateSampleTypesOperation;
		this.UpdateDataSetsOperation = dtos.UpdateDataSetsOperation;
		this.UpdateDataSetTypesOperation = dtos.UpdateDataSetTypesOperation;
		this.UpdateMaterialsOperation = dtos.UpdateMaterialsOperation;
		this.UpdateMaterialTypesOperation = dtos.UpdateMaterialTypesOperation;
		this.UpdatePropertyTypesOperation = dtos.UpdatePropertyTypesOperation;
		this.UpdatePluginsOperation = dtos.UpdatePluginsOperation;
		this.UpdateVocabulariesOperation = dtos.UpdateVocabulariesOperation;
		this.UpdateVocabularyTermsOperation = dtos.UpdateVocabularyTermsOperation;
		this.UpdateExternalDmsOperation = dtos.UpdateExternalDmsOperation;
		this.UpdateTagsOperation = dtos.UpdateTagsOperation;
		this.UpdateAuthorizationGroupsOperation = dtos.UpdateAuthorizationGroupsOperation;
		this.UpdatePersonsOperation = dtos.UpdatePersonsOperation;
		this.UpdateOperationExecutionsOperation = dtos.UpdateOperationExecutionsOperation;
		this.UpdateSemanticAnnotationsOperation = dtos.UpdateSemanticAnnotationsOperation;
		this.UpdateQueriesOperation = dtos.UpdateQueriesOperation;
		this.UpdatePersonalAccessTokensOperation = dtos.UpdatePersonalAccessTokensOperation;

		this.GetSpacesOperation = dtos.GetSpacesOperation;
		this.GetProjectsOperation = dtos.GetProjectsOperation;
		this.GetExperimentsOperation = dtos.GetExperimentsOperation;
		this.GetSamplesOperation = dtos.GetSamplesOperation;
		this.GetDataSetsOperation = dtos.GetDataSetsOperation;
		this.GetMaterialsOperation = dtos.GetMaterialsOperation;
		this.GetVocabularyTermsOperation = dtos.GetVocabularyTermsOperation;
		this.GetTagsOperation = dtos.GetTagsOperation;
		this.GetSemanticAnnotationsOperation = dtos.GetSemanticAnnotationsOperation;
		this.GetOperationExecutionsOperation = dtos.GetOperationExecutionsOperation;
		this.GetQueriesOperation = dtos.GetQueriesOperation;
		this.GetQueryDatabasesOperation = dtos.GetQueryDatabasesOperation;
		this.GetPersonalAccessTokensOperation = dtos.GetPersonalAccessTokensOperation;

		this.SearchSpacesOperation = dtos.SearchSpacesOperation;
		this.SearchProjectsOperation = dtos.SearchProjectsOperation;
		this.SearchExperimentsOperation = dtos.SearchExperimentsOperation;
		this.SearchExperimentTypesOperation = dtos.SearchExperimentTypesOperation;
		this.SearchSamplesOperation = dtos.SearchSamplesOperation;
		this.SearchSampleTypesOperation = dtos.SearchSampleTypesOperation;
		this.SearchDataSetsOperation = dtos.SearchDataSetsOperation;
		this.SearchDataSetTypesOperation = dtos.SearchDataSetTypesOperation;
		this.SearchMaterialsOperation = dtos.SearchMaterialsOperation;
		this.SearchMaterialTypesOperation = dtos.SearchMaterialTypesOperation;
		this.SearchPluginsOperation = dtos.SearchPluginsOperation;
		this.SearchVocabulariesOperation = dtos.SearchVocabulariesOperation;
		this.SearchVocabularyTermsOperation = dtos.SearchVocabularyTermsOperation;
		this.SearchExternalDmsOperation = dtos.SearchExternalDmsOperation;
		this.SearchTagsOperation = dtos.SearchTagsOperation;
		this.SearchAuthorizationGroupsOperation = dtos.SearchAuthorizationGroupsOperation;
		this.SearchRoleAssignmentsOperation = dtos.SearchRoleAssignmentsOperation;
		this.SearchPersonsOperation = dtos.SearchPersonsOperation;
		this.SearchCustomASServicesOperation = dtos.SearchCustomASServicesOperation;
		this.SearchSearchDomainServicesOperation = dtos.SearchSearchDomainServicesOperation;
		this.SearchAggregationServicesOperation = dtos.SearchAggregationServicesOperation;
		this.SearchReportingServicesOperation = dtos.SearchReportingServicesOperation;
		this.SearchProcessingServicesOperation = dtos.SearchProcessingServicesOperation;
		this.SearchObjectKindModificationsOperation = dtos.SearchObjectKindModificationsOperation;
		this.SearchGloballyOperation = dtos.SearchGloballyOperation;
		this.SearchOperationExecutionsOperation = dtos.SearchOperationExecutionsOperation;
		this.SearchDeletionsOperation = dtos.SearchDeletionsOperation;
		this.SearchEventsOperation = dtos.SearchEventsOperation;
		this.SearchDataStoresOperation = dtos.SearchDataStoresOperation;
		this.SearchPropertyTypesOperation = dtos.SearchPropertyTypesOperation;
		this.SearchPropertyAssignmentsOperation = dtos.SearchPropertyAssignmentsOperation;
		this.SearchSemanticAnnotationsOperation = dtos.SearchSemanticAnnotationsOperation;
		this.SearchQueriesOperation = dtos.SearchQueriesOperation;
		this.SearchQueryDatabasesOperation = dtos.SearchQueryDatabasesOperation;
		this.SearchPersonalAccessTokensOperation = dtos.SearchPersonalAccessTokensOperation;
		this.SearchSessionInformationOperation = dtos.SearchSessionInformationOperation;

		this.DeleteSpacesOperation = dtos.DeleteSpacesOperation;
		this.DeleteProjectsOperation = dtos.DeleteProjectsOperation;
		this.DeleteExperimentsOperation = dtos.DeleteExperimentsOperation;
		this.DeleteSamplesOperation = dtos.DeleteSamplesOperation;
		this.DeleteDataSetsOperation = dtos.DeleteDataSetsOperation;
		this.DeleteMaterialsOperation = dtos.DeleteMaterialsOperation;
		this.DeleteExternalDmsOperation = dtos.DeleteExternalDmsOperation;
		this.DeletePluginsOperation = dtos.DeletePluginsOperation;
		this.DeletePropertyTypesOperation = dtos.DeletePropertyTypesOperation;
		this.DeleteVocabulariesOperation = dtos.DeleteVocabulariesOperation;
		this.DeleteVocabularyTermsOperation = dtos.DeleteVocabularyTermsOperation;
		this.DeleteExperimentTypesOperation = dtos.DeleteExperimentTypesOperation;
		this.DeleteSampleTypesOperation = dtos.DeleteSampleTypesOperation;
		this.DeleteDataSetTypesOperation = dtos.DeleteDataSetTypesOperation;
		this.DeleteMaterialTypesOperation = dtos.DeleteMaterialTypesOperation;
		this.DeleteTagsOperation = dtos.DeleteTagsOperation;
		this.DeleteAuthorizationGroupsOperation = dtos.DeleteAuthorizationGroupsOperation;
		this.DeleteRoleAssignmentsOperation = dtos.DeleteRoleAssignmentsOperation;
		this.DeleteOperationExecutionsOperation = dtos.DeleteOperationExecutionsOperation;
		this.DeleteSemanticAnnotationsOperation = dtos.DeleteSemanticAnnotationsOperation;
		this.DeleteQueriesOperation = dtos.DeleteQueriesOperation;
		this.DeletePersonsOperation = dtos.DeletePersonsOperation;
		this.DeletePersonalAccessTokensOperation = dtos.DeletePersonalAccessTokensOperation;

		this.RevertDeletionsOperation = dtos.RevertDeletionsOperation;
		this.ConfirmDeletionsOperation = dtos.ConfirmDeletionsOperation;
		this.ExecuteCustomASServiceOperation = dtos.ExecuteCustomASServiceOperation;
		this.ExecuteAggregationServiceOperation = dtos.ExecuteAggregationServiceOperation;
		this.ExecuteReportingServiceOperation = dtos.ExecuteReportingServiceOperation;
		this.ExecuteProcessingServiceOperation = dtos.ExecuteProcessingServiceOperation;
		this.ExecuteSearchDomainServiceOperation = dtos.ExecuteSearchDomainServiceOperation;
		this.ExecuteQueryOperation = dtos.ExecuteQueryOperation;
		this.ExecuteSqlOperation = dtos.ExecuteSqlOperation;
		this.ArchiveDataSetsOperation = dtos.ArchiveDataSetsOperation;
		this.UnarchiveDataSetsOperation = dtos.UnarchiveDataSetsOperation;
		this.LockDataSetsOperation = dtos.LockDataSetsOperation;
		this.UnlockDataSetsOperation = dtos.UnlockDataSetsOperation;

		this.SynchronousOperationExecutionOptions = dtos.SynchronousOperationExecutionOptions;
		this.AsynchronousOperationExecutionOptions = dtos.AsynchronousOperationExecutionOptions;
		this.OperationExecutionDeletionOptions = dtos.OperationExecutionDeletionOptions;
		this.OperationExecutionFetchOptions = dtos.OperationExecutionFetchOptions;
		this.OperationExecutionPermId = dtos.OperationExecutionPermId;
		this.OperationExecutionSearchCriteria = dtos.OperationExecutionSearchCriteria;
		this.OperationExecutionUpdate = dtos.OperationExecutionUpdate;

		this.AllFields = dtos.AllFields;
		this.Attribute = dtos.Attribute;
		this.ExportableKind = dtos.ExportableKind;
		this.ExportablePermId = dtos.ExportablePermId;
		this.ExportData = dtos.ExportData;
		this.IExportableFields = dtos.IExportableFields;
		this.SelectedFields = dtos.SelectedFields;
		this.ExportFormat = dtos.ExportFormat;
		this.ExportOptions = dtos.ExportOptions;
		this.XlsTextFormat = dtos.XlsTextFormat;
		this.ExportResult = dtos.ExportResult;

		this.IImportData = dtos.IImportData;
		this.ImportFormat = dtos.ImportFormat;
		this.ImportScript = dtos.ImportScript;
		this.UncompressedImportData = dtos.UncompressedImportData;
		this.ZipImportData = dtos.ZipImportData;
		this.ImportMode = dtos.ImportMode;
		this.ImportOptions = dtos.ImportOptions;
		this.ImportOperation = dtos.ImportOperation;
		this.ImportOperationResult = dtos.ImportOperationResult;

		this.getDtos = function() {
			return dtos;
		}

		this.getId = function(entity) {
			if (typeof entity["getPermId"] === 'function') {
				return entity.getPermId();
			}
			if (typeof entity["getId"] === 'function') {
				return entity.getId();
			}
			this.fail("Neither 'getPermId()' nor 'getId()' are functions of entity " + entity);
		}

		this.generateId = function(base) {
			var date = new Date();
			var parts = [ "V3", base, date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), Math.round(1000000 * Math.random()) ];
			return parts.join("_");
		},

		this.createSpace = function(facade) {
			var c = this;
			var creation = new dtos.SpaceCreation();
			creation.setCode(c.generateId("SPACE"));
			return facade.createSpaces([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createProject = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.ProjectCreation();
				creation.setCode(c.generateId("PROJECT"));
				creation.setSpaceId(spacePermId);
				return facade.createProjects([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createExperiment = function(facade) {
			var c = this;
			return c.createProject(facade).then(function(projectPermId) {
				var creation = new dtos.ExperimentCreation();
				creation.setCode(c.generateId("EXPERIMENT"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setProjectId(projectPermId);
				return facade.createExperiments([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createSample = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.SampleCreation();
				creation.setCode(c.generateId("SAMPLE"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setSpaceId(spacePermId);
				return facade.createSamples([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createLinkDataSet = function(facade, path, gitCommitHash, gitRepositoryId) {
			var c = this;
			return c.createExperiment(facade).then(function(experimentPermId) {
				return c.createFileExternalDms(facade).then(function(emdsPermId) {
					var dataSet = new dtos.DataSetCreation();
					dataSet.setAutoGeneratedCode(true);
					dataSet.setTypeId(new c.EntityTypePermId("LINK_TYPE"));
					dataSet.setExperimentId(experimentPermId);
					dataSet.setDataStoreId(new c.DataStorePermId("DSS1"));
					var linkedData = new c.LinkedDataCreation();
					var cc = new c.ContentCopyCreation();
					cc.setExternalDmsId(emdsPermId);
					cc.setPath(path);
					cc.setGitCommitHash(gitCommitHash);
					cc.setGitRepositoryId(gitRepositoryId);
					linkedData.setContentCopies([ cc ]);
					dataSet.setLinkedData(linkedData);
					return facade.createDataSets([ dataSet ]).then(function(permIds) {
						return permIds[0];
					});
				});
			});
		}.bind(this);

		this.createDataSet = function(facade, dataSetType) {
			var c = this;
			return this.getResponseFromJSTestAggregationService(facade, {
				"dataSetType" : dataSetType
			}, function(response) {
				return new dtos.DataSetPermId(response.result.rows[0][0].value);
			});
		}.bind(this);

		this.waitUntilEmailWith = function(facade, textSnippet, timeout) {
			var c = this;
			var dfd = $.Deferred();
			var start = new Date().getTime();
			var waitForEmailWith = function() {
				var parameters = {
					"method" : "getEmailsWith",
					"text-snippet" : textSnippet
				}
				c.getResponseFromJSTestAggregationService(facade, parameters, function(response) {
					if (response.result.rows.length == 0) {
						var now = new Date().getTime();
						if (now - start > timeout) {
							c.fail("No e-mail with text snippet '" + textSnippet + "' after " + timeout + " msec.");
							dfd.reject();
						} else {
							setTimeout(waitForEmailWith, 1000);
						}
					} else {
						dfd.resolve(response.result.rows);
					}
				});
			};
			waitForEmailWith();
			return dfd.promise();
		}.bind(this);

		this.waitUntilIndexed = function(facade, dataSetCode, timeout) {
			var c = this;
			var dfd = $.Deferred();
			var start = new Date().getTime();

			var searchAndWait = function() {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withPermId().thatEquals(dataSetCode);

				facade.searchDataSets(criteria, c.createDataSetFetchOptions()).then(function(result) {
					if (result.getTotalCount() == 0) {
						var now = new Date().getTime();
						if (now - start > timeout) {
							c.fail("Data set " + dataSetCode + " not indexed after " + timeout + " msec.");
							dfd.reject();
						} else {
							setTimeout(searchAndWait, 1000);
						}
					} else {
						dfd.resolve();
					}
				});
			};
			searchAndWait();
			return dfd.promise();
		}.bind(this);

		this.getResponseFromJSTestAggregationService = function(facade, params, callback) {
			var c = this;
			return $.ajax({
				"url" : "http://" + testHost + ":20001/datastore_server/rmi-dss-api-v1.json",
				"type" : "POST",
				"processData" : false,
				"dataType" : "json",
				"data" : JSON.stringify({
					"method" : "createReportFromAggregationService",
					"params" : [ facade._private.sessionToken, "js-test", params ],
					"id" : "1",
					"jsonrpc" : "2.0"
				})
			}).then(callback);
		}.bind(this);

		this.createMaterial = function(facade) {
			var c = this;
			var creation = new dtos.MaterialCreation();
			creation.setCode(c.generateId("MATERIAL"));
			creation.setTypeId(new dtos.EntityTypePermId("COMPOUND"));
			return facade.createMaterials([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createPropertyType = function(facade) {
			var c = this;
			var creation = new dtos.PropertyTypeCreation();
			creation.setCode(c.generateId("PROPERTY_TYPE"));
			creation.setLabel("Testing");
			creation.setDescription("testing");
			creation.setDataType(c.DataType.VARCHAR);
			creation.setMultiValue(false);
			return facade.createPropertyTypes([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createPlugin = function(facade) {
			var c = this;
			var creation = new dtos.PluginCreation();
			creation.setName(c.generateId("PLUGIN"));
			creation.setPluginType(c.PluginType.DYNAMIC_PROPERTY);
			creation.setScript("42");
			creation.setEntityKind(c.EntityKind.SAMPLE);
			return facade.createPlugins([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createVocabulary = function(facade) {
			var c = this;
			var creation = new dtos.VocabularyCreation();
			creation.setCode(c.generateId("VOCABULARY"));
			return facade.createVocabularies([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createVocabularyTerm = function(facade) {
			var c = this;
			var creation = new dtos.VocabularyTermCreation();
			creation.setCode(c.generateId("VOCABULARY_TERM"));
			creation.setVocabularyId(new c.VocabularyPermId("TEST-VOCABULARY"));
			return facade.createVocabularyTerms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createExperimentType = function(facade) {
			var c = this;
			var creation = new dtos.ExperimentTypeCreation();
			creation.setCode(c.generateId("EXPERIMENT_TYPE"));
			return facade.createExperimentTypes([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createSampleType = function(facade) {
			var c = this;
			var creation = new dtos.SampleTypeCreation();
			creation.setCode(c.generateId("SAMPLE_TYPE"));
			return facade.createSampleTypes([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createDataSetType = function(facade) {
			var c = this;
			var creation = new dtos.DataSetTypeCreation();
			creation.setCode(c.generateId("DATA_SET_TYPE"));
			return facade.createDataSetTypes([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createMaterialType = function(facade) {
			var c = this;
			var creation = new dtos.MaterialTypeCreation();
			creation.setCode(c.generateId("MATERIAL_TYPE"));
			return facade.createMaterialTypes([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createExternalDms = function(facade) {
			var c = this;
			var creation = new dtos.ExternalDmsCreation();
			creation.setCode(c.generateId("EMDS"));
			creation.setAddressType(c.ExternalDmsAddressType.URL);
			creation.setAddress("https://my-server:8443/my-app/q=${term}")
			return facade.createExternalDms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createFileExternalDms = function(facade) {
			var c = this;
			var creation = new c.ExternalDmsCreation();
			creation.setCode(c.generateId("EMDS"));
			creation.setLabel("Test File EDMS");
			creation.setAddressType(c.ExternalDmsAddressType.FILE_SYSTEM);
			creation.setAddress("host:my/path")
			return facade.createExternalDms([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createTag = function(facade) {
			var c = this;
			var creation = new dtos.TagCreation();
			creation.setCode(c.generateId("TAG"));
			return facade.createTags([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createAuthorizationGroup = function(facade) {
			var c = this;
			var creation = new dtos.AuthorizationGroupCreation();
			creation.setCode(c.generateId("AUTHORIZATION_GROUP"));
			creation.setUserIds([ new c.PersonPermId("power_user") ]);
			return facade.createAuthorizationGroups([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createRoleAssignment = function(facade, isUser) {
			var c = this;
			return c.createSpace(facade).then(function(spaceId) {
				var creation = new dtos.RoleAssignmentCreation();
				creation.setRole(c.Role.ADMIN);
				if (isUser) {
					creation.setUserId(new c.PersonPermId("power_user"));
				} else {
					creation.setAuthorizationGroupId(new c.AuthorizationGroupPermId("TEST-GROUP"));
				}
				creation.setSpaceId(spaceId);
				return facade.createRoleAssignments([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createPerson = function(facade) {
			var c = this;
			var creation = new dtos.PersonCreation();
			creation.setUserId(c.generateId("USER"));
			return facade.createPersons([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createSemanticAnnotation = function(facade) {
			var c = this;
			var creation = new dtos.SemanticAnnotationCreation();
			creation.setEntityTypeId(new dtos.EntityTypePermId("UNKNOWN", "SAMPLE"));
			creation.setPredicateOntologyId(c.generateId("jsPredicateOntologyId"));
			creation.setPredicateOntologyVersion(c.generateId("jsPredicateOntologyVersion"));
			creation.setPredicateAccessionId(c.generateId("jsPredicateAccessionId"));
			creation.setDescriptorOntologyId(c.generateId("jsDescriptorOntologyId"));
			creation.setDescriptorOntologyVersion(c.generateId("jsDescriptorOntologyVersion"));
			creation.setDescriptorAccessionId(c.generateId("jsDescriptorAccessionId"));
			return facade.createSemanticAnnotations([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createOperationExecution = function(facade) {
			var c = this;
			var operation = new dtos.GetSpacesOperation([ new dtos.SpacePermId("/TEST") ], new dtos.SpaceFetchOptions());
			var options = new dtos.SynchronousOperationExecutionOptions();
			options.setExecutionId(new dtos.OperationExecutionPermId());
			return facade.executeOperations([ operation ], options).then(function() {
				return options.getExecutionId();
			});
		}.bind(this);

		this.createQuery = function(facade) {
			var c = this;
			var creation = new dtos.QueryCreation();
			creation.setName(c.generateId("QUERY"));
			creation.setDatabaseId(new c.QueryDatabaseName("openbisDB"));
			creation.setQueryType(c.QueryType.GENERIC);
			creation.setSql("select code from spaces");
			return facade.createQueries([ creation ]).then(function(techIds) {
				return techIds[0];
			});
		}.bind(this);

		this.createPersonalAccessToken = function(facade) {
			var c = this;
			var now = new Date();
			var creation = new dtos.PersonalAccessTokenCreation();
			creation.setSessionName(c.generateId("PERSONAL_ACCESS_TOKEN"));
			creation.setValidFromDate(now.getTime());
			creation.setValidToDate(new Date(now.getTime() + 24 * 3600 * 1000).getTime());
			return facade.createPersonalAccessTokens([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.findSpace = function(facade, id) {
			var c = this;
			return facade.getSpaces([ id ], c.createSpaceFetchOptions()).then(function(spaces) {
				return spaces[id];
			});
		}.bind(this);

		this.findProject = function(facade, id) {
			var c = this;
			return facade.getProjects([ id ], c.createProjectFetchOptions()).then(function(projects) {
				return projects[id];
			});
		}.bind(this);

		this.findExperiment = function(facade, id) {
			var c = this;
			return facade.getExperiments([ id ], c.createExperimentFetchOptions()).then(function(experiments) {
				return experiments[id];
			});
		}.bind(this);

		this.findExperimentType = function(facade, id) {
			var c = this;
			var criteria = new c.ExperimentTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchExperimentTypes(criteria, c.createExperimentTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findSample = function(facade, id) {
			var c = this;
			return facade.getSamples([ id ], c.createSampleFetchOptions()).then(function(samples) {
				return samples[id];
			});
		}.bind(this);

		this.findSampleType = function(facade, id) {
			var c = this;
			var criteria = new c.SampleTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchSampleTypes(criteria, c.createSampleTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findDataSet = function(facade, id) {
			var c = this;
			return facade.getDataSets([ id ], c.createDataSetFetchOptions()).then(function(dataSets) {
				return dataSets[id];
			});
		}.bind(this);

		this.findDataSetType = function(facade, id) {
			var c = this;
			var criteria = new c.DataSetTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchDataSetTypes(criteria, c.createDataSetTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findMaterial = function(facade, id) {
			var c = this;
			return facade.getMaterials([ id ], c.createMaterialFetchOptions()).then(function(materials) {
				return materials[id];
			});
		}.bind(this);

		this.findMaterialType = function(facade, id) {
			var c = this;
			var criteria = new c.MaterialTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchMaterialTypes(criteria, c.createMaterialTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findPropertyType = function(facade, id) {
			var c = this;
			var criteria = new c.PropertyTypeSearchCriteria();
			criteria.withId().thatEquals(id);
			return facade.searchPropertyTypes(criteria, c.createPropertyTypeFetchOptions()).then(function(results) {
				return results.getObjects()[0];
			});
		}.bind(this);

		this.findPlugin = function(facade, id) {
			var c = this;
			return facade.getPlugins([ id ], c.createPluginFetchOptions()).then(function(plugins) {
				return plugins[id];
			});
		}.bind(this);

		this.findVocabulary = function(facade, id) {
			var c = this;
			return facade.getVocabularies([ id ], c.createVocabularyFetchOptions()).then(function(vocabularies) {
				return vocabularies[id];
			});
		}.bind(this);

		this.findVocabularyTerm = function(facade, id) {
			var c = this;
			return facade.getVocabularyTerms([ id ], c.createVocabularyTermFetchOptions()).then(function(terms) {
				return terms[id];
			});
		}.bind(this);

		this.findTag = function(facade, id) {
			var c = this;
			return facade.getTags([ id ], c.createTagFetchOptions()).then(function(tags) {
				return tags[id];
			});
		}.bind(this);

		this.findAuthorizationGroup = function(facade, id) {
			var c = this;
			return facade.getAuthorizationGroups([ id ], c.createAuthorizationGroupFetchOptions()).then(function(groups) {
				return groups[id];
			});
		}.bind(this);

		this.findRoleAssignment = function(facade, id) {
			var c = this;
			return facade.getRoleAssignments([ id ], c.createRoleAssignmentFetchOptions()).then(function(assignments) {
				return assignments[id];
			});
		}.bind(this);

		this.findPerson = function(facade, id) {
			var c = this;
			return facade.getPersons([ id ], c.createPersonFetchOptions()).then(function(persons) {
				return persons[id];
			});
		}.bind(this);

		this.findSemanticAnnotation = function(facade, id) {
			var c = this;
			return facade.getSemanticAnnotations([ id ], c.createSemanticAnnotationFetchOptions()).then(function(annotations) {
				return annotations[id];
			});
		}.bind(this);

		this.findExternalDms = function(facade, id) {
			var c = this;
			return facade.getExternalDataManagementSystems([ id ], c.createExternalDmsFetchOptions()).then(function(edms) {
				return edms[id];
			});
		}.bind(this);

		this.findOperationExecution = function(facade, id) {
			var c = this;
			return facade.getOperationExecutions([ id ], c.createOperationExecutionFetchOptions()).then(function(executions) {
				return executions[id];
			});
		}.bind(this);

		this.findQuery = function(facade, id) {
			var c = this;
			return facade.getQueries([ id ], c.createQueryFetchOptions()).then(function(queries) {
				return queries[id];
			});
		}.bind(this);

		this.findPersonalAccessToken = function(facade, id) {
			var c = this;
			return facade.getPersonalAccessTokens([ id ], c.createPersonalAccessTokenFetchOptions()).then(function(pats) {
				return pats[id];
			});
		}.bind(this);

		this.deleteSpace = function(facade, id) {
			var c = this;
			var options = new dtos.SpaceDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSpaces([ id ], options);
		}.bind(this);

		this.deleteProject = function(facade, id) {
			var c = this;
			var options = new dtos.ProjectDeletionOptions();
			options.setReason("test reason");
			return facade.deleteProjects([ id ], options);
		}.bind(this);

		this.deleteExperiment = function(facade, id) {
			var c = this;
			var options = new dtos.ExperimentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperiments([ id ], options);
		}.bind(this);

		this.deleteSample = function(facade, id) {
			var c = this;
			var options = new dtos.SampleDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSamples([ id ], options);
		}.bind(this);

		this.deleteDataSet = function(facade, id) {
			var c = this;
			var options = new dtos.DataSetDeletionOptions();
			options.setReason("test reason");
			return facade.deleteDataSets([ id ], options);
		}.bind(this);

		this.deleteMaterial = function(facade, id) {
			var c = this;
			var options = new dtos.MaterialDeletionOptions();
			options.setReason("test reason");
			return facade.deleteMaterials([ id ], options);
		}.bind(this);

		this.deleteExternalDms = function(facade, id) {
			var c = this;
			var options = new dtos.ExternalDmsDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExternalDataManagementSystems([ id ], options);
		}.bind(this);

		this.deleteExperimentType = function(facade, id) {
			var c = this;
			var options = new dtos.ExperimentTypeDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperimentTypes([ id ], options);
		}.bind(this);

		this.deleteSampleType = function(facade, id) {
			var c = this;
			var options = new dtos.SampleTypeDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSampleTypes([ id ], options);
		}.bind(this);

		this.deleteDataSetType = function(facade, id) {
			var c = this;
			var options = new dtos.DataSetTypeDeletionOptions();
			options.setReason("test reason");
			return facade.deleteDataSetTypes([ id ], options);
		}.bind(this);

		this.deleteMaterialType = function(facade, id) {
			var c = this;
			var options = new dtos.MaterialTypeDeletionOptions();
			options.setReason("test reason");
			return facade.deleteMaterialTypes([ id ], options);
		}.bind(this);

		this.deletePlugin = function(facade, id) {
			var c = this;
			var options = new dtos.PluginDeletionOptions();
			options.setReason("test reason");
			return facade.deletePlugins([ id ], options);
		}.bind(this);

		this.deletePropertyType = function(facade, id) {
			var c = this;
			var options = new dtos.PropertyTypeDeletionOptions();
			options.setReason("test reason");
			return facade.deletePropertyTypes([ id ], options);
		}.bind(this);

		this.deleteVocabulary = function(facade, id) {
			var c = this;
			var options = new dtos.VocabularyDeletionOptions();
			options.setReason("test reason");
			return facade.deleteVocabularies([ id ], options);
		}.bind(this);

		this.deleteVocabularyTerm = function(facade, id) {
			var c = this;
			var options = new dtos.VocabularyTermDeletionOptions();
			options.setReason("test reason");
			return facade.deleteVocabularyTerms([ id ], options);
		}.bind(this);

		this.replaceVocabularyTerm = function(facade, id) {
			var c = this;
			var options = new dtos.VocabularyTermDeletionOptions();
			options.setReason("test reason");
			options.replace(id, new c.VocabularyTermPermId("TEST-TERM-1", "TEST-VOCABULARY"));
			return facade.deleteVocabularyTerms([ id ], options);
		}.bind(this);

		this.deleteTag = function(facade, id) {
			var c = this;
			var options = new dtos.TagDeletionOptions();
			options.setReason("test reason");
			return facade.deleteTags([ id ], options);
		}.bind(this);

		this.deleteAuthorizationGroup = function(facade, id) {
			var c = this;
			var options = new dtos.AuthorizationGroupDeletionOptions();
			options.setReason("test reason");
			return facade.deleteAuthorizationGroups([ id ], options);
		}.bind(this);

		this.deleteRoleAssignment = function(facade, id) {
			var c = this;
			var options = new dtos.RoleAssignmentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteRoleAssignments([ id ], options);
		}.bind(this);

		this.deleteOperationExecution = function(facade, id) {
			var c = this;
			var options = new dtos.OperationExecutionDeletionOptions();
			options.setReason("test reason");
			return facade.deleteOperationExecutions([ id ], options);
		}.bind(this);

		this.deleteSemanticAnnotation = function(facade, id) {
			var c = this;
			var options = new dtos.SemanticAnnotationDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSemanticAnnotations([ id ], options);
		}.bind(this);

		this.deleteQuery = function(facade, id) {
			var c = this;
			var options = new dtos.QueryDeletionOptions();
			options.setReason("test reason");
			return facade.deleteQueries([ id ], options);
		}.bind(this);

		this.deletePersonalAccessToken = function(facade, id) {
			var c = this;
			var options = new dtos.PersonalAccessTokenDeletionOptions();
			options.setReason("test reason");
			return facade.deletePersonalAccessTokens([ id ], options);
		}.bind(this);

		this.deletePerson = function(facade, id) {
			var c = this;
			var options = new dtos.PersonDeletionOptions();
			options.setReason("test reason");
			return facade.deletePersons([ id ], options);
		}.bind(this);

		this.getObjectProperty = function(object, propertyName) {
			var propertyNames = propertyName.split('.');
			for ( var pn in propertyNames) {
				object = object[propertyNames[pn]];
			}
			return object;
		};

		this.createFacade = function() {
			var dfd = $.Deferred();
			dfd.resolve(new openbis(testApiUrl));
			return dfd.promise();
		};

		this.createFacadeAndLogin = function() {
			var dfd = $.Deferred();

			this.createFacade().then(function(facade) {
				facade.login(testUserId, testUserPassword).done(function() {
					dfd.resolve(facade);
				}).fail(function() {
					dfd.reject(arguments);
				});
			});

			return dfd.promise();
		};

		this.createSpaceFetchOptions = function() {
			var fo = new dtos.SpaceFetchOptions();
			fo.withProjects();
			fo.withSamples();
			fo.withRegistrator();
			return fo;
		};

		this.createProjectFetchOptions = function() {
			var fo = new dtos.ProjectFetchOptions();
			fo.withSpace();
			fo.withExperiments();
			fo.withRegistrator();
			fo.withModifier();
			fo.withLeader();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createExperimentFetchOptions = function() {
			var fo = new dtos.ExperimentFetchOptions();
			fo.withType();
			fo.withProject().withSpace();
			fo.withDataSets();
			fo.withSamples();
			fo.withHistory();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withSampleProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createExperimentTypeFetchOptions = function() {
			var fo = new dtos.ExperimentTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			fo.withPropertyAssignments().withPlugin();
			return fo;
		};

		this.createSampleFetchOptions = function() {
			var fo = new dtos.SampleFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProject();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withSampleProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainer();
			fo.withComponents();
			fo.withDataSets();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			fo.withChildrenUsing(fo);
			return fo;
		};

		this.createSampleTypeFetchOptions = function() {
			var fo = new dtos.SampleTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			fo.withPropertyAssignments().withPlugin();
			return fo;
		};

		this.createDataSetFetchOptions = function() {
			var fo = new dtos.DataSetFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSample();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withSampleProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainers();
			fo.withComponents();
			fo.withPhysicalData().withFileFormatType();
			fo.withPhysicalData().withLocatorType();
			fo.withPhysicalData().withStorageFormat();
			fo.withLinkedData().withExternalDms();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			return fo;
		};

		this.createDataSetTypeFetchOptions = function() {
			var fo = new dtos.DataSetTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			fo.withPropertyAssignments().withPlugin();
			return fo;
		};

		this.createMaterialFetchOptions = function() {
			var fo = new dtos.MaterialFetchOptions();
			fo.withType();
			fo.withHistory();
			fo.withRegistrator();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			return fo;
		};

		this.createMaterialTypeFetchOptions = function() {
			var fo = new dtos.MaterialTypeFetchOptions();
			fo.withPropertyAssignments().withPropertyType();
			fo.withPropertyAssignments().withRegistrator();
			fo.withPropertyAssignments().withPlugin();
			return fo;
		};

		this.createPluginFetchOptions = function() {
			var fo = new dtos.PluginFetchOptions();
			fo.withScript();
			fo.withRegistrator();
			return fo;
		};

		this.createVocabularyFetchOptions = function() {
			var fo = new dtos.VocabularyFetchOptions();
			fo.withTerms();
			fo.withRegistrator();
			return fo;
		};

		this.createVocabularyTermFetchOptions = function() {
			var fo = new dtos.VocabularyTermFetchOptions();
			fo.withVocabulary();
			fo.withRegistrator();
			return fo;
		};

		this.createGlobalSearchObjectFetchOptions = function() {
			var fo = new dtos.GlobalSearchObjectFetchOptions();
			fo.withExperiment();
			fo.withSample();
			fo.withDataSet();
			fo.withMaterial();
			return fo;
		};

		this.createObjectKindModificationFetchOptions = function() {
			var fo = new dtos.ObjectKindModificationFetchOptions();
			return fo;
		};

		this.createTagFetchOptions = function() {
			var fo = new dtos.TagFetchOptions();
			fo.withExperiments();
			fo.withSamples();
			fo.withDataSets();
			fo.withMaterials();
			return fo;
		};

		this.createAuthorizationGroupFetchOptions = function() {
			var fo = new dtos.AuthorizationGroupFetchOptions();
			fo.withRegistrator();
			fo.withUsers();
			var rafo = fo.withRoleAssignments();
			rafo.withSpace();
			rafo.withProject().withSpace();
			return fo;
		};

		this.createRoleAssignmentFetchOptions = function() {
			var fo = new dtos.RoleAssignmentFetchOptions();
			fo.withProject();
			fo.withSpace();
			fo.withUser();
			fo.withAuthorizationGroup();
			fo.withRegistrator();
			return fo;
		};

		this.createPersonFetchOptions = function() {
			var fo = new dtos.PersonFetchOptions();
			fo.withSpace();
			fo.withRoleAssignments().withSpace();
			fo.withRegistrator();
			fo.withAllWebAppSettings();
			return fo;
		};

		this.createPropertyTypeFetchOptions = function() {
			var fo = new dtos.PropertyTypeFetchOptions();
			fo.withVocabulary();
			fo.withMaterialType();
			fo.withSampleType();
			fo.withSemanticAnnotations();
			fo.withRegistrator();
			return fo;
		};

		this.createPropertyAssignmentFetchOptions = function() {
			var fo = new dtos.PropertyAssignmentFetchOptions();
			fo.withEntityType();
			fo.withPropertyType();
			fo.withSemanticAnnotations();
			fo.withRegistrator();
			return fo;
		};

		this.createSemanticAnnotationFetchOptions = function() {
			var fo = new dtos.SemanticAnnotationFetchOptions();
			fo.withEntityType();
			fo.withPropertyType();
			fo.withPropertyAssignment().withEntityType();
			fo.withPropertyAssignment().withPropertyType();
			fo.withPropertyAssignment().withPlugin();
			return fo;
		};

		this.createExternalDmsFetchOptions = function() {
			var fo = new dtos.ExternalDmsFetchOptions();
			return fo;
		};

		this.createOperationExecutionFetchOptions = function() {
			var fo = new dtos.OperationExecutionFetchOptions();
			fo.withOwner().withSpace();
			fo.withOwner().withRegistrator();
			fo.withNotification();
			fo.withSummary().withOperations();
			fo.withSummary().withProgress();
			fo.withSummary().withError();
			fo.withSummary().withResults();
			fo.withDetails().withOperations();
			fo.withDetails().withProgress();
			fo.withDetails().withError();
			fo.withDetails().withResults();
			return fo;
		};

		this.createDataStoreFetchOptions = function() {
			var fo = new dtos.DataStoreFetchOptions();
			return fo;
		};

		this.createDataSetFileFetchOptions = function() {
			var fo = new dtos.DataSetFileFetchOptions();
			return fo;
		};

		this.createQueryFetchOptions = function() {
			var fo = new dtos.QueryFetchOptions();
			fo.withRegistrator();
			return fo;
		};

		this.createPersonalAccessTokenFetchOptions = function() {
			var fo = new dtos.PersonalAccessTokenFetchOptions();
			fo.withOwner();
			fo.withRegistrator();
			fo.withModifier();
			return fo;
		};

		this.extractIdentifiers = function(entities) {
			identifiers = []
			entities.forEach(function(entity) {
				identifiers.push(entity.getIdentifier());
			});
			identifiers.sort();
			return identifiers;
		}

		this.extractCodes = function(entities) {
			var codes = [];
			entities.forEach(function(entity) {
				codes.push(entity.getCode());
			});
			codes.sort();
			return codes;
		}

		this.assertNull = function(actual, msg) {
			this.assertEqual(actual, null, msg)
		};

		this.assertNotNull = function(actual, msg) {
			this.assertNotEqual(actual, null, msg);
		};

		this.assertTrue = function(actual, msg) {
			this.assertEqual(actual, true, msg);
		};

		this.assertFalse = function(actual, msg) {
			this.assertEqual(actual, false, msg);
		};

		this.assertContains = function(actual, expected, msg) {
			actual = actual ? actual : "";
			this.assertTrue(actual.indexOf(expected) >= 0, msg);
		};

		this.assertEqual = function(actual, expected, msg) {
			this.assert.equal(actual, expected, msg);
		};

		this.assertNotEqual = function(actual, expected, msg) {
			this.assert.notEqual(actual, expected, msg);
		};

		this.assertDate = function(millis, msg, year, month, day, hour, minute) {
			var date = new Date(millis);
			var actual = "";
			var expected = "";

			if (year) {
				actual += date.getUTCFullYear();
				expected += year;
			}
			if (month) {
				actual += "-" + (date.getUTCMonth() + 1);
				expected += "-" + month;
			}
			if (day) {
				actual += "-" + date.getUTCDate();
				expected += "-" + day;
			}
			if (hour) {
				actual += " " + date.getUTCHours();
				expected += " " + hour;
			}
			if (minute) {
				actual += ":" + date.getUTCMinutes();
				expected += ":" + minute;
			}

			this.assertEqual(actual, expected, msg);
		};

		this.assertToday = function(millis, msg) {
			var today = new Date();
			this.assertDate(millis, msg, today.getUTCFullYear(), today.getUTCMonth() + 1, today.getUTCDate());
		};

		this.assertEqualDictionary = function(actual, expected, msg) {
			this.assert.equal(this.renderDictionary(actual), this.renderDictionary(expected), msg);
		};

		this.renderDictionary = function(dictionary) {
			var keys = Object.keys(dictionary);
			keys.sort();
			var result = "[";
			for (i = 0; i < keys.length; i++) {
				var key = keys[i];
				result += key + ":" + dictionary[key];
				if (i < keys.length - 1) {
					result += ", ";
				}
			}
			return result + "]";
		}

		this.assertObjectsCount = function(objects, count) {
			this.assertEqual(objects.length, count, 'Got ' + count + ' object(s)');
		};

		this.assertObjectsWithValues = function(objects, propertyName, propertyValues) {
			var thisCommon = this;
			var values = {};

			$.each(objects, function(index, object) {
				var value = thisCommon.getObjectProperty(object, propertyName);
				if (value in values == false) {
					values[value] = true;
				}
			});

            var expected = propertyValues.sort();
            var actual = Object.keys(values).sort();
			this.assert.deepEqual(actual, expected, 'Objects have correct ' + propertyName + ' values. Expected: [' + expected + '] - Actual: [' + actual + ']');
		};

		this.assertObjectsWithOrWithoutCollections = function(objects, accessor, checker) {
			var theObjects = null;

			if ($.isArray(objects)) {
				theObjects = objects;
			} else {
				theObjects = [ objects ];
			}

			var theAccessor = null;

			if ($.isFunction(accessor)) {
				theAccessor = accessor;
			} else {
				theAccessor = function(object) {
					return object[accessor];
				}
			}

			checker(theObjects, theAccessor);
		};

		this.assertObjectsWithCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.some(function(object) {
					var value = accessor(object);
					return value && Object.keys(value).length > 0;
				}), 'Objects have non-empty collections accessed via: ' + accessor);
			});
		};

		this.assertObjectsWithoutCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.every(function(object) {
					var value = accessor(object);
					return !value || Object.keys(value).length == 0;
				}), 'Objects have empty collections accessed via: ' + accessor);
			});
		};

		this.shallowEqual = function(actual, expected, message) {
			function oneWay(c, from, to) {
				var isBad = _.chain(_.keys(from)).filter(function(k) {
					return !_.isFunction(from[k]) && !_.isArray(from[k]) && !_.isObject(from[k]) && !_.isFunction(to[k]) && !_.isArray(to[k]) && !_.isObject(to[k]);
				}).any(function(k) {
					if (from[k] !== to[k]) {
						c.ok("--- k:" + k + " from:" + from[k] + " to:" + to[k]);
						return true;
					}
				}).value();

				if (isBad) {
					assert.propEqual(actual, expected);
				}
			}

			oneWay(this, actual, expected);
			oneWay(this, expected, actual);
		};

		this.start = function() {
			this.done = this.assert.async();
		};

		this.finish = function() {
			if (this.done) {
				this.done();
			}
		};

		this.ok = function(msg) {
			this.assert.ok(true, msg);
		};

		this.section = function(msg) {
			this.assert.ok(true, "******************************************************");
			this.assert.ok(true, msg);
			this.assert.ok(true, "******************************************************");
		};

		this.fail = function(msg) {
			this.assert.ok(false, msg);
		};

	};

	return Common;
})
