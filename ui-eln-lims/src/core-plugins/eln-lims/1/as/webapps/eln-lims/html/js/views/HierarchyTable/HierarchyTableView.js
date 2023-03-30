/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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

function HierarchyTableView(controller, model) {
	this._model = model;
	this._controller = controller;
	this._container = $("<div>");
	this._dataGrid;
	this._hierarchyFilterController;
	
	this.repaint = function(views) {
		var _this = this;
		
		var $containerColumn = $("<form>", {
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		$containerColumn.append(this._container);
		views.content.append($containerColumn);
		
		views.header.append($("<h1>").append("Dataset Hierarchy Table: " + Util.getDisplayNameForEntity(this._model.entity)));
		
		this._hierarchyFilterController = new HierarchyFilterController(this._model.entity, function() { _this._dataGrid.refresh(); });
		this._hierarchyFilterController.init(views.header);
		this._showHierarchy();
		
	}
	
	this._showHierarchy = function() {
		var _this = this;
		
		var columns = [];
		columns.push({
			label : 'Level',
			property : 'level',
			sortable : true,
			sort : function(data1, data2, asc) {
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(data1.level, data2.level);
			}
		});
		if(this._model.entity["@type"] === "as.dto.dataset.DataSet") {
			columns.push({
				label : "Registration Date",
				property : 'registrationDate',
				sortable : true
			});
			columns.push({
				label : "History Id",
				property : 'historyId',
				sortable : true
			});
			columns.push({
				label : "Repository Id",
				property : 'repositoryId',
				sortable : true
			});
			columns.push({
				label : ELNDictionary.Sample + " Code",
				property : 'sampleCode',
				sortable : true,
				render : function(data) {
					if(data.entity.sample && data.entity.sample.permId) {
						return FormUtil.getFormLink(data.entity.sample.code, "Sample", data.entity.sample.permId.permId, null);
					}
				}
			});
			columns.push({
				label : ELNDictionary.getExperimentDualName() + " Code",
				property : 'experimentCode',
				sortable : true,
				render : function(data) {
					if(data.entity.experiment && data.entity.experiment.identifier) {
						return FormUtil.getFormLink(data.entity.experiment.code, "Experiment", data.entity.experiment.identifier.identifier, null);
					}
				}
			});
		}
		columns.push({
			label : "Type",
			property : 'type',
			sortable : true
		});
		if(this._model.entity["@type"] === "as.dto.sample.Sample") {
			columns.push({
				label : 'Identifier',
				property : 'identifier',
				sortable : true,
				render : function(data) {
				    var id = data.code.toLowerCase();
					return FormUtil.getFormLink(data.identifier, "Sample", data.permId, null, id);
				}
			});
		}
		if(this._model.entity["@type"] === "as.dto.dataset.DataSet") {
			columns.push({
				label : 'Code',
				property : 'code',
				sortable : true,
				render : function(data) {
					return FormUtil.getFormLink(data.permId, "DataSet", data.permId, null);
				}
			});
		}
		columns.push({
			label : 'Name',
			property : 'name',
			sortable : true
		});
		columns.push({
			label : 'Path',
			property : 'path',
			sortable : true
		});
		if(this._model.entity["@type"] === "as.dto.sample.Sample") {
			columns.push({
				label : 'Parent/Annotations',
				property : 'parentAnnotations',
				sortable : true,
				render : function(data) {
				    var id = "parent-annotations-" + data.entity.code.toLowerCase();
				    var $component = $("<div>", { id : id });
				    $component.append(_this._annotationsRenderer(_this._model.relationShipsMap[data.permId].parents, data.entity));
				    return $component;
				}
			});
			columns.push({
				label : 'Children/Annotations',
				property : 'childrenAnnotations',
				sortable : true,
				render : function(data) {
					var id = "children-annotations-" + data.entity.code.toLowerCase();
                    var $component = $("<div>", { id : id });
                    $component.append(_this._annotationsRenderer(_this._model.relationShipsMap[data.permId].children, data.entity));
                    return $component;
				}
			});
		}
		var getDataList = function(callback) {
			var data = _this._model.getData();
			var parentsLimit = _this._hierarchyFilterController.getParentsLimit();
			var childrenLimit = _this._hierarchyFilterController.getChildrenLimit();
			var types = _this._hierarchyFilterController.getSelectedEntityTypes();
			var filteredData = [];
			for (var i = 0; i < data.length; i++) {
				var row = data[i];
				if (row.level == 0 || ($.inArray(row.type, types) >= 0 
						&& row.level <= childrenLimit && row.level >= -parentsLimit)) {
					filteredData.push(row);
				}
			}
			callback(filteredData);
		}
		
		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, null, false, this._model.entity["@type"] + "_HIERARCHY_TABLE", false, {
			fileFormat: DataGridExportOptions.FILE_FORMAT.TSV,
			filePrefix: 'hierarchy'
		}, 90);
		this._dataGrid.init(this._container);
		this._container.prepend($("<legend>").append(" " + ELNDictionary.Sample + " Hierarchy"));
	}
	
    /*
	 * Only samples have annotations, if they are not samples, they will simply not be found
	 */
	this._annotationsRenderer = function(entities, entity) {
	    var annotations = {};
	    if(entity["@type"] === "as.dto.sample.Sample") {
	        annotations = FormUtil.getAnnotationsFromSample(entity);
	    }
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, entities, {
			startRow : function() {
				if (content !== "") {
					content += "<br /><br />";
				}
				rowStarted = true;
			},
			addKeyValue : function(key, value) {
				if (rowStarted === false) {
					content += ", ";
				}
				var label = key;
				if (key === "CODE") {
					label = "Code";
				} else {
					var propertyType = profile.getPropertyType(key);
					if (propertyType) {
						label = propertyType.label;
					}
				}
				content += "<b>" + label + "</b>: " + value;
				rowStarted = false;
			}
		})
		return content;
	}
}