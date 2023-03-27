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

function ProjectFormController(mainController, mode, project) {
	this._mainController = mainController;
	this._projectFormModel = new ProjectFormModel(mode, project);
	this._projectFormView = new ProjectFormView(this, this._projectFormModel);
	
	this.init = function(views) {
		var _this = this;
		if (project.permId) {
			require([ "as/dto/project/id/ProjectPermId", "as/dto/project/fetchoptions/ProjectFetchOptions", 
				"as/dto/experiment/id/ExperimentIdentifier", "as/dto/rights/fetchoptions/RightsFetchOptions" ],
				function(ProjectPermId, ProjectFetchOptions, ExperimentIdentifier, RightsFetchOptions) {
				var id = new ProjectPermId(project.permId);
				var fetchOptions = new ProjectFetchOptions();
				fetchOptions.withSpace();
				mainController.openbisV3.getProjects([ id ], fetchOptions).done(function(map) {
					_this._projectFormModel.v3_project = map[id];
					var spaceCode = _this._projectFormModel.project.spaceCode;
					var projectCode = _this._projectFormModel.project.code;
					_this._mainController.getUserRole({
						space: spaceCode,
						project: projectCode,
					}, function(roles){
						_this._projectFormModel.roles = roles;
						var dummyId = new ExperimentIdentifier("/" + spaceCode + "/" + projectCode + "/__DUMMY_FOR_RIGHTS_CALCULATION__");
						mainController.openbisV3.getRights([id, dummyId], new RightsFetchOptions()).done(function(rightsByIds) {
							_this._projectFormModel.rights = rightsByIds[id];
							_this._projectFormModel.experimentRights = rightsByIds[dummyId];
							_this._projectFormView.repaint(views);
							Util.unblockUI();
						});
					});
				});
			});
		} else {
			_this._projectFormView.repaint(views);
			Util.unblockUI();
		}
	}

    this.getDependentEntities = function(callback) {
        var _this = this;
        require(["as/dto/project/id/ProjectPermId", "as/dto/project/fetchoptions/ProjectFetchOptions"],
        function(ProjectPermId, ProjectFetchOptions) {
            var id = new ProjectPermId(_this._projectFormModel.project.permId);
            var fetchOptions = new ProjectFetchOptions();
            fetchOptions.withExperiments();
            fetchOptions.withSamples().withExperiment();
            mainController.openbisV3.getProjects([ id ], fetchOptions).done(function(map) {
                var project = map[id];
                callback(project.getExperiments(), project.getSamples());
            });
        });
    }

    this.deleteDependentEntities = function(reason, experiments, samples) {
        Util.blockUI();
        var _this = this;
        var experimentIds = new Set();
        experiments.forEach(e => experimentIds.add(e.getPermId()));
        var independentSamples = [];
        samples.forEach(function(sample) {
            var experiment = sample.getExperiment();
            if (experiment == null || experimentIds.has(experiment.getPermId()) == false) {
                independentSamples.push(sample.getPermId());
            }
        });
        this._deleteExperiments(reason, Array.from(experimentIds), function() {
            _this._deleteSamples(reason, independentSamples, function() {
                Util.showSuccess("Entities moved to Thrashcan", function() {
                    _this._mainController.sideMenu.refreshCurrentNode();
                    Util.unblockUI();
                });
            });
        });
    }
    
    this._deleteExperiments = function(reason, experimentIds, callback) {
        if (experimentIds.length > 0) {
            require(["as/dto/experiment/delete/ExperimentDeletionOptions"],
            function(ExperimentDeletionOptions) {
                var deletionOptions = new ExperimentDeletionOptions();
                deletionOptions.setReason(reason);
                mainController.openbisV3.deleteExperiments(experimentIds, deletionOptions).done(callback);
            });
        } else {
            callback();
        }
    }
    
    this._deleteSamples = function(reason, sampleIds, callback) {
        if (sampleIds.length > 0) {
            require(["as/dto/sample/delete/SampleDeletionOptions"],
            function(SampleDeletionOptions) {
                var deletionOptions = new SampleDeletionOptions();
                deletionOptions.setReason(reason);
                mainController.openbisV3.deleteSamples(sampleIds, deletionOptions).done(callback);
            });
        } else {
            callback();
        }
    }

	this.deleteProject = function(reason) {
        var _this = this;
        var projectIdentifier = this._projectFormModel.v3_project.identifier.identifier;
        mainController.serverFacade.listDeletions(function(deletions) {
            var dependentDeletions = [];
            deletions.forEach(function(deletion) {
                var deletedObjects = deletion.getDeletedObjects();
                for (var idx = 0; idx < deletedObjects.length; idx++) {
                    var deletedObject = deletedObjects[idx];
                    var kind = deletedObject.entityKind;
                    if (kind == "EXPERIMENT" || kind == "SAMPLE") {
                        var splitted = deletedObject.identifier.split("/");
                        if (splitted.length > 3 && ("/" + splitted[1] + "/" + splitted[2]) == projectIdentifier) {
                            dependentDeletions.push(deletion);
                            break;
                        }
                    }
                };
            });
            if (dependentDeletions.length > 0) {
                var text = "This project can only be deleted if the following deletions sets in Trashcan are deleted permanently:<br>";
                dependentDeletions.forEach(function(deletion) {
                    text += Util.getFormatedDate(new Date(deletion.deletionDate)) + " (reason: " + deletion.reason + ")<br>";
                });
                Util.showInfo(text);
            } else {
                mainController.serverFacade.deleteProjects([_this._projectFormModel.project.id], reason, function(data) {
                    Util.unblockUI()
                    if(data.error) {
                        Util.showError(data.error.message);
                    } else {
                        Util.showSuccess("Project Deleted");
                        mainController.sideMenu.deleteNodeByEntityPermId("PROJECT", _this._projectFormModel.project.permId, true);
                    }
                });
            }
        });
	}
	
	this.createNewExperiment = function(experimentTypeCode) {
		var argsMap = {
				"experimentTypeCode" : experimentTypeCode,
				"projectIdentifier" : IdentifierUtil.getProjectIdentifier(this._projectFormModel.project.spaceCode, this._projectFormModel.project.code)
		}
		var argsMapStr = JSON.stringify(argsMap);
		
		this._mainController.changeView("showCreateExperimentPage", argsMapStr);
	}
	
	this.enableEditing = function() {
		this._mainController.changeView('showEditProjectPageFromPermId', this._projectFormModel.project.permId);
	}
	
	this.isDirty = function() {
		return this._projectFormModel.isFormDirty;
	}
	
	this.updateProject = function() {
		Util.blockUI();
		if(this._mainController.profile.allDataStores.length > 0) {
			var method = "";
			if(this._projectFormModel.mode === FormMode.CREATE) {
				if(!this._projectFormModel.project.code) {
					Util.showError("Code Missing.");
					return;
				}
				method = "insertProject";
			} else if(this._projectFormModel.mode === FormMode.EDIT) {
				method = "updateProject";
			}
			
			var parameters = {
					//API Method
					"method" : method,
					//Identification Info
					"projectIdentifier" : IdentifierUtil.getProjectIdentifier(this._projectFormModel.project.spaceCode, this._projectFormModel.project.code),
					"projectDescription" : this._projectFormModel.project.description
			};
			
			var _this = this;
			this._mainController.serverFacade.createReportFromAggregationService(this._mainController.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var message = "";
					if(_this._projectFormModel.mode === FormMode.CREATE) {
						message = "Project Created.";
						_this._mainController.sideMenu.refreshCurrentNode(); //Space Node
					} else if(_this._projectFormModel.mode === FormMode.EDIT) {
						message = "Project Updated.";
					}
					
					var callbackOk = function() {
						_this._projectFormModel.isFormDirty = false;
						_this._mainController.changeView("showProjectPageFromIdentifier", parameters["projectIdentifier"]);
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

	this.getDefaultSpaceValue = function (key, callback) {
		this._mainController.serverFacade.getSetting(key, callback);
	};

	this.setDefaultSpaceValue = function (key, value) {
		this._mainController.serverFacade.setSetting(key, value);
	};
}
