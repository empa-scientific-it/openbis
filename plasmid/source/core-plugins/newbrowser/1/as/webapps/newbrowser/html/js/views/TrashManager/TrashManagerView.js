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

function TrashManagerView(trashManagerController, trashManagerModel) {
	this._trashManagerController = trashManagerController;
	this._trashManagerModel = trashManagerModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//
		// Form template and title
		//
		var $containerColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $trashIcon = $("<span>", { 'class' : 'glyphicon glyphicon-trash'});
		$containerColumn.append($("<h1>").append($trashIcon).append(" Trashcan"));
		
		//
		// Table
		//
		var columns = [ {
			label : 'Entities',
			property : 'entities',
			sortable : true
		} , {
			label : 'Reason',
			property : 'reason',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var delIdx = 0; delIdx < _this._trashManagerModel.deletions.length; delIdx++) {
				var deletion = _this._trashManagerModel.deletions[delIdx];
				
				//
				// 1. Build a text representation of the deleted entities counting how many of them have been returned.
				//
				var entitiesExperimentsCount = 0;
				var entitiesExperiments = "";
				var entitiesSamplesCount = 0;
				var entitiesSamples = "";
				var entitiesDatasetsCount = 0;
				var entitiesDatasets = "";
				
				var addEntityToList = function(type, list, entity) {
					if(list === "") {
						list =  type + ":";
					}
					list += "<br>";
					list += deletion.deletedEntities[enIdx].identifier + " (" + deletion.deletedEntities[enIdx].entityType + ")";
					return list;
				}
				
				for(var enIdx = 0; enIdx < deletion.deletedEntities.length; enIdx++) {
					var entity = deletion.deletedEntities[enIdx];
					switch(deletion.deletedEntities[enIdx].entityKind) {
						case "EXPERIMENT":
							entitiesExperimentsCount++;
							entitiesExperiments = addEntityToList("Experiments", entitiesExperiments, entity);
							break;
						case "SAMPLE":
							entitiesSamplesCount++;
							entitiesSamples = addEntityToList("Samples", entitiesSamples, entity);
							break;
						case "DATA_SET":
							entitiesDatasetsCount++;
							entitiesDatasets = addEntityToList("Datasets", entitiesDatasets, entity);
							break;
					}
				}
				
				//
				// 2. Add at the end of each entity a counter with the entities that are missing.
				//
				if(deletion.totalExperimentsCount > entitiesExperimentsCount) {
					entitiesExperiments += "<br> (plus " + (deletion.totalExperimentsCount - entitiesExperimentsCount) + " more) ..."
				}
				
				if(deletion.totalSamplesCount > entitiesSamplesCount) {
					entitiesSamples += "<br> (plus " + (deletion.totalSamplesCount - entitiesSamplesCount) + " more) ..."
				}
				
				if(deletion.totalDatasetsCount > entitiesDatasetsCount) {
					entitiesDatasets += "<br> (plus " + (deletion.totalDatasetsCount - entitiesDatasetsCount) + " more) ..."
				}
				
				//
				// 3. Small Layout fixes.
				//
				if(entitiesExperiments !== "" && entitiesSamples !== "") {
					entitiesSamples = "<br>" + entitiesSamples;
				}
				if(entitiesSamples !== "" && entitiesDatasets !== "") {
					entitiesDatasets = "<br>" + entitiesDatasets;
				}
				
				//
				// 4. Push data into list
				//
				dataList.push({
					entities : entitiesExperiments + entitiesSamples + entitiesDatasets,
					reason : deletion.reasonOrNull
				});
			}
			callback(dataList);
		}
		
		var dataGridContainer = $("<div>");
		var dataGrid = new DataGridController(null, columns, getDataList, null);
		dataGrid.init(dataGridContainer);
		$containerColumn.append(dataGridContainer);
		
		//
		// Empty all button
		//
		var deleteAllBtn = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;"}).append("Empty Trash");
		deleteAllBtn.click(function() {
			_this._trashManagerController.emptyTrash();
		});
		$containerColumn.append(deleteAllBtn);
		//
		$container.append($containerColumn);
	}
}