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
		var columns = [];
		columns.push({
			label : 'Link',
			property : 'link',
			isExportable: false,
			sortable : false,
			filterable: false,
			showByDefault: true,
			render : function(data) {
				var storagePropertyGroup = profile.getStoragePropertyGroup();
				var boxProperty = data[storagePropertyGroup.boxProperty];
				if(!boxProperty) {
					boxProperty = "NoBox";
				}
				var positionProperty = data[storagePropertyGroup.positionProperty];
				if(!positionProperty) {
					positionProperty = "NoPos";
				}
				var displayName = boxProperty + " - " + positionProperty;
				var id = displayName.split(" ").join("").toLowerCase() + "-id";
				return (data['$object'].newSample)?displayName:FormUtil.getFormLink(displayName, "Sample", data['$object'].permId, null, id);
			}
		});
		columns.push({
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : false,
			filterable: false,
			showByDefault: false,
			hide : true,
		});
		columns.push({
			label : 'Storage Name',
			property : 'storageName',
			isExportable: false,
			sortable : true,
			showByDefault: true
		});
		
		var storagePropertyCodes = profile.getAllPropertiCodesForTypeCode("STORAGE_POSITION");
		var storagePropertyCodesAsMap = {};
		var propertiesToSkip = ["$XMLCOMMENTS", "$ANNOTATIONS_STATE"];
		for(var pIdx = 0; pIdx < storagePropertyCodes.length; pIdx++) {
			if($.inArray(storagePropertyCodes[pIdx], propertiesToSkip) !== -1) {
				continue;
			}
			storagePropertyCodesAsMap[storagePropertyCodes[pIdx]] = true;
		}
		for (propertyCode in storagePropertyCodesAsMap) {
			var propertyType = profile.getPropertyType(propertyCode);
			columns.push({
				label : propertyType.label,
				property : propertyType.code,
				sortable : true
			});
		}
		
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
			
			var storagePropertyGroup = profile.getStoragePropertyGroup();
			for(var i = 0; i < sampleChildren.length; i++) {
				var sample = sampleChildren[i];
				if(sample.sampleTypeCode !== "STORAGE_POSITION" || sample.deleteSample) {
					continue;
				}
				
				var object = { '$object' : sample };
				object["id"] = sample.identifier
				object["identifier"] = sample.identifier;
				for (propertyCode in storagePropertyCodesAsMap) {
					var propertyType = profile.getPropertyType(propertyCode);
					if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
						object[propertyCode] = FormUtil.getVocabularyLabelForTermCode(propertyType, sample.properties[propertyCode]);
					} else {
						object[propertyCode] = sample.properties[propertyCode];
					}
				}
				
				if(sample && sample.properties && sample.properties[profile.getStoragePropertyGroup().nameProperty]) {
					object.storageName = _this._storageListModel.storageLabels[sample.properties[profile.getStoragePropertyGroup().nameProperty]];
				}
				dataList.push(object);
			}
			callback(dataList);
		}
		
		var rowClick = null;
		if(!this._storageListModel.isDisabled) {
			rowClick = function(data) {
				var oldSample = data.data['$object'];
				oldSample.newSample = true;
				delete oldSample["@id"];
				delete oldSample["@type"];
				_this.showStorageWidget(data.data['$object'])
			}
		}

		// multi-select delete option
		var isMultiselectable = false;
		var extraOptions = [];
		if(!this._storageListModel.isDisabled) {
			isMultiselectable = true;
			extraOptions.push({ name : "Delete", action : function(selected) {
				for (var i=0; i<selected.length; i++) {
					_this.removeChildFromSampleOrMarkToDelete(selected[i]);
				}
				_this._dataGrid.refresh();
			}});
		}

		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, "STORAGE_WIDGET", isMultiselectable, {
			fileFormat: DataGridExportOptions.FILE_FORMAT.TSV,
			filePrefix: 'storage'
		}, 60);

		var $dataGridContainer = $("<div>");
		this._dataGrid.init($dataGridContainer, extraOptions);
		
		var $storageAddButton = $("<a id='add-storage-btn'>");
		$storageAddButton.addClass("btn");
		$storageAddButton.addClass("btn-default");
		$storageAddButton.append($("<i>", { class : "glyphicon glyphicon-plus" } )).append(" New Storage Position");
		
		$storageAddButton.on("click", function(event) {
			var uuid = Util.guid();
			var newChildSample = {
					newSample : true,
					newSampleJustCreated : true,
					code : uuid,
					identifier : null,
					sampleTypeCode : "STORAGE_POSITION",
					experimentIdentifier: null,
					properties : {}
			};
			_this._storageListModel.sample.children.push(newChildSample);
			rowClick({ data : { '$object' : newChildSample }});
		});
		
		if(this._storageListModel.isDisabled) {
			$storageAddButton.attr("disabled", "");
		}
		
		$container.append($("<p>").append($storageAddButton));
	    $container.append($dataGridContainer);
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
			positionDragAndDropEnabled: "off",
			storageId : "storage-drop-down-id"
		}, this._storageListModel.sample.spaceCode);
		
		var storagePropGroup = profile.getStoragePropertyGroup();
		storageController.getModel().storagePropertyGroup = storagePropGroup;
		this._storageListController._saveState(sampleChild, storagePropGroup);
		storageController.bindSample(sampleChild, this._storageListModel.isDisabled);
		
		var storageContainer = $("#storage-pop-up-container");
		storageController.getView().repaint(storageContainer, function() {
			storageContainer.append(containerButtons);

			if(!sampleChild.newSampleJustCreated) { //Restrict storages to the ones the created group
				var storageToDropdown = storageController.getView().getStoragesDropdown();
				var selectedStorageSpace = IdentifierUtil.getSpaceCodeFromIdentifier(sampleChild.identifier);
				var selectedSpace = selectedStorageSpace.replace("STORAGE", "ELN_SETTINGS");
				// Show/hide storages from Storage To based on the one from the sample
				for(var sIdx = 0; sIdx < storageToDropdown[0].childNodes.length; sIdx++) {
					var spaceCode = storageToDropdown[0].childNodes[sIdx].attributes.spaceCode;
					var show = spaceCode && spaceCode.value === selectedSpace;
					storageToDropdown[0].childNodes[sIdx].disabled = !show;
				}
			}
			$("#storage-accept").on("click", function(event) {
				storageController.isValid(function(isValid) {
					if(isValid) {
                        // 1. Find the Space of the selected storage
                        var settingsSpaceCode = storageController.getModel().storageConfig.spaceCode;
                        var storageSpaceCode = profile.getSettingsSpacePrefix(settingsSpaceCode) + "STORAGE";
                        // 2. Set the Space the sample belongs to and with that the storage
                        if(sampleChild.newSampleJustCreated) {
                            sampleChild.identifier = IdentifierUtil.getSampleIdentifier(storageSpaceCode, null, sampleChild.code);
                            delete sampleChild.newSampleJustCreated;
                            var postFix = "";
                            if(storageSpaceCode.length > "STORAGE".length) {
                                postFix = storageSpaceCode.substring("STORAGE".length + 1);
                            }
                            sampleChild.experimentIdentifier = "/" + storageSpaceCode + "/STORAGE_POSITIONS" + postFix + "/STORAGE_POSITIONS_COLLECTION" + postFix;
                        } else {
                            // On update the identifier should be set, fail if not
                        }
						Util.unblockUI();
						_this._dataGrid.refresh();
					}
				});
			});
			
			$("#storage-cancel").on("click", function(event) {
				if(sampleChild.newSampleJustCreated) {
					_this.removeChildFromSampleOrMarkToDelete(sampleChild);
				} else {
					_this._storageListController._restoreState(sampleChild);
				}
				Util.unblockUI();
				_this._dataGrid.refresh();
			});
			
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
				if(allChildren[i].code === child.code) {
					allChildren.splice(i,1);
				}
			}
		} else {
			//Mark To delete
			child.deleteSample = true;
		}
	}
	
}
