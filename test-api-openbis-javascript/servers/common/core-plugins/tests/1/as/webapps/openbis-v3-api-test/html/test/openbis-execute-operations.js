define([], function() {

	var executeOperationsFacade = function(facade, dtos) {

		this._private = {};

		this._executeOperation = function(operation) {
			return facade.executeOperations([ operation ], new dtos.SynchronousOperationExecutionOptions());
		}

		this._executeCreateOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectIds();
			});
		}

		this._executeUpdateOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectIds();
			});
		}

		this._executeGetOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getObjectMap();
			});
		}

		this._executeSearchOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				return results.getResults()[0].getSearchResult();
			});
		}

		this._executeDeleteOperation = function(operation) {
			return this._executeOperation(operation).then(function(results) {
				var result = results.getResults()[0];
				if (result.getDeletionId) {
					return result.getDeletionId();
				}
			});
		}

		this.login = function(user, password) {
			var thisFacade = this;
			return facade.login(user, password).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			return facade.loginAs(user, password, asUserId).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAsAnonymousUser = function() {
			var thisFacade = this;
			return facade.loginAsAnonymousUser().done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.logout = function() {
			var thisFacade = this;
			return facade.logout().done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.getSessionInformation = function() {
			return this._executeOperation(new dtos.GetSessionInformationOperation()).then(function(results) {
				return results.getResults()[0].getSessionInformation();
			});
		}

		this.getServerInformation = function() {
			return this._executeOperation(new dtos.GetServerInformationOperation()).then(function(results) {
				return results.getResults()[0].getServerInformation();
			});
		}

		this.getServerPublicInformation = function() {
			return this._executeOperation(new dtos.GetServerPublicInformationOperation()).then(function(results) {
				return results.getResults()[0].getServerInformation();
			});
		}

		this.createPermIdStrings = function(count) {
			return this._executeOperation(new dtos.CreatePermIdsOperation(count)).then(function(results) {
				return results.getResults()[0].getPermIds();
			});
		}

		this.createCodes = function(prefix, entityKind, count) {
			return this._executeOperation(new dtos.CreateCodesOperation(prefix, entityKind, count)).then(function(results) {
				return results.getResults()[0].getCodes();
			});
		}

		this.createSpaces = function(creations) {
			return this._executeCreateOperation(new dtos.CreateSpacesOperation(creations));
		}

		this.createProjects = function(creations) {
			return this._executeCreateOperation(new dtos.CreateProjectsOperation(creations));
		}

		this.createExperiments = function(creations) {
			return this._executeCreateOperation(new dtos.CreateExperimentsOperation(creations));
		}

		this.createExperimentTypes = function(creations) {
			return this._executeCreateOperation(new dtos.CreateExperimentTypesOperation(creations));
		}

		this.createExternalDms = function(creations) {
			return this._executeCreateOperation(new dtos.CreateExternalDmsOperation(creations));
		}

		this.createSamples = function(creations) {
			return this._executeCreateOperation(new dtos.CreateSamplesOperation(creations));
		}

		this.createSampleTypes = function(creations) {
			return this._executeCreateOperation(new dtos.CreateSampleTypesOperation(creations));
		}

		this.createDataSetTypes = function(creations) {
			return this._executeCreateOperation(new dtos.CreateDataSetTypesOperation(creations));
		}

		this.createDataSets = function(creations) {
			return this._executeCreateOperation(new dtos.CreateDataSetsOperation(creations));
		}

		this.createMaterials = function(creations) {
			return this._executeCreateOperation(new dtos.CreateMaterialsOperation(creations));
		}

		this.createMaterialTypes = function(creations) {
			return this._executeCreateOperation(new dtos.CreateMaterialTypesOperation(creations));
		}

		this.createPropertyTypes = function(creations) {
			return this._executeCreateOperation(new dtos.CreatePropertyTypesOperation(creations));
		}

		this.createPlugins = function(creations) {
			return this._executeCreateOperation(new dtos.CreatePluginsOperation(creations));
		}

		this.createVocabularyTerms = function(creations) {
			return this._executeCreateOperation(new dtos.CreateVocabularyTermsOperation(creations));
		}

		this.createVocabularies = function(creations) {
			return this._executeCreateOperation(new dtos.CreateVocabulariesOperation(creations));
		}

		this.createTags = function(creations) {
			return this._executeCreateOperation(new dtos.CreateTagsOperation(creations));
		}

		this.createAuthorizationGroups = function(creations) {
			return this._executeCreateOperation(new dtos.CreateAuthorizationGroupsOperation(creations));
		}

		this.createRoleAssignments = function(creations) {
			return this._executeCreateOperation(new dtos.CreateRoleAssignmentsOperation(creations));
		}

		this.createPersons = function(creations) {
			return this._executeCreateOperation(new dtos.CreatePersonsOperation(creations));
		}

		this.createSemanticAnnotations = function(creations) {
			return this._executeCreateOperation(new dtos.CreateSemanticAnnotationsOperation(creations));
		}

		this.createQueries = function(creations) {
			return this._executeCreateOperation(new dtos.CreateQueriesOperation(creations));
		}

		this.createPersonalAccessTokens = function(creations) {
			return this._executeCreateOperation(new dtos.CreatePersonalAccessTokensOperation(creations));
		}

		this.updateSpaces = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateSpacesOperation(updates));
		}

		this.updateProjects = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateProjectsOperation(updates));
		}

		this.updateExperiments = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateExperimentsOperation(updates));
		}

		this.updateExperimentTypes = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateExperimentTypesOperation(updates));
		}

		this.updateSamples = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateSamplesOperation(updates));
		}

		this.updateSampleTypes = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateSampleTypesOperation(updates));
		}

		this.updateDataSets = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateDataSetsOperation(updates));
		}

		this.updateDataSetTypes = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateDataSetTypesOperation(updates));
		}

		this.updateMaterials = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateMaterialsOperation(updates));
		}

		this.updateMaterialTypes = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateMaterialTypesOperation(updates));
		}

		this.updateVocabularies = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateVocabulariesOperation(updates));
		}

		this.updatePropertyTypes = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdatePropertyTypesOperation(updates));
		}

		this.updatePlugins = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdatePluginsOperation(updates));
		}

		this.updateVocabularyTerms = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateVocabularyTermsOperation(updates));
		}

		this.updateExternalDataManagementSystems = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateExternalDmsOperation(updates));
		}

		this.updateTags = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateTagsOperation(updates));
		}

		this.updateAuthorizationGroups = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateAuthorizationGroupsOperation(updates));
		}

		this.updatePersons = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdatePersonsOperation(updates));
		}

		this.updateOperationExecutions = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateOperationExecutionsOperation(updates));
		}

		this.updateSemanticAnnotations = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateSemanticAnnotationsOperation(updates));
		}

		this.updateQueries = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdateQueriesOperation(updates));
		}

		this.updatePersonalAccessTokens = function(updates) {
			return this._executeUpdateOperation(new dtos.UpdatePersonalAccessTokensOperation(updates));
		}

		this.getSpaces = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetSpacesOperation(ids, fetchOptions));
		}

		this.getProjects = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetProjectsOperation(ids, fetchOptions));
		}

		this.getExperiments = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetExperimentsOperation(ids, fetchOptions));
		}

		this.getExperimentTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetExperimentTypesOperation(ids, fetchOptions));
		}

		this.getSamples = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetSamplesOperation(ids, fetchOptions));
		}

		this.getSampleTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetSampleTypesOperation(ids, fetchOptions));
		}

		this.getDataSets = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetDataSetsOperation(ids, fetchOptions));
		}

		this.getDataSetTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetDataSetTypesOperation(ids, fetchOptions));
		}

		this.getMaterials = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetMaterialsOperation(ids, fetchOptions));
		}

		this.getMaterialTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetMaterialTypesOperation(ids, fetchOptions));
		}

		this.getPropertyTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetPropertyTypesOperation(ids, fetchOptions));
		}

		this.getPlugins = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetPluginsOperation(ids, fetchOptions));
		}

		this.getVocabularies = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetVocabulariesOperation(ids, fetchOptions));
		}

		this.getVocabularyTerms = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetVocabularyTermsOperation(ids, fetchOptions));
		}

		this.getTags = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetTagsOperation(ids, fetchOptions));
		}

		this.getAuthorizationGroups = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetAuthorizationGroupsOperation(ids, fetchOptions));
		}

		this.getRoleAssignments = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetRoleAssignmentsOperation(ids, fetchOptions));
		}

		this.getRights = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetRightsOperation(ids, fetchOptions));
		}

		this.getPersons = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetPersonsOperation(ids, fetchOptions));
		}

		this.getSemanticAnnotations = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetSemanticAnnotationsOperation(ids, fetchOptions));
		}

		this.getExternalDataManagementSystems = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetExternalDmsOperation(ids, fetchOptions));
		}

		this.getOperationExecutions = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetOperationExecutionsOperation(ids, fetchOptions));
		}

		this.getQueries = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetQueriesOperation(ids, fetchOptions));
		}

		this.getQueryDatabases = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetQueryDatabasesOperation(ids, fetchOptions));
		}

		this.getPersonalAccessTokens = function(ids, fetchOptions) {
			return this._executeGetOperation(new dtos.GetPersonalAccessTokensOperation(ids, fetchOptions));
		}

		this.searchSpaces = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSpacesOperation(criteria, fetchOptions));
		}

		this.searchProjects = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchProjectsOperation(criteria, fetchOptions));
		}

		this.searchExperiments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchExperimentsOperation(criteria, fetchOptions));
		}

		this.searchExperimentTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchExperimentTypesOperation(criteria, fetchOptions));
		}

		this.searchSamples = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSamplesOperation(criteria, fetchOptions));
		}

		this.searchSampleTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSampleTypesOperation(criteria, fetchOptions));
		}

		this.searchDataSets = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchDataSetsOperation(criteria, fetchOptions));
		}

		this.searchDataSetTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchDataSetTypesOperation(criteria, fetchOptions));
		}

		this.searchMaterials = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchMaterialsOperation(criteria, fetchOptions));
		}

		this.searchMaterialTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchMaterialTypesOperation(criteria, fetchOptions));
		}

		this.searchPlugins = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchPluginsOperation(criteria, fetchOptions));
		}

		this.searchVocabularies = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchVocabulariesOperation(criteria, fetchOptions));
		}

		this.searchVocabularyTerms = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchVocabularyTermsOperation(criteria, fetchOptions));
		}

		this.searchExternalDataManagementSystems = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchExternalDmsOperation(criteria, fetchOptions));
		}

		this.searchTags = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchTagsOperation(criteria, fetchOptions));
		}

		this.searchAuthorizationGroups = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchAuthorizationGroupsOperation(criteria, fetchOptions));
		}

		this.searchRoleAssignments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchRoleAssignmentsOperation(criteria, fetchOptions));
		}

		this.searchPersons = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchPersonsOperation(criteria, fetchOptions));
		}

		this.searchCustomASServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchCustomASServicesOperation(criteria, fetchOptions));
		}

		this.searchSearchDomainServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSearchDomainServicesOperation(criteria, fetchOptions));
		}

		this.searchAggregationServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchAggregationServicesOperation(criteria, fetchOptions));
		}

		this.searchReportingServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchReportingServicesOperation(criteria, fetchOptions));
		}

		this.searchProcessingServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchProcessingServicesOperation(criteria, fetchOptions));
		}

		this.searchObjectKindModifications = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchObjectKindModificationsOperation(criteria, fetchOptions));
		}

		this.searchGlobally = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchGloballyOperation(criteria, fetchOptions));
		}

		this.searchOperationExecutions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchOperationExecutionsOperation(criteria, fetchOptions));
		}

		this.searchDataStores = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchDataStoresOperation(criteria, fetchOptions));
		}

		this.searchPropertyTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchPropertyTypesOperation(criteria, fetchOptions));
		}

		this.searchPropertyAssignments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchPropertyAssignmentsOperation(criteria, fetchOptions));
		}

		this.searchSemanticAnnotations = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSemanticAnnotationsOperation(criteria, fetchOptions));
		}

		this.searchQueries = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchQueriesOperation(criteria, fetchOptions));
		}

		this.searchQueryDatabases = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchQueryDatabasesOperation(criteria, fetchOptions));
		}

		this.searchPersonalAccessTokens = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchPersonalAccessTokensOperation(criteria, fetchOptions));
		}

		this.searchSessionInformation = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchSessionInformationOperation(criteria, fetchOptions));
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteSpacesOperation(ids, deletionOptions));
		}

		this.deleteProjects = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteProjectsOperation(ids, deletionOptions));
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteExperimentsOperation(ids, deletionOptions));
		}

		this.deleteSamples = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteSamplesOperation(ids, deletionOptions));
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteDataSetsOperation(ids, deletionOptions));
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteMaterialsOperation(ids, deletionOptions));
		}

		this.deleteExternalDataManagementSystems = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteExternalDmsOperation(ids, deletionOptions));
		}

		this.deletePlugins = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeletePluginsOperation(ids, deletionOptions));
		}

		this.deletePropertyTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeletePropertyTypesOperation(ids, deletionOptions));
		}

		this.deleteVocabularies = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteVocabulariesOperation(ids, deletionOptions));
		}

		this.deleteVocabularyTerms = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteVocabularyTermsOperation(ids, deletionOptions));
		}

		this.deleteExperimentTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteExperimentTypesOperation(ids, deletionOptions));
		}

		this.deleteSampleTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteSampleTypesOperation(ids, deletionOptions));
		}

		this.deleteDataSetTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteDataSetTypesOperation(ids, deletionOptions));
		}

		this.deleteMaterialTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteMaterialTypesOperation(ids, deletionOptions));
		}

		this.deleteTags = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteTagsOperation(ids, deletionOptions));
		}

		this.deleteAuthorizationGroups = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteAuthorizationGroupsOperation(ids, deletionOptions));
		}

		this.deleteRoleAssignments = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteRoleAssignmentsOperation(ids, deletionOptions));
		}

		this.deleteOperationExecutions = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteOperationExecutionsOperation(ids, deletionOptions));
		}

		this.deleteSemanticAnnotations = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteSemanticAnnotationsOperation(ids, deletionOptions));
		}

		this.deleteQueries = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeleteQueriesOperation(ids, deletionOptions));
		}

		this.deletePersonalAccessTokens = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeletePersonalAccessTokensOperation(ids, deletionOptions));
		}

		this.deletePersons = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new dtos.DeletePersonsOperation(ids, deletionOptions));
		}

		this.searchDeletions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchDeletionsOperation(criteria, fetchOptions));
		}

		this.searchEvents = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new dtos.SearchEventsOperation(criteria, fetchOptions));
		}

		this.revertDeletions = function(ids) {
			return this._executeOperation(new dtos.RevertDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.confirmDeletions = function(ids) {
			return this._executeOperation(new dtos.ConfirmDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeCustomASService = function(serviceId, options) {
			return this._executeOperation(new dtos.ExecuteCustomASServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeAggregationService = function(serviceId, options) {
			return this._executeOperation(new dtos.ExecuteAggregationServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeReportingService = function(serviceId, options) {
			return this._executeOperation(new dtos.ExecuteReportingServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeProcessingService = function(serviceId, options) {
			return this._executeOperation(new dtos.ExecuteProcessingServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeSearchDomainService = function(options) {
			return this._executeOperation(new dtos.ExecuteSearchDomainServiceOperation(options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeQuery = function(queryId, options) {
			return this._executeOperation(new dtos.ExecuteQueryOperation(queryId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeSql = function(sql, options) {
			return this._executeOperation(new dtos.ExecuteSqlOperation(sql, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.evaluatePlugin = function(options) {
			return this._executeOperation(new dtos.EvaluatePluginOperation(options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.archiveDataSets = function(ids, options) {
			return this._executeOperation(new dtos.ArchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.unarchiveDataSets = function(ids, options) {
			return this._executeOperation(new dtos.UnarchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.lockDataSets = function(ids, options) {
			return this._executeOperation(new dtos.LockDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.unlockDataSets = function(ids, options) {
			return this._executeOperation(new dtos.UnlockDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeOperations = function(operations, options) {
			return facade.executeOperations(operations, options);
		}

		this.getDataStoreFacade = function() {
			return facade.getDataStoreFacade.apply(facade, arguments);
		}

		this.executeImport = function(importData, importOptions) {
			return this._executeOperation(new dtos.ImportOperation(importData, importOptions));
		}

		this.executeExport = function(exportData, exportOptions) {
			return this._executeOperation(new dtos.ExportOperation(exportData, exportOptions));
		}

	}

	return executeOperationsFacade;

});