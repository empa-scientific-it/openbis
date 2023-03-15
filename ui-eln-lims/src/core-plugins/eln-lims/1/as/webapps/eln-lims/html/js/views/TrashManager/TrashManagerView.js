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
	var deleteMessageOne = "The selected entity in the trashcan will be deleted permanently. This action cannot be undone!<br><br>Are you sure you want to continue?";
	var deleteMessageMany = "All entities in the trashcan will be deleted permanently. This action cannot be undone!<br><br>Are you sure you want to continue?";
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		var _this = this;
				
		//
		// Title and Empty all button
		//
		var deleteAllBtn = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;", "id" : "empty-trash-btn"}).append("Empty Trash");
		deleteAllBtn.click(function() {
			Util.showWarning(deleteMessageMany, function() {
				_this._trashManagerController.emptyTrash();
			});
		});
		
		$header.append($("<h1>").append("Trashcan"));
		$header.append(deleteAllBtn);
		
		//
		// Form template
		//
		var $containerColumn = $("<form>", {
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		$container.append($containerColumn);
		
		//
		// Table
		//
		var columns = [ {
			label : 'Deletion Date',
			property : 'deletionDate',
			sortable : true,
			renderFilter : function(params) {
				return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP");
			},
			filter : function(data, filter){
				return FormUtil.filterDateRangeGridColumn(data.deletionDate, filter)
			}
		} , {
			label : 'Entities',
			property : 'entities',
			render: function(data){
				return $("<div>").html(data.entities)
			},
			sortable : true
		} , {
			label : 'Reason',
			property : 'reason',
			sortable : true
		} , {
			label : "Operations",
			property : 'operations',
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown' });
				var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
				var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
				$dropDownMenu.append($caret);
				$dropDownMenu.append($list);
				
				var clickFunction = function($dropDown) {
					return function(event) {
						event.stopPropagation();
						event.preventDefault();
						$caret.dropdown('toggle');
					};
				}
				$dropDownMenu.dropdown();
				$dropDownMenu.click(clickFunction($dropDownMenu));
				
				//Options
				var $recoverOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Revert Deletions'}).append("Revert Deletions"));
				$recoverOption.click(function(e) {
					_this._trashManagerController.revertDeletions([data.entity.id]);
				});
				$list.append($recoverOption);
				
				var $removeOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Remove Permanently'}).append("Delete Permanently"));
				$removeOption.click(function(e) {
					Util.showWarning(deleteMessageOne, function() {
						_this._trashManagerController.deletePermanently([data.entity.id], false);
					});
				});
				$list.append($removeOption);
				
                var $removeIncludedOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Remove Permanently (including dependent entries in trashcan)'})
                            .append("Delete Permanently<br>&nbsp;&nbsp;(including dependent entries in trashcan)"));
                $removeIncludedOption.click(function(e) {
                    Util.showWarning(deleteMessageOne, function() {
                        _this._trashManagerController.deletePermanently([data.entity.id], true);
                    });
                });
                $list.append($removeIncludedOption);
                
				return $dropDownMenu;
			},
			filterable : false,
			sortable : false
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
                    var deletedObject = deletion.deletedObjects[enIdx];
                    var id = 'deleted-' + deletedObject.identifier + "-id";
					id = id.split("/").join("-").toLowerCase();
					list += "<br><div id = " + id + ">";
                    list += deletedObject.identifier + " (" + deletedObject.entityTypeCode + ")";
					list += "</div>";
					return list;
				}
				
                for(var enIdx = 0; enIdx < deletion.deletedObjects.length; enIdx++) {
                    var entity = deletion.deletedObjects[enIdx];
                    switch(entity.entityKind) {
						case "EXPERIMENT":
							entitiesExperimentsCount++;
							entitiesExperiments = addEntityToList("Experiments", entitiesExperiments, entity);
							break;
						case "SAMPLE":
							entitiesSamplesCount++;
							entitiesSamples = addEntityToList("" + ELNDictionary.Samples + "", entitiesSamples, entity);
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
				
                if (deletion.totalDataSetsCount > entitiesDatasetsCount) {
                    entitiesDatasets += "<br> (plus " + (deletion.totalDataSetsCount - entitiesDatasetsCount) + " more) ..."
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
					id: deletion.id,
                    deletionDate : Util.getFormatedDate(new Date(deletion.deletionDate)),
					entities : entitiesExperiments + entitiesSamples + entitiesDatasets,
					reason : deletion.reason,
					entity : deletion
				});
			}
			callback(dataList);
		}
		
		var dataGridContainer = $("<div>").css("margin-top", "-10px").css("margin-left", "-10px");
		var dataGrid = new DataGridController(null, columns, [], null, getDataList, null, true, "TRASHCAN_TABLE", false, {
			fileFormat: DataGridExportOptions.FILE_FORMAT.TSV,
			filePrefix: 'trashcan'
		}, 90);
		dataGrid.init(dataGridContainer);
		$containerColumn.append(dataGridContainer);
	}
}