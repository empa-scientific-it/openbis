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

function StorageView(storageController, storageModel, gridView) {
	this._storageController = storageController;
	this._storageModel = storageModel;
	this._gridView = gridView;
	
	this._storageGroupsDropDown = FormUtil.getStoragePropertyGroupsDropdown("", true);
	this._defaultStoragesDropDown = FormUtil.getDefaultStoragesDropDown("", false);
	this._userIdDropdown = $('<select>', { 'id' : 'userIdSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	this._gridContainer = $("<div>");
	this._boxField = FormUtil._getInputField("text", "", "Box Name", null, false);
	this._boxSizeDropDown = FormUtil.getDefaultStorageBoxSizesDropDown("", false);
	this._boxContentsDropDown = $('<select>', { 'id' : 'boxSamplesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	this._positionContainer = $("<div>");
	
	this.repaint = function($container) {
		//
		// Paint View
		//
		var _this = this;
		//$container.empty(); To allow display into a pop-up
		if( this._storageModel.config.title) { //It can be null
			$container.append("<h2>" + this._storageModel.config.title + "</h2>");
		}
		
		if(this._storageModel.config.storagePropertyGroupSelector === "on") {
			//Paint
			var $controlGroupStoragesGroups = FormUtil.getFieldForComponentWithLabel(this._storageGroupsDropDown, "Group");
			$container.append($controlGroupStoragesGroups);
			this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(this._storageGroupsDropDown.val());
			//Attach Event
			this._storageGroupsDropDown.change(function(event) {
				_this._storageController.setSelectStorageGroup($(this).val());
			});
		}
		
		if(this._storageModel.config.storageSelector === "on") {
			//Sample to bind
			if(this._storageModel.sample) {
				this._defaultStoragesDropDown.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.nameProperty]);
			}
			//Paint
			var $controlGroupStorages = FormUtil.getFieldForComponentWithLabel(this._defaultStoragesDropDown, "Storage");
			$container.append($controlGroupStorages);
			//Attach Event
			this._defaultStoragesDropDown.change(function(event) {
				var storageName = $(this).val();
				if(_this._storageModel.sample) { //Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.nameProperty] = storageName;
				}
				_this._storageController.setSelectStorage(storageName);
				
				if(storageName === "") {
					_this._storageModel.cleanSample(false);
				} else {
					_this._storageModel.cleanSample(true);
				}
			});
		}
		
		if(this._storageModel.config.userSelector === "on" && !this._storageModel.sample) {
			//Paint
			var $controlGroupUserId = FormUtil.getFieldForComponentWithLabel(this._userIdDropdown, "User Id Filter");
			$container.append($controlGroupUserId);
			this._userIdDropdown.multiselect();
			//Attach Event
			this._userIdDropdown.change(function() {
				var selectedUserIds = $(this).val();
				_this._storageController.setUserIdsSelected(selectedUserIds);
			});
		} else if(this._storageModel.sample) { //If someone is updating a sample, his user should go with it
			this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
		}
		
		$container.append(FormUtil.getFieldForComponentWithLabel(this._gridContainer, "Rack"));
		if(this._storageModel.sample) {
			this._storageController.setSelectStorage(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.nameProperty]);
		}
		
		if(this._storageModel.config.boxSelector === "on" || this._storageModel.config.rackSelector === "on") {
			//Paint
			this._boxField.hide();
			var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(this._boxField, "Box Name");
			$container.append($controlGroupBox);
			//Attach Event
			this._boxField.keyup(function() {
				if(_this._storageModel.sample) { // Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = $(this).val();
				}
				_this._storageController.setBoxSelected($(this).val());
			});
			// Sample to bind
			if(this._storageModel.sample && this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxProperty]) {
				this._boxField.show();
				this._boxField.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxProperty]);
				this._storageController.setBoxSelected(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxProperty]);
				this._boxField.attr("disabled", "");
			}
		}
		
		if(this._storageModel.config.boxSizeSelector === "on") {
			//Paint
			this._boxSizeDropDown.hide();
			var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(this._boxSizeDropDown, "Box Size");
			$container.append($controlGroupBox);
			//Attach Event
			this._boxSizeDropDown.change(function() {
				if(_this._storageModel.sample) { // Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = $(this).val();
				}
				_this._storageController.setBoxSizeSelected($(this).val(), true);
			});
			// Sample to bind
			if(this._storageModel.sample && this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxSizeProperty]) {
				this._boxSizeDropDown.show();
				this._boxSizeDropDown.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxSizeProperty]);
				_this._storageController.setBoxSizeSelected(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxSizeProperty], false);
				this._boxSizeDropDown.attr("disabled", "");
			}
		}
		
		if(this._storageModel.config.contentsSelector === "on") {
			//Paint
			var $controlGroupBoxContents = FormUtil.getFieldForComponentWithLabel(this._boxContentsDropDown, "Box Contents");
			$container.append($controlGroupBoxContents);
			this._boxContentsDropDown.multiselect();
			//Attach Event
			this._boxContentsDropDown.change(function() {
				var samplesOfBox = _this._gridView._gridModel.getLabelDataByLabelName(_this._storageModel.row,  _this._storageModel.column, _this._storageModel.boxName);
				var selectedSamplePermIds = $(this).val();
				var selectedSamples = [];
				for(var i = 0; i < samplesOfBox.samples.length; i++) {
					var sample = samplesOfBox.samples[i];
					if($.inArray(sample.permId, selectedSamplePermIds) !== -1) {
						selectedSamples.push(sample);
					}
				}
				_this._storageController.setBoxContentsSelected(selectedSamples);
			});
		}
		
		if(this._storageModel.config.positionSelector === "on" && this._storageModel.sample) {
			$container.append(FormUtil.getFieldForComponentWithLabel(this._positionContainer, "Box Position"));
			this.showPosField(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxSizeProperty], false);
		}
		
		if(this._storageModel.isDisabled) {
			this._storageGroupsDropDown.attr("disabled", "");
			this._defaultStoragesDropDown.attr("disabled", "");
			this._userIdDropdown.attr("disabled", "");
			this._boxField.attr("disabled", "");
			this._boxContentsDropDown.attr("disabled", "");
		}
	}
	
	this.refreshUserIdContents = function() {
		this._userIdDropdown.empty();
		var contents = this._storageModel.userIds;
		if(contents) {
			for (var i = 0; i < contents.length; i++) {
				this._userIdDropdown.append($('<option>', { 'value' : contents[i] , 'selected' : ''}).html(contents[i]));
			}
		} 
		this._userIdDropdown.multiselect('rebuild');
	}
	
	this.refreshBoxContents = function() {
		this._boxContentsDropDown.empty();
		var contents = this._storageModel.boxContents;
		if(contents) {
			for (var i = 0; i < contents.length; i++) {
				this._boxContentsDropDown.append($('<option>', { 'value' : contents[i].permId , 'selected' : ''}).html(contents[i].code));
			}
		} 
		this._boxContentsDropDown.multiselect('rebuild');
	}
	
	//
	// View specific methods
	//
	this.isNewBoxName = function() {
		return !this._boxField.prop('disabled');
	}
	
	this.resetSelectStorageDropdown = function() {
		this._defaultStoragesDropDown.val("");
	}
	
	this.refreshGrid = function() {
		this._gridView.repaint(this._gridContainer);
		
		if(this._storageModel.sample && 
				this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.rowProperty] &&
				this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.columnProperty]) {
			this._storageController._gridController.selectPosition(
					this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.rowProperty],
					this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.columnProperty]);
		}
	}
	
	this.hideBoxSizeField = function() {
		this._boxSizeDropDown.val("");
		this._boxSizeDropDown.hide();
	}
	
	this.showBoxSizeField = function() {
		this._boxSizeDropDown.val("");
		this._boxSizeDropDown.removeAttr("disabled");
		this._boxSizeDropDown.show();
	}
	
	this.hidePosField = function() {
		this._positionContainer.empty();
		this._positionContainer.append($("<p>").append("Select a box to see his contents."));
	}
	
	this.showPosField = function(boxSizeCode, isNew) {
		if(this._storageModel.config.positionSelector === "on" && this._storageModel.sample) {
			//Pointer to himself
			var _this = this;
			
			var propertyTypeCodes = [this._storageModel.storagePropertyGroup.boxProperty];
			var propertyValues = ["'" + this._storageModel.boxName + "'"];
			mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues, function(samples) {
				//Labels
				var labels = [];
				samples.forEach(function(element, index, array) {
					var code = element.code;
					var position  = element.properties[_this._storageModel.storagePropertyGroup.positionProperty];
					if(position) {
						var xyPos = Util.getXYfromLetterNumberCombination(position);
						var x = xyPos[0];
						var y = xyPos[1];
						
						var row = labels[x];
						if(!row) {
							row = [];
							labels[x] = row;
						}
						
						var col = row[y];
						if(!col) {
							col = [];
							row[y] = col;
						}
						
						label = { displayName : code, data : {} };
						col.push(label);
					} else {
						//Not position found
					}
				});
				
				//Repaint
				if(boxSizeCode) {
					_this._storageController._gridControllerPosition.getModel().useLettersOnRows = true;
					var rowsAndCols = boxSizeCode.split("X");
					var numRows = parseInt(rowsAndCols[0]);
					var numCols = parseInt(rowsAndCols[1]);
					_this._storageController._gridControllerPosition.getModel().reset(numRows, numCols, labels);
					_this._storageController._gridControllerPosition.getView().setPosSelectedEventHandler(function(posX, posY) {
						//Binded sample
						if(_this._storageModel.sample) {
							_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = Util.getLetterForNumber(posX) + posY;
						}
					}); 
					
					//
					// Box low of space alert
					//
					var positionsUsed = samples.length;
					var totalPositions = numRows * numCols;
					var used = positionsUsed / totalPositions;
					if(used >= profile.storagesConfiguration["boxSpaceLowWarning"]) {
						Util.showInfo("Box space is getting low, currently " + positionsUsed + " out of " + totalPositions + " posible positions are taken.", function() {}, true);
					}
				}
				
				_this._storageController._gridControllerPosition.init(_this._positionContainer);
				
				if(isNew) {
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
					_this._storageModel.boxPosition = null;
				} else {
					if(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty]) {
						_this._storageModel.boxPosition = _this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty];
						var xyPos = Util.getXYfromLetterNumberCombination(_this._storageModel.boxPosition);
						_this._storageController._gridControllerPosition.selectPosition(xyPos[0], xyPos[1]);
					}
				}
			});
		}
	}
	
	this.hideBoxField = function() {
		this._boxField.val("");
		this._boxField.hide();
	}
	
	this.showBoxField = function() {
		this._boxField.val("");
		this._boxField.removeAttr("disabled");
		this._boxField.show();
	}
	
	this.showBoxName = function() {
		this._boxField.val(this._storageModel.boxName);
		this._boxField.attr("disabled", "");
		this._boxField.show();
	}
	
	this.showBoxSize = function() {
		this._boxSizeDropDown.val(this._storageModel.boxSize);
		this._boxSizeDropDown.attr("disabled", "");
		this._boxSizeDropDown.show();
	}


}