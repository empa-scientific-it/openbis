/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

function ExperimentFormController(mainController, mode, experiment) {
	this._mainController = mainController;
	this._experimentFormModel = new ExperimentFormModel(mode, experiment);
	this._experimentFormView = new ExperimentFormView(this, this._experimentFormModel);
	
	this.init = function(views) {
		var _this = this;
		mainController.serverFacade.getExperimentType(experiment.experimentTypeCode, function(experimentType) {
        _this._experimentFormModel.experimentType = experimentType;
		require([ "as/dto/experiment/id/ExperimentPermId", "as/dto/sample/id/SampleIdentifier", 
            "as/dto/dataset/id/DataSetPermId", "as/dto/experiment/fetchoptions/ExperimentFetchOptions" ],
            function(ExperimentPermId, SampleIdentifier, DataSetPermId, ExperimentFetchOptions) {
				if (experiment.permId) {
					var id = new ExperimentPermId(experiment.permId);
					var fetchOptions = new ExperimentFetchOptions();
					fetchOptions.withProject().withSpace();
                    fetchOptions.withType();
                    fetchOptions.withProperties();
                    fetchOptions.withSamples().withProperties();
                    fetchOptions.withDataSets().withType();
                    fetchOptions.withDataSets().withProperties();
                    fetchOptions.withDataSets().withSample();
					mainController.openbisV3.getExperiments([ id ], fetchOptions).done(function(map) {
						_this._experimentFormModel.v3_experiment = map[id];
						var expeId = _this._experimentFormModel.v3_experiment.getIdentifier().getIdentifier();
                        var dummySampleId = new SampleIdentifier(IdentifierUtil.createDummySampleIdentifierFromExperimentIdentifier(expeId));
                        var dummyDataSetId = new DataSetPermId(IdentifierUtil.createDummyDataSetIdentifierFromExperimentIdentifier(expeId));
                        mainController.openbisV3.getRights([ id , dummySampleId, dummyDataSetId], null).done(function(rightsByIds) {
                            _this._experimentFormModel.rights = rightsByIds[id];
                            _this._experimentFormModel.sampleRights = rightsByIds[dummySampleId];
                            _this._experimentFormModel.dataSetRights = rightsByIds[dummyDataSetId];
                            _this._experimentFormView.repaint(views);
						});
					});
				} else {
					_this._experimentFormView.repaint(views);
				}
		});
		});
	}
	
	this.isDirty = function() {
		return this._experimentFormModel.isFormDirty;
	}
	
	this._addCommentsWidget = function($container) {
		var commentsController = new CommentsController(this._experimentFormModel.experiment, this._experimentFormModel.mode, this._experimentFormModel);
		if(this._experimentFormModel.mode !== FormMode.VIEW || 
			this._experimentFormModel.mode === FormMode.VIEW && !commentsController.isEmpty()) {
			commentsController.init($container);
			return true;
		} else {
			return false;
		}
	}
	
	this.deleteExperiment = function(reason) {
		var _this = this;
		
		mainController.serverFacade.listSamplesForExperiments([this._experimentFormModel.experiment], function(dataSamples) {
			mainController.serverFacade.deleteExperiments([_this._experimentFormModel.experiment.id], reason, function(dataExperiment) {
				Util.unblockUI()
				if(dataExperiment.error) {
					Util.showError(dataExperiment.error.message);
				} else {
                    Util.showSuccess("" + ELNDictionary.getExperimentKindName(_this._experimentFormModel.experiment.experimentTypeCode) + " moved to Trashcan");
					
					//Delete experiment from UI
					mainController.sideMenu.deleteNodeByEntityPermId("EXPERIMENT", _this._experimentFormModel.experiment.permId, true);
				}
			});
		});
	}
	
	this.updateExperiment = function() {
		Util.blockUI();
		
		var experimentType = this._mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		
		//Identification Info (This way of collecting the identifier also works for the creation mode)
		var experimentSpace = IdentifierUtil.getSpaceCodeFromIdentifier(this._experimentFormModel.experiment.identifier);
		var experimentProject = IdentifierUtil.getProjectCodeFromExperimentIdentifier(this._experimentFormModel.experiment.identifier);
		var experimentCode = this._experimentFormModel.experiment.code;
		var experimentIdentifier = IdentifierUtil.getExperimentIdentifier(experimentSpace, experimentProject, experimentCode);
		
		var method = "";
		if(this._experimentFormModel.mode === FormMode.CREATE) {
			method = "insertExperiment";
		} else if(this._experimentFormModel.mode === FormMode.EDIT) {
			method = "updateExperiment";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"experimentType" : this._experimentFormModel.experiment.experimentTypeCode,
				"experimentIdentifier" : experimentIdentifier,
				//Properties
				"experimentProperties" : this._experimentFormModel.experiment.properties
		};
		
		var _this = this;
		
		if(this._mainController.profile.allDataStores.length > 0) {
			this._mainController.serverFacade.createReportFromAggregationService(this._mainController.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var experimentType = _this._mainController.profile.getExperimentTypeForExperimentTypeCode(_this._experimentFormModel.experiment.experimentTypeCode);
					var experimentTypeDisplayName = experimentType.description;
					if(!experimentTypeDisplayName) {
						experimentTypeDisplayName = _this._experimentFormModel.experiment.experimentTypeCode;
					}
					
					var message = "";
                    var prefix = ELNDictionary.getExperimentKindName(_this._experimentFormModel.experiment.experimentTypeCode);
					if(_this._experimentFormModel.mode === FormMode.CREATE) {
                        message = prefix + " Created.";
					} else if(_this._experimentFormModel.mode === FormMode.EDIT) {
                        message = prefix + " Updated.";
					}
					
					var callbackOk = function() {
						_this._experimentFormModel.isFormDirty = false;
						
						if(_this._experimentFormModel.mode === FormMode.CREATE) {
							_this._mainController.sideMenu.refreshCurrentNode(); //Project
						} else if(_this._experimentFormModel.mode === FormMode.EDIT) {
							_this._mainController.sideMenu.refreshNodeParentByPermId("EXPERIMENT", _this._experimentFormModel.experiment.permId);
						}
						
						var isInventory = profile.isInventorySpace(experimentSpace);
						if(isInventory) {
							_this._mainController.changeView("showSamplesPage", encodeURIComponent('["' +
									experimentIdentifier + '",false]'));
						} else {
							_this._mainController.changeView("showExperimentPageFromIdentifier",
									encodeURIComponent('["' + experimentIdentifier + '",false]'));
						}
						Util.unblockUI();
					}
					
					Util.showSuccess(message, callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
				
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}