define([ 'jquery', 'openbis', 'test/common' ], function($, openbis, common) {

	var c = new common();

	var facade = function() {

		this._private = {};
		this._openbis = new openbis();

		this._executeOperation = function(operation) {
			return this._openbis.executeOperations([ operation ], new c.SynchronousOperationExecutionOptions());
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
			return this._openbis.login(user, password).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			return this._openbis.loginAs(user, password, asUserId).done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.loginAsAnonymousUser = function() {
			var thisFacade = this;
			return this._openbis.loginAsAnonymousUser().done(function(sessionToken) {
				thisFacade._private.sessionToken = sessionToken;
			});
		}

		this.logout = function() {
			var thisFacade = this;
			return this._openbis.logout().done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.getSessionInformation = function() {
			return this._executeOperation(new c.GetSessionInformationOperation()).then(function(results) {
				return results.getResults()[0].getSessionInformation();
			});
		}

		this.getServerInformation = function() {
			return this._executeOperation(new c.GetServerInformationOperation()).then(function(results) {
				return results.getResults()[0].getServerInformation();
			});
		}

		this.getServerPublicInformation = function() {
			return this._executeOperation(new c.GetServerPublicInformationOperation()).then(function(results) {
				return results.getResults()[0].getServerInformation();
			});
		}

		this.createPermIdStrings = function(count) {
			return this._executeOperation(new c.CreatePermIdsOperation(count)).then(function(results) {
				return results.getResults()[0].getPermIds();
			});
		}

		this.createCodes = function(prefix, entityKind, count) {
			return this._executeOperation(new c.CreateCodesOperation(prefix, entityKind, count)).then(function(results) {
				return results.getResults()[0].getCodes();
			});
		}

		this.createSpaces = function(creations) {
			return this._executeCreateOperation(new c.CreateSpacesOperation(creations));
		}

		this.createProjects = function(creations) {
			return this._executeCreateOperation(new c.CreateProjectsOperation(creations));
		}

		this.createExperiments = function(creations) {
			return this._executeCreateOperation(new c.CreateExperimentsOperation(creations));
		}

		this.createExperimentTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateExperimentTypesOperation(creations));
		}

		this.createExternalDms = function(creations) {
			return this._executeCreateOperation(new c.CreateExternalDmsOperation(creations));
		}

		this.createSamples = function(creations) {
			return this._executeCreateOperation(new c.CreateSamplesOperation(creations));
		}

		this.createSampleTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateSampleTypesOperation(creations));
		}

		this.createDataSetTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateDataSetTypesOperation(creations));
		}

		this.createDataSets = function(creations) {
			return this._executeCreateOperation(new c.CreateDataSetsOperation(creations));
		}

		this.createMaterials = function(creations) {
			return this._executeCreateOperation(new c.CreateMaterialsOperation(creations));
		}

		this.createMaterialTypes = function(creations) {
			return this._executeCreateOperation(new c.CreateMaterialTypesOperation(creations));
		}

		this.createPropertyTypes = function(creations) {
			return this._executeCreateOperation(new c.CreatePropertyTypesOperation(creations));
		}

		this.createPlugins = function(creations) {
			return this._executeCreateOperation(new c.CreatePluginsOperation(creations));
		}

		this.createVocabularyTerms = function(creations) {
			return this._executeCreateOperation(new c.CreateVocabularyTermsOperation(creations));
		}

		this.createVocabularies = function(creations) {
			return this._executeCreateOperation(new c.CreateVocabulariesOperation(creations));
		}

		this.createTags = function(creations) {
			return this._executeCreateOperation(new c.CreateTagsOperation(creations));
		}

		this.createAuthorizationGroups = function(creations) {
			return this._executeCreateOperation(new c.CreateAuthorizationGroupsOperation(creations));
		}

		this.createRoleAssignments = function(creations) {
			return this._executeCreateOperation(new c.CreateRoleAssignmentsOperation(creations));
		}

		this.createPersons = function(creations) {
			return this._executeCreateOperation(new c.CreatePersonsOperation(creations));
		}

		this.createSemanticAnnotations = function(creations) {
			return this._executeCreateOperation(new c.CreateSemanticAnnotationsOperation(creations));
		}

		this.createQueries = function(creations) {
			return this._executeCreateOperation(new c.CreateQueriesOperation(creations));
		}

		this.createPersonalAccessTokens = function(creations) {
			return this._executeCreateOperation(new c.CreatePersonalAccessTokensOperation(creations));
		}

		this.updateSpaces = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSpacesOperation(updates));
		}

		this.updateProjects = function(updates) {
			return this._executeUpdateOperation(new c.UpdateProjectsOperation(updates));
		}

		this.updateExperiments = function(updates) {
			return this._executeUpdateOperation(new c.UpdateExperimentsOperation(updates));
		}

		this.updateExperimentTypes = function(updates) {
			return this._executeUpdateOperation(new c.UpdateExperimentTypesOperation(updates));
		}

		this.updateSamples = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSamplesOperation(updates));
		}

		this.updateSampleTypes = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSampleTypesOperation(updates));
		}

		this.updateDataSets = function(updates) {
			return this._executeUpdateOperation(new c.UpdateDataSetsOperation(updates));
		}

		this.updateDataSetTypes = function(updates) {
			return this._executeUpdateOperation(new c.UpdateDataSetTypesOperation(updates));
		}

		this.updateMaterials = function(updates) {
			return this._executeUpdateOperation(new c.UpdateMaterialsOperation(updates));
		}

		this.updateMaterialTypes = function(updates) {
			return this._executeUpdateOperation(new c.UpdateMaterialTypesOperation(updates));
		}

		this.updateVocabularies = function(updates) {
			return this._executeUpdateOperation(new c.UpdateVocabulariesOperation(updates));
		}

		this.updatePropertyTypes = function(updates) {
			return this._executeUpdateOperation(new c.UpdatePropertyTypesOperation(updates));
		}

		this.updatePlugins = function(updates) {
			return this._executeUpdateOperation(new c.UpdatePluginsOperation(updates));
		}

		this.updateVocabularyTerms = function(updates) {
			return this._executeUpdateOperation(new c.UpdateVocabularyTermsOperation(updates));
		}

		this.updateExternalDataManagementSystems = function(updates) {
			return this._executeUpdateOperation(new c.UpdateExternalDmsOperation(updates));
		}

		this.updateTags = function(updates) {
			return this._executeUpdateOperation(new c.UpdateTagsOperation(updates));
		}

		this.updateAuthorizationGroups = function(updates) {
			return this._executeUpdateOperation(new c.UpdateAuthorizationGroupsOperation(updates));
		}

		this.updatePersons = function(updates) {
			return this._executeUpdateOperation(new c.UpdatePersonsOperation(updates));
		}

		this.updateOperationExecutions = function(updates) {
			return this._executeUpdateOperation(new c.UpdateOperationExecutionsOperation(updates));
		}

		this.updateSemanticAnnotations = function(updates) {
			return this._executeUpdateOperation(new c.UpdateSemanticAnnotationsOperation(updates));
		}

		this.updateQueries = function(updates) {
			return this._executeUpdateOperation(new c.UpdateQueriesOperation(updates));
		}

		this.updatePersonalAccessTokens = function(updates) {
			return this._executeUpdateOperation(new c.UpdatePersonalAccessTokensOperation(updates));
		}

		this.getSpaces = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSpacesOperation(ids, fetchOptions));
		}

		this.getProjects = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetProjectsOperation(ids, fetchOptions));
		}

		this.getExperiments = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetExperimentsOperation(ids, fetchOptions));
		}

		this.getExperimentTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetExperimentTypesOperation(ids, fetchOptions));
		}

		this.getSamples = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSamplesOperation(ids, fetchOptions));
		}

		this.getSampleTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSampleTypesOperation(ids, fetchOptions));
		}

		this.getDataSets = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetDataSetsOperation(ids, fetchOptions));
		}

		this.getDataSetTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetDataSetTypesOperation(ids, fetchOptions));
		}

		this.getMaterials = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetMaterialsOperation(ids, fetchOptions));
		}

		this.getMaterialTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetMaterialTypesOperation(ids, fetchOptions));
		}

		this.getPropertyTypes = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetPropertyTypesOperation(ids, fetchOptions));
		}

		this.getPlugins = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetPluginsOperation(ids, fetchOptions));
		}

		this.getVocabularies = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetVocabulariesOperation(ids, fetchOptions));
		}

		this.getVocabularyTerms = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetVocabularyTermsOperation(ids, fetchOptions));
		}

		this.getTags = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetTagsOperation(ids, fetchOptions));
		}

		this.getAuthorizationGroups = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetAuthorizationGroupsOperation(ids, fetchOptions));
		}

		this.getRoleAssignments = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetRoleAssignmentsOperation(ids, fetchOptions));
		}

		this.getPersons = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetPersonsOperation(ids, fetchOptions));
		}

		this.getSemanticAnnotations = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetSemanticAnnotationsOperation(ids, fetchOptions));
		}

		this.getExternalDataManagementSystems = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetExternalDmsOperation(ids, fetchOptions));
		}

		this.getOperationExecutions = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetOperationExecutionsOperation(ids, fetchOptions));
		}

		this.getQueries = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetQueriesOperation(ids, fetchOptions));
		}

		this.getQueryDatabases = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetQueryDatabasesOperation(ids, fetchOptions));
		}

		this.getPersonalAccessTokens = function(ids, fetchOptions) {
			return this._executeGetOperation(new c.GetPersonalAccessTokensOperation(ids, fetchOptions));
		}

		this.searchSpaces = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSpacesOperation(criteria, fetchOptions));
		}

		this.searchProjects = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchProjectsOperation(criteria, fetchOptions));
		}

		this.searchExperiments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchExperimentsOperation(criteria, fetchOptions));
		}

		this.searchExperimentTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchExperimentTypesOperation(criteria, fetchOptions));
		}

		this.searchSamples = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSamplesOperation(criteria, fetchOptions));
		}

		this.searchSampleTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSampleTypesOperation(criteria, fetchOptions));
		}

		this.searchDataSets = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDataSetsOperation(criteria, fetchOptions));
		}

		this.searchDataSetTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDataSetTypesOperation(criteria, fetchOptions));
		}

		this.searchMaterials = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchMaterialsOperation(criteria, fetchOptions));
		}

		this.searchMaterialTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchMaterialTypesOperation(criteria, fetchOptions));
		}

		this.searchPlugins = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchPluginsOperation(criteria, fetchOptions));
		}

		this.searchVocabularies = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchVocabulariesOperation(criteria, fetchOptions));
		}

		this.searchVocabularyTerms = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchVocabularyTermsOperation(criteria, fetchOptions));
		}

		this.searchExternalDataManagementSystems = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchExternalDmsOperation(criteria, fetchOptions));
		}

		this.searchTags = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchTagsOperation(criteria, fetchOptions));
		}

		this.searchAuthorizationGroups = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchAuthorizationGroupsOperation(criteria, fetchOptions));
		}

		this.searchRoleAssignments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchRoleAssignmentsOperation(criteria, fetchOptions));
		}

		this.searchPersons = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchPersonsOperation(criteria, fetchOptions));
		}

		this.searchCustomASServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchCustomASServicesOperation(criteria, fetchOptions));
		}

		this.searchSearchDomainServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSearchDomainServicesOperation(criteria, fetchOptions));
		}

		this.searchAggregationServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchAggregationServicesOperation(criteria, fetchOptions));
		}

		this.searchReportingServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchReportingServicesOperation(criteria, fetchOptions));
		}

		this.searchProcessingServices = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchProcessingServicesOperation(criteria, fetchOptions));
		}

		this.searchObjectKindModifications = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchObjectKindModificationsOperation(criteria, fetchOptions));
		}

		this.searchGlobally = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchGloballyOperation(criteria, fetchOptions));
		}

		this.searchOperationExecutions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchOperationExecutionsOperation(criteria, fetchOptions));
		}

		this.searchDataStores = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDataStoresOperation(criteria, fetchOptions));
		}

		this.searchPropertyTypes = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchPropertyTypesOperation(criteria, fetchOptions));
		}

		this.searchPropertyAssignments = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchPropertyAssignmentsOperation(criteria, fetchOptions));
		}

		this.searchSemanticAnnotations = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSemanticAnnotationsOperation(criteria, fetchOptions));
		}

		this.searchQueries = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchQueriesOperation(criteria, fetchOptions));
		}

		this.searchQueryDatabases = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchQueryDatabasesOperation(criteria, fetchOptions));
		}

		this.searchPersonalAccessTokens = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchPersonalAccessTokensOperation(criteria, fetchOptions));
		}

		this.searchSessionInformation = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchSessionInformationOperation(criteria, fetchOptions));
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSpacesOperation(ids, deletionOptions));
		}

		this.deleteProjects = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteProjectsOperation(ids, deletionOptions));
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteExperimentsOperation(ids, deletionOptions));
		}

		this.deleteSamples = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSamplesOperation(ids, deletionOptions));
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteDataSetsOperation(ids, deletionOptions));
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteMaterialsOperation(ids, deletionOptions));
		}

		this.deleteExternalDataManagementSystems = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteExternalDmsOperation(ids, deletionOptions));
		}

		this.deletePlugins = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeletePluginsOperation(ids, deletionOptions));
		}

		this.deletePropertyTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeletePropertyTypesOperation(ids, deletionOptions));
		}

		this.deleteVocabularies = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteVocabulariesOperation(ids, deletionOptions));
		}

		this.deleteVocabularyTerms = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteVocabularyTermsOperation(ids, deletionOptions));
		}

		this.deleteExperimentTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteExperimentTypesOperation(ids, deletionOptions));
		}

		this.deleteSampleTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSampleTypesOperation(ids, deletionOptions));
		}

		this.deleteDataSetTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteDataSetTypesOperation(ids, deletionOptions));
		}

		this.deleteMaterialTypes = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteMaterialTypesOperation(ids, deletionOptions));
		}

		this.deleteTags = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteTagsOperation(ids, deletionOptions));
		}

		this.deleteAuthorizationGroups = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteAuthorizationGroupsOperation(ids, deletionOptions));
		}

		this.deleteRoleAssignments = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteRoleAssignmentsOperation(ids, deletionOptions));
		}

		this.deleteOperationExecutions = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteOperationExecutionsOperation(ids, deletionOptions));
		}

		this.deleteSemanticAnnotations = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteSemanticAnnotationsOperation(ids, deletionOptions));
		}

		this.deleteQueries = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeleteQueriesOperation(ids, deletionOptions));
		}

		this.deletePersonalAccessTokens = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeletePersonalAccessTokensOperation(ids, deletionOptions));
		}

		this.deletePersons = function(ids, deletionOptions) {
			return this._executeDeleteOperation(new c.DeletePersonsOperation(ids, deletionOptions));
		}

		this.searchDeletions = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchDeletionsOperation(criteria, fetchOptions));
		}

		this.searchEvents = function(criteria, fetchOptions) {
			return this._executeSearchOperation(new c.SearchEventsOperation(criteria, fetchOptions));
		}

		this.revertDeletions = function(ids) {
			return this._executeOperation(new c.RevertDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.confirmDeletions = function(ids) {
			return this._executeOperation(new c.ConfirmDeletionsOperation(ids)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeCustomASService = function(serviceId, options) {
			return this._executeOperation(new c.ExecuteCustomASServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeAggregationService = function(serviceId, options) {
			return this._executeOperation(new c.ExecuteAggregationServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeReportingService = function(serviceId, options) {
			return this._executeOperation(new c.ExecuteReportingServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeProcessingService = function(serviceId, options) {
			return this._executeOperation(new c.ExecuteProcessingServiceOperation(serviceId, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeSearchDomainService = function(options) {
			return this._executeOperation(new c.ExecuteSearchDomainServiceOperation(options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeQuery = function(queryId, options) {
			return this._executeOperation(new c.ExecuteQueryOperation(queryId, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.executeSql = function(sql, options) {
			return this._executeOperation(new c.ExecuteSqlOperation(sql, options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.evaluatePlugin = function(options) {
			return this._executeOperation(new c.EvaluatePluginOperation(options)).then(function(results) {
				return results.getResults()[0].getResult();
			});
		}

		this.archiveDataSets = function(ids, options) {
			return this._executeOperation(new c.ArchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.unarchiveDataSets = function(ids, options) {
			return this._executeOperation(new c.UnarchiveDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.lockDataSets = function(ids, options) {
			return this._executeOperation(new c.LockDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.unlockDataSets = function(ids, options) {
			return this._executeOperation(new c.UnlockDataSetsOperation(ids, options)).then(function(results) {
				return results.getResults()[0];
			});
		}

		this.executeOperations = function(operations, options) {
			return this._openbis.executeOperations(operations, options);
		}

		this.getDataStoreFacade = function() {
			return this._openbis.getDataStoreFacade.apply(this._openbis, arguments);
		}

		this.executeImport = function(importData, importOptions) {
			return this._executeOperation(new c.ImportOperation(importData, importOptions));
		}

	}

	return facade;

});