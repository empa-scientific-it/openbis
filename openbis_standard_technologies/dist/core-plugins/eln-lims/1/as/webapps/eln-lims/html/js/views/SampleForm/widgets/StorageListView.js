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
function StorageListView(storageListController, storageListModel) {
	this._storageListController = storageListController;
	this._storageListModel = storageListModel;
	this._dataGrid = null; //TO-DO This is a controller, should not be here
	
	this.repaint = function($container) {
		var _this = this;

		//
		// Data Grid
		//
		var columns = [ {
			label : 'Group',
			property : 'groupDisplayName',
			sortable : true
		} , {
			label : 'Name',
			property : 'nameProperty',
			sortable : true
		} , {
			label : 'Row',
			property : 'rowProperty',
			sortable : true
		}, {
			label : 'Column',
			property : 'columnProperty',
			sortable : true
		}, {
			label : 'Box',
			property : 'boxProperty',
			sortable : true
		}, {
			label : 'Box Size',
			property : 'boxSizeProperty',
			sortable : true
		}, {
			label : 'Position',
			property : 'positionProperty',
			sortable : true
		}, {
			label : 'User',
			property : 'userProperty',
			sortable : true
		}];
		
		if(!this._storageListModel.isDisabled) {
			columns.push(this.createOperationsColumn());
		}
		
		var getDataList = function(callback) {
			var dataList = [];
			var sampleChildren = _this._storageListModel.sample.children;
			if(!sampleChildren) {
				sampleChildren = [];
				_this._storageListModel.sample.children = sampleChildren;
			}
			var sampleType = mainController.profile.getSampleTypeForSampleTypeCode("STORAGE_POSITION");
			var storagePropertyGroup = profile.getStoragePropertyGroup();
			
			for(var i = 0; i < sampleChildren.length; i++) {
				var sample = sampleChildren[i];
				if(sample.sampleTypeCode !== "STORAGE_POSITION" || sample.deleteSample) {
					continue;
				}
				var userProperty = sample.properties[storagePropertyGroup.userProperty];
				
				var namePropertyTypeCode = storagePropertyGroup.nameProperty;
				var namePropertyCode = sample.properties[namePropertyTypeCode];
				var nameProperty = null;
				if(namePropertyCode) {
					var namePropertyTerm = profile.getVocabularyTermByCodes("STORAGE_NAMES", namePropertyCode);
					if(namePropertyTerm && namePropertyTerm.label) {
						nameProperty = namePropertyTerm.label;
					} else {
						nameProperty = namePropertyCode;
					}
				}
				
				if(	(userProperty && userProperty !== "") ||
					(nameProperty && nameProperty !== "")) {
					dataList.push({
						'$object' : sample,
						groupDisplayName : storagePropertyGroup.groupDisplayName,
						nameProperty : nameProperty,
						rowProperty : sample.properties[storagePropertyGroup.rowProperty],
						columnProperty : sample.properties[storagePropertyGroup.columnProperty],
						boxProperty : sample.properties[storagePropertyGroup.boxProperty],
						boxSizeProperty : sample.properties[storagePropertyGroup.boxSizeProperty],
						positionProperty : sample.properties[storagePropertyGroup.positionProperty],
						userProperty : userProperty
					});
				}
			}
			callback(dataList);
		}
		
		var rowClick = null;
		if(!this._storageListModel.isDisabled) {
			rowClick = function(data) {
				_this.showStorageWidget(data.data['$object'])
			}
		}
		
		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, "STORAGE_WIDGET");
		
		var $dataGridContainer = $("<div>");
		this._dataGrid.init($dataGridContainer);
		$container.append($dataGridContainer);
		
		var $storageAddButton = $("<a>", { class : 'btn btn-default', style : "float: right; background-color:#f9f9f9;" }).append($("<i>", { class : "glyphicon glyphicon-plus" } ));
		
		$storageAddButton.on("click", function(event) {
			var uuid = Util.guid();
			var newChildSample = {
					newSample : true,
					code : uuid,
					identifier : "/STORAGE/" + uuid,
					sampleTypeCode : "STORAGE_POSITION",
					properties : {}
			};
			_this._storageListModel.sample.children.push(newChildSample);
			rowClick({ data : { '$object' : newChildSample }});
		});
		
		if(this._storageListModel.isDisabled) {
			$storageAddButton.attr("disabled", "");
		}
		
		$container.append($storageAddButton);
	}
	
	this.showStorageWidget = function(sampleChild) {
		var _this = this;
		var css = {
				'text-align' : 'left',
				'top' : '5%',
				'width' : '80%',
				'left' : '10%',
				'right' : '10%',
				'overflow' : 'auto',
				'height' : '90%'
		};
		
		var container = "<div id='storage-pop-up-container'></div>";
		var containerButtons = "<a class='btn btn-default' id='storage-accept'>Accept</a> <a class='btn btn-default' id='storage-cancel'>Cancel</a>";
			
		Util.blockUI(container, css);
		
		var storageController = new StorageController({
			title : "Physical Storage",
			storagePropertyGroupSelector : "off",
			storageSelector : "on",
			userSelector : "off",
			boxSelector: "on",
			boxSizeSelector: "on",
			rackSelector: "on",
			rackPositionMultiple: "off",
			rackBoxDragAndDropEnabled: "off",
			rackBoxDropEventHandler : null,
			positionSelector: "on",
			positionDropEventHandler: null,
			boxPositionMultiple: "on",
			positionDragAndDropEnabled: "off"
		});
		
		var storagePropGroup = profile.getStoragePropertyGroup();
		storageController.getModel().storagePropertyGroup = storagePropGroup;
		this._storageListController._saveState(sampleChild, storagePropGroup);
		storageController.bindSample(sampleChild, this._storageListModel.isDisabled);
		
		var storageContainer = $("#storage-pop-up-container");
		storageController.getView().repaint(storageContainer);
		
		storageContainer.append(containerButtons);
		$("#storage-accept").on("click", function(event) {
			storageController.isValid(function(isValid) {
				if(isValid) {
					Util.unblockUI();
					_this._dataGrid.refresh();
				}
			});
		});
		
		$("#storage-cancel").on("click", function(event) {
			_this._storageListController._restoreState(sampleChild);
			Util.unblockUI();
			_this._dataGrid.refresh();
		});
	}
	
	this.createOperationsColumn = function() {
		var _this = this;
		return {
			label : "",
			property : "_Operations_",
			isExportable: false,
			showByDefault: true,
			sortable : false,
			render : function(data) {
				var $minus = FormUtil.getButtonWithIcon("glyphicon-minus", function(event) { 
					event.stopPropagation();
					event.preventDefault();
					var sample = data['$object'];
					_this.removeChildFromSampleOrMarkToDelete(sample);
					_this._dataGrid.refresh();
				}, null, "Delete");
				return $minus;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		}
	}
	
	this.removeChildFromSampleOrMarkToDelete = function(child) {
		if(child.newSample) {
			//Remove
			var allChildren = this._storageListModel.sample.children;
			for(var i = 0; i < allChildren.length; i++) {
				if(allChildren[i].permId === child.permId) {
					allChildren.splice(i,1);
				}
			}
		} else {
			//Mark To delete
			child.deleteSample = true;
		}
	}
	
}