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

function DataSetFormController(parentController, mode, entity, dataSet, isMini, dataSetV3) {
	this._parentController = parentController;
	this._dataSetFormModel = new DataSetFormModel(mode, entity, dataSet, isMini, dataSetV3);
	this._dataSetFormView = new DataSetFormView(this, this._dataSetFormModel);
	
	this.init = function(views) {
		var _this = this;
		
		mainController.serverFacade.listDataSetTypes(function(data) {
					_this._dataSetFormModel.dataSetTypes = data.result;
					mainController.serverFacade.getSetting("DataSetFormModel.isAutoUpload", function(value) {
						_this._dataSetFormModel.isAutoUpload = (value === "true");
						
						if(mode !== FormMode.CREATE) {
							var datasetPermId = dataSet.code;
							require([ "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/fetchoptions/DataSetFetchOptions" ],
								function(DataSetPermId, DataSetFetchOptions) {
									var ids = [new DataSetPermId(datasetPermId)];
									var fetchOptions = new DataSetFetchOptions();
									fetchOptions.withLinkedData().withExternalDms();
									fetchOptions.withExperiment();
									fetchOptions.withSample();
									mainController.openbisV3.getDataSets(ids, fetchOptions).done(function(map) {
										_this._dataSetFormModel.v3_dataset = map[datasetPermId];
										_this._dataSetFormModel.linkedData = map[datasetPermId].linkedData;
										mainController.openbisV3.getRights(ids, null).done(function(rightsByIds) {
											_this._dataSetFormModel.rights = rightsByIds[datasetPermId];
											_this._dataSetFormView.repaint(views);
										});
									});
							});
						} else {
							_this._dataSetFormView.repaint(views);
						}
					});
		});
	}
	
	this.isDirty = function() {
		return this._dataSetFormModel.isFormDirty;
	}
	
	this._addCommentsWidget = function($container) {
		var commentsController = new CommentsController(this._dataSetFormModel.dataSet, this._dataSetFormModel.mode, this._dataSetFormModel);
		if(this._dataSetFormModel.mode !== FormMode.VIEW || 
			this._dataSetFormModel.mode === FormMode.VIEW && !commentsController.isEmpty()) {
			commentsController.init($container);
			return true;
		} else {
			return false;
		}
	}
	
	this._getDataSetType = function(typeCode) {
		for(var i = 0; i < this._dataSetFormModel.dataSetTypes.length; i++) {
			if(this._dataSetFormModel.dataSetTypes[i].code === typeCode) {
				return this._dataSetFormModel.dataSetTypes[i];
			}
		}
		return null;
	}
	
	this.deleteDataSet = function(reason) {
		var _this = this;
		Util.blockUI();
		mainController.serverFacade.deleteDataSets([this._dataSetFormModel.dataSet.code], reason, function(data) {
			if(data.error) {
				Util.showError(data.error.message);
			} else {
				Util.showSuccess("Data Set Deleted");
				
//				setTimeout(function() { //Give some time to update the index
					var space = null;
					if(_this._dataSetFormModel.isExperiment()) {
						mainController.changeView('showExperimentPageFromIdentifier', _this._dataSetFormModel.entity.identifier.identifier);
						experimentIdentifier = _this._dataSetFormModel.entity.identifier.identifier;
						space = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
					} else {
						mainController.changeView('showViewSamplePageFromPermId', _this._dataSetFormModel.entity.permId);
						sampleIdentifier = _this._dataSetFormModel.entity.identifier;
						space = IdentifierUtil.getSpaceCodeFromIdentifier(sampleIdentifier);
					}
					
					var isInventory = profile.isInventorySpace(space);
					if(!isInventory) {
						mainController.sideMenu.refreshNodeParent(_this._dataSetFormModel.dataSet.code);
					}
//				}, 3000);
			}
		});
	}
	
	this._showError = function(errorMessage) {
		Util.blockUI();
		Util.showUserError(errorMessage, function() { Util.unblockUI(); });
	}
	//
	// Form Submit
	//
	this.submitDataSet = function() {
		//
		// Check upload is finish
		//
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			if(this._dataSetFormModel.files.length === 0) {
				this._showError("You should upload at least one file.");
				return;
			}
			if ($('#DATASET_TYPE').val() === "") {
				this._showError("No Data Set Type specified.");
				return;
			}
			
			if(Uploader.uploadsInProgress()) {
				this._showError("Please wait the upload to finish.");
				return;
			}
		}
		
		Util.blockUI();
		var _this = this;
		
		//
		// Metadata Submit and Creation (Step 2)
		//
		var metadata = this._dataSetFormModel.dataSet.properties;
			
		var isZipDirectoryUpload = profile.isZipDirectoryUpload($('#DATASET_TYPE').val());
		if(isZipDirectoryUpload === null) {
			isZipDirectoryUpload = $("#isZipDirectoryUpload"+":checked").val() === "on";
		}
		
		var folderName = $('#folderName').val();
		if(!folderName) {
			folderName = 'DEFAULT';
		}
		
		var method = null;
		var space = null;
		var sampleIdentifier = null;
		var experimentIdentifier = null;
		
		if(this._dataSetFormModel.isExperiment()) {
			experimentIdentifier = this._dataSetFormModel.entity.identifier.identifier;
			space = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
		} else {
			sampleIdentifier = this._dataSetFormModel.entity.identifier;
			space = IdentifierUtil.getSpaceCodeFromIdentifier(sampleIdentifier);
		}
		
		var isInventory = profile.isInventorySpace(space);
		var dataSetTypeCode = null;
		var dataSetCode = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			method = "insertDataSet";
			dataSetTypeCode = $('#DATASET_TYPE').val();
		} else if(this._dataSetFormModel.mode === FormMode.EDIT) {
			method = "updateDataSet";
			dataSetCode = this._dataSetFormModel.dataSet.code;
			dataSetTypeCode = this._dataSetFormModel.dataSet.dataSetTypeCode;
		}
		
		var dataSetParents = [];
		
		if(this._dataSetFormModel.datasetParentsComponent) {
			var dataSetParentObjects = this._dataSetFormModel.datasetParentsComponent.getSelected();
			for(var oIdx = 0; oIdx < dataSetParentObjects.length; oIdx++) {
				dataSetParents.push(dataSetParentObjects[oIdx].permId.permId)
			}
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"dataSetCode" : dataSetCode, //Used for updates
				"sampleIdentifier" : sampleIdentifier, //Use for creation
				"experimentIdentifier" : experimentIdentifier, //Use for creation
				"dataSetParents" : dataSetParents,
				"dataSetType" : dataSetTypeCode,
				"filenames" : _this._dataSetFormModel.files.map(filename => filename.trim()),
				"folderName" : folderName,
				"isZipDirectoryUpload" : isZipDirectoryUpload,
				//Metadata
				"metadata" : metadata,
				//For Moving files
				"sessionID" : mainController.serverFacade.openbisServer.getSession()
		};
			
		if(profile.allDataStores.length > 0) {
			mainController.serverFacade.createReportFromAggregationService(profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var callbackOk = function() {
						_this._dataSetFormModel.isFormDirty = false;
						Util.unblockUI();
						if(_this._dataSetFormModel.mode === FormMode.CREATE) {
							if(_this._dataSetFormModel.isExperiment()) {
								mainController.changeView('showExperimentPageFromIdentifier', _this._dataSetFormModel.entity.identifier.identifier);
							} else {
								mainController.changeView('showViewSamplePageFromPermId', _this._dataSetFormModel.entity.permId);
							}
						} else if(_this._dataSetFormModel.mode === FormMode.EDIT) {
							mainController.changeView('showViewDataSetPageFromPermId', _this._dataSetFormModel.dataSet.code);
						}
					}
					
					setTimeout(function() {
						if(_this._dataSetFormModel.mode === FormMode.CREATE) {
							Util.showSuccess("DataSet Created.", callbackOk);
							if(!isInventory) {
								mainController.sideMenu.refreshCurrentNode();
							}
						} else if(_this._dataSetFormModel.mode === FormMode.EDIT) {
							Util.showSuccess("DataSet Updated.", callbackOk);
							if(!isInventory) {
								mainController.sideMenu.refreshNodeParent(_this._dataSetFormModel.dataSet.code);
							}
						}
					}, 3000);
					
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}

	this.setArchivingRequested = function(archivingRequested) {
		var _this = this;
		var dataSetPermId = this._dataSetFormModel.dataSetV3.permId.permId;
		var physicalDataUpdate = { archivingRequested : archivingRequested }
		Util.blockUI();
		mainController.serverFacade.updateDataSet(dataSetPermId, physicalDataUpdate, function() {
			_this._reloadView();
			Util.unblockUI();
		});
	}

	this.setArchivingLock = function(lock) {
		var _this = this;
		var dataSetPermId = this._dataSetFormModel.dataSetV3.permId.permId;
		Util.blockUI();
		mainController.serverFacade.lockDataSet(dataSetPermId, lock, function() {
			_this._reloadView();
			Util.unblockUI();
		});
	}

	this.unarchive = function() {
		var _this = this;
		var dataSetPermId = this._dataSetFormModel.dataSetV3.permId.permId;
		mainController.serverFacade.getArchivingInfo([dataSetPermId], function(info) {
			var containerSize = info[dataSetPermId]["numberOfDataSets"].length;
			if (containerSize > 1) {
				mainController.serverFacade.getServiceProperty("ui.unarchiving.threshold.relative", "2", function(rThreshold) {
					mainController.serverFacade.getServiceProperty("ui.unarchiving.threshold.absolute", "10e9", function(aThreshold) {
						var callback = function() {
							_this.forceUnarchiving(dataSetPermId)
						};
						var totalSize = info["total size"];
						var size = info[dataSetPermId]["size"];
						var threshold = Math.max(rThreshold * size, parseFloat(aThreshold));
						if (totalSize > threshold) {
							var text = "Unarchiving this data set leads to unarchiving of additional " 
								+ (containerSize - 1) + " data sets. All these data sets need " 
								+ PrintUtil.renderNumberOfBytes(totalSize) + " memory.\n" 
								+ "Do you want to unarchive this data set anyway?";
							Util.showWarning(text, callback);
						} else {
							callback();
						}
					});
				});
			} else {
				_this.forceUnarchiving(dataSetPermId);
			}
		});
	}

	this.forceUnarchiving = function(dataSetPermId) {
		var _this = this;
		Util.blockUI();
		mainController.serverFacade.unarchiveDataSets([dataSetPermId], function() {
			_this._reloadView();
			Util.unblockUI();
		});
	}

	this._reloadView = function() {
		if(this._dataSetFormModel.mode === FormMode.VIEW) {
			mainController.changeView('showViewDataSetPageFromPermId', this._dataSetFormModel.dataSet.code);
		} else {
			mainController.changeView('showEditDataSetPageFromPermId', this._dataSetFormModel.dataSet.code);
		}
	}

}