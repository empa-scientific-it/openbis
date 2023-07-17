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

/**
 * Creates an instance of DataSetViewer.
 *
 * @constructor
 * @this {DataSetViewer}
 * @param {String} containerId The container where the DataSetViewer will be atached.
 * @param {String} profile Global configuration.
 * @param {Sample} sample The sample where to check for the data.
 * @param {ServerFacade} serverFacade Point of contact to make calls to the server
 * @param {String} datastoreDownloadURL The datastore url in format http://localhost:8889/datastore_server.
 * @param {Map} datasets API result with the datasets to show.
 * @param {Boolean} enableUpload If true, the button to create datasets is shown, this will require the sample to be present.
 */
function DataSetViewerController(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableDeepUnfolding, formMode) {
    this._datasetViewerModel = new DataSetViewerModel(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableDeepUnfolding, formMode);
	this._datasetViewerView = new DataSetViewerView(this, this._datasetViewerModel);
	
	this.init = function() {
		var _this = this;
		// Loading the datasets
		if(this._datasetViewerModel.datasets) {
			if(this._datasetViewerModel.datasets.length > 0) {
				this.updateDatasets(this._datasetViewerModel.datasets);
			}
		} else {
			var _this = this;
			if (this._datasetViewerModel.isExperiment()) {
			    _this._datasetViewerModel.dataSetsFoundOnSamples = false;
				serverFacade.listExperimentsForIdentifiers([this._datasetViewerModel.entity.identifier.identifier], function(data) {
					serverFacade.listDataSetsForExperiment(data.result[0], function(datasets) {
						var results;
						if(_this._datasetViewerModel.isExperiment()) { //Filter out datasets own by samples
							results = [];
							for(var dIdx = 0; dIdx < datasets.result.length; dIdx++) {
								var dataset = datasets.result[dIdx];
								if(!dataset.sampleIdentifierOrNull) {
									results.push(dataset);
								} else {
								    _this._datasetViewerModel.dataSetsFoundOnSamples = true;
								}
							}
						} else {
							results = datasets.result;
						}
						
						if(results.length > 0) {
							_this.updateDatasets(results);
						} else if(_this._datasetViewerModel.dataSetsFoundOnSamples) {
						    $("#" + _this._datasetViewerModel.containerId).append("DataSets found on owned " + ELNDictionary.Samples + ".");
						}
					});
				});
			} else {
				serverFacade.listDataSetsForSample(this._datasetViewerModel.entity, true, function(datasets) {
					if(datasets.result.length > 0) {
						_this.updateDatasets(datasets.result);
					}
				});
			}
		}
	}

	this._getDataSetType = function(dataset) {
        var type = dataset.dataSetTypeCode;
        if(type) {
            return type;
        }
        type = dataset.type;
        if(type) {
            return dataset.type.code;
        }
        return type;
    }
	
	this.updateDatasets = function(datasets) {
		var _this = this;
		var datasetPermIds = [];
		
		for(var i = 0; i < datasets.length; i++) { //DataSets for entity
            var type = this._getDataSetType(datasets[i]);
            if (type && (profile.showDataset(type) || profile.showDatasetOnNav(type))) {
                datasetPermIds.push(datasets[i].code);
            }
		}

		if(datasetPermIds.length === 0) {
		    mainController.sideMenu.removeSubSideMenu();
		    return;
		}

		require([ "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/fetchoptions/DataSetFetchOptions" ],
				function(DataSetPermId, DataSetFetchOptions) {
					var ids = [];
					for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
						var id = new DataSetPermId(datasetPermIds[dIdx]);
						ids.push(id);
					}
		            var fetchOptions = new DataSetFetchOptions();
                    fetchOptions.withType();
                    fetchOptions.withRegistrator();
                    fetchOptions.withModifier();
		            fetchOptions.withPhysicalData();
		            fetchOptions.withLinkedData().withExternalDms();
		            fetchOptions.withProperties();
		            var parentFetchOptions = fetchOptions.withParents();
		            parentFetchOptions.withLinkedData().withExternalDms();
		            parentFetchOptions.withProperties();
		            
					mainController.openbisV3.getDataSets(ids, fetchOptions).done(function(map) {
					
						var toShow = {
									standard : {},
									lastVersionOfLinked : {},
									lastVersionOfHistory: {}
						}
						
			            	for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
			            			var dataset = map[datasetPermIds[dIdx]];
								
								if(dataset.linkedData && dataset.linkedData.contentCopies && dataset.linkedData.contentCopies[0]) {
									var repositoryId = dataset.linkedData.contentCopies[0].gitRepositoryId;
									//console.log("dataset: " + dataset.permId.permId + " repository: " + repositoryId);
									var lastInToShow = toShow.lastVersionOfLinked[repositoryId];
									if(!lastInToShow || dataset.registrationDate > lastInToShow.registrationDate) {
										toShow.lastVersionOfLinked[repositoryId] = dataset;
									}
								} else if(dataset.properties && dataset.properties["HISTORY_ID"]) {
									var historyId = dataset.properties["HISTORY_ID"];
									//console.log("dataset: " + dataset.permId.permId + " history: " + historyId);
									var lastInToShow = toShow.lastVersionOfHistory[historyId];
									if(!lastInToShow || dataset.registrationDate > lastInToShow.registrationDate) {
										toShow.lastVersionOfHistory[historyId] = dataset;
									}
								} else {
									//console.log("dataset: " + dataset.permId.permId);
									toShow.standard[dataset.permId.permId] = dataset;
								}
						}
						
						// Rebuild other lists
						for(repositoryId in toShow.lastVersionOfLinked) {
							var dataset = toShow.lastVersionOfLinked[repositoryId];
							toShow.standard[dataset.permId.permId] = dataset;
						}
						for(historyId in toShow.lastVersionOfHistory) {
							var dataset = toShow.lastVersionOfHistory[historyId];
							toShow.standard[dataset.permId.permId] = dataset;
						}
						
						// V3 List
						for(datasetId in toShow.standard) {
							_this._datasetViewerModel.v3Datasets.push(toShow.standard[datasetId]);
						}
						
						// TODO : Legacy list to be removed
						var v1Standard = [];
						for(var i = 0; i < datasets.length; i++) { //DataSets for entity
							var dataset = datasets[i];
							if(toShow.standard[dataset.code]) {
								_this._datasetViewerModel.entityDataSets[dataset.code] = dataset;
								v1Standard.push(dataset);
							}
						}
						
						// TODO : Legacy list to be removed
						_this._datasetViewerModel.datasets = v1Standard; //In case they are loaded after the model is already created.
					
		            		_this._datasetViewerView.repaintDatasets();
		            });
		});
		
		
	}
	
}
