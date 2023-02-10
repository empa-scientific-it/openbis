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

function StorageController(configOverride, spaceCode) {
	//Pointer to himself
	var _this = this;
	
	//This controller M/V
	this._storageModel = new StorageModel(configOverride, spaceCode);
	
	//Dependent widgets
	
	this._gridController = new GridController(this._storageModel.config.rackPositionMultiple === "on", this._storageModel.config.rackBoxDragAndDropEnabled === "on", this._storageModel.config.storageId);
	this._gridControllerPosition = new GridController(this._storageModel.config.boxPositionMultiple === "on", this._storageModel.config.positionDragAndDropEnabled === "on", this._storageModel.config.storageId);
	
	this._storageView = new StorageView(this, this._storageModel, this._gridController.getView(), this._gridControllerPosition.getView());
	
	if(this._storageModel.config.boxSelector === "on") {
		this._gridController.getView().setLabelSelectedEventHandler(function(posX, posY, label, data) {
			//Binded sample
			if(_this._storageModel.sample) {
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.rowProperty] = posX;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.columnProperty] = posY;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = label;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = data.size;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
			}
			
			
			// Delete old state in model and view and set new sate in model and view
			_this._storageModel.resetBoxInfo(posX, posY, label, data.size, null, null);
			if(data.samples.length > 1 || 
					data.samples.length === 1 && data.samples[0].properties[_this._storageModel.storagePropertyGroup.boxProperty]) {
				_this._storageView.showBoxName();
			} else {
				_this._storageView.hideBoxField();
			}
			
			if(data.samples.length > 1 || 
					data.samples.length === 1 && data.samples[0].properties[_this._storageModel.storagePropertyGroup.boxSizeProperty]) {
				_this._storageView.showBoxSize();
			} else {
				_this._storageView.hideBoxSizeField();
			}
			
			if(data.samples.length > 1 || 
					data.samples.length === 1 && data.samples[0].properties[_this._storageModel.storagePropertyGroup.positionProperty]) {
				_this._storageView.showPosField(data.size, true);
			} else {
				_this._storageView.hidePosField();
			}
			
		}); 
	}
	
	if(this._storageModel.config.rackSelector === "on") {
		this._gridController.getView().setPosSelectedEventHandler(function(posX, posY) {
			//Binded sample
			if(_this._storageModel.sample) {
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.rowProperty] = posX;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.columnProperty] = posY;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = null;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = null;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
			}
			// Delete old state in model and view and set new sate in model and view
			_this._storageModel.resetBoxInfo(posX, posY, null, null, null, null);
			_this._storageView.showBoxField();
			_this._storageView.showBoxSizeField();
			_this._storageView.hidePosField();
		}); 
	}
	
	if(this._storageModel.config.rackBoxDropEventHandler !== null) {
		this._gridController.getView().setPosDropEventHandler(
			function(oldX, oldY, newX, newY, data, newDataHolder, extraDragData) {
				_this._storageModel.config.rackBoxDropEventHandler(data,
							extraDragData,
							_this._storageModel.storagePropertyGroup,
							_this._storageModel.storageCode,
							mainController.serverFacade.getUserId(),
							(Util.getLetterForNumber(oldX) + oldY),
							(Util.getLetterForNumber(newX) + newY),
							newDataHolder);
		});
	}
	
	if(this._storageModel.config.positionDropEventHandler !== null) {
		this._gridControllerPosition.getView().setPosDropEventHandler(
			function(oldX, oldY, newX, newY, data, newDataHolder, extraDragData) {
				_this._storageModel.config.positionDropEventHandler(data,
							extraDragData,
							_this._storageModel.storagePropertyGroup,
							_this._storageModel.storageCode,
							_this._storageModel.row,
							_this._storageModel.column,
							_this._storageModel.boxName,
							_this._storageModel.boxSize,
							mainController.serverFacade.getUserId(),
							(Util.getLetterForNumber(oldX) + oldY),
							(Util.getLetterForNumber(newX) + newY),
							newDataHolder);
		});
	}
	
	this.setUserIdsSelected = function(userIdsSelected) {
		this._storageModel.userIdsSelected = userIdsSelected;
		this._gridController.getModel().labelsFilter = function(posX, posY, sortedLabels) {
			var sortedLabelsToReturn = [];
			for(var i = 0; i < sortedLabels.length; i++) {
				var labelSamples = _this._gridController.getModel().getLabelDataByLabelName(posX, posY, sortedLabels[i].displayName).samples;
				var labelSamplesSelected = [];
				for(var j = 0; j < labelSamples.length; j++) {
					var labelSample = labelSamples[j];
					var sampleUserId = labelSample.properties[_this._storageModel.storagePropertyGroup.userProperty];
					if($.inArray(sampleUserId, userIdsSelected) !== -1) {
						labelSamplesSelected.push(labelSample);
					}
				}
				if(labelSamplesSelected.length !== 0) {
					sortedLabelsToReturn.push(sortedLabels[i]);
				}
			}
			return sortedLabelsToReturn;
		}
		
		this._gridController.getModel().dataFilter = function(posX, posY, labelData) {
			var labelSamplesSelected = [];
			for(var j = 0; j < labelData.samples.length; j++) {
				var labelSample = labelData.samples[j];
				var sampleUserId = labelSample.properties[_this._storageModel.storagePropertyGroup.userProperty];
				if($.inArray(sampleUserId, userIdsSelected) !== -1) {
					labelSamplesSelected.push(labelSample);
				}
			}
			return { size : labelData.size , samples : labelSamplesSelected}; //Create new data object with selected samples
		}
		
		this._storageModel.resetBoxInfo(null, null, null, null, null, null);
		this._storageView.refreshGrid();
		this._storageView.hideBoxField();
		this._storageView.hideBoxSizeField();
		this._storageView.hidePosField();
	}
	
	this.setUserIds = function(userIds) {
		this._storageModel.userIds = userIds;
		this._storageView.refreshUserIdContents();
	}
	
	this.setBoxSelected = function(boxName) {
		this._storageModel.boxName = boxName;
	}
	
	this.setBoxSizeSelected = function(boxSize, isNew) {
		var _this = this;
		this._storageModel.boxSize = boxSize;
		if(isNew) {
			this._isUserTypingExistingBox(function(error) {
				if(error !== null) {
					Util.showUserError(error, function() {}, true);
				} else {
					_this._storageView.showPosField(boxSize, isNew);
				}
			});
		}
	}
	
	this._deleteRackBoxContentStateInModelView = function() {
		// Delete old state in model and view and set new sate in model and view
		this._storageModel.resetBoxInfo(null, null, null, null, null, null);
		this._storageView.hideBoxField();
		this._storageView.hideBoxSizeField();
		this._storageView.hidePosField();
	}
	
	this.setSelectStorageGroup = function() {
		//Delete old state
		this._deleteRackBoxContentStateInModelView();
		this._storageView.resetSelectStorageDropdown();
		this._gridController.getModel().reset();
		
		//Set new state
		this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup();
		this._storageView.refreshGrid();
		this.updateDragEvents();
	}
	
	this.initFinish = function() {
		this.updateDragEvents();
	}
	
	this.updateDragEvents = function() {
		if(this._storageModel.config.rackBoxDropEventHandler !== null) {
			this._gridController.getView().setExtraDragData(_this._storageModel.storagePropertyGroup);
		}
		if(this._storageModel.config.positionDropEventHandler !== null) {
			this._gridControllerPosition.getView().setExtraDragData(_this._storageModel.storagePropertyGroup);
		}
	}
	
	this.setSelectStorage = function(selectedStorageCode) {
		//
		// Delete old state in model and view
		//
		this._deleteRackBoxContentStateInModelView();
		
		//
		// Obtain Storage Configuration
		//
		this._storageModel.storageCode = selectedStorageCode;
		profile.getStorageConfiguation(selectedStorageCode, function(storageConfig) {
			_this._storageModel.storageConfig = storageConfig;
			if(storageConfig) {
				_this._gridController.getModel().reset(storageConfig.rowNum, storageConfig.colNum);
			} else {
				_this._gridController.getModel().reset(null, null);
			}
			
			//
			// Obtain Storage Boxes
			//
			var propertyTypeCodes = [_this._storageModel.storagePropertyGroup.nameProperty];
			var propertyValues = [selectedStorageCode];
			
			mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues,
					function(samples) {
						var boxes = [];
						var userIds = [];
						positionsUsed = 0;
						samples.forEach(function(element, index, array) {
							
							var boxCode = element.properties[_this._storageModel.storagePropertyGroup.boxProperty];
							if(!boxCode || boxCode.trim().length === 0) {
								if(element && element.sampleTypeCode === "STORAGE_POSITION") {
									if(element.parents && element.parents[0]) {
										if(profile.propertyReplacingCode &&  element.parents[0].properties && element.parents[0].properties[profile.propertyReplacingCode]) {
											boxCode = element.parents[0].properties[profile.propertyReplacingCode];
										} else {
											boxCode = element.parents[0].code;
										}
									} else {
										boxCode = element.properties[profile.propertyReplacingCode];
										if(!boxCode) {
											boxCode = element.code;
										}
									}
								}
							}
							
							var boxSize = element.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty];
							var boxRow  = element.properties[_this._storageModel.storagePropertyGroup.rowProperty];
							var boxCol  = element.properties[_this._storageModel.storagePropertyGroup.columnProperty];
							var userId = element.properties[_this._storageModel.storagePropertyGroup.userProperty];
							
							if($.inArray(userId, userIds) === -1) {
								userIds.push(userId);
							}
							
							var boxesRow = boxes[boxRow];
							if(!boxesRow) {
								boxesRow = [];
								boxes[boxRow] = boxesRow;
							}
							
							var boxesCol = boxesRow[boxCol];
							if(!boxesCol) {
								boxesCol = [];
								boxesRow[boxCol] = boxesCol;
							}
							
							var getBoxFromCol = function(col, boxName) {
								for(var i = 0; i < col.length; i++) {
									box = col[i];
									if(box.displayName === boxName) {
										return box;
									}
								}
								return null;
							}
							
							var boxSamples = getBoxFromCol(boxesCol, boxCode);
							if(!boxSamples) {
								positionsUsed++;
								boxSamples = { displayName : boxCode, data : { size: boxSize, samples: [] } };
							} else if(!boxSamples.data.size) { //To help instances where they are migrating data where not all boxes are set
									boxSamples.data.size = boxSize;
							}
							boxSamples.data.samples.push(element);
							
							boxesCol.push(boxSamples);
						}, true);
						
						//
						// Storage low of space alert
						//
						if(storageConfig) {
							var totalPositions = storageConfig.rowNum * storageConfig.colNum * storageConfig.boxNum;
							var used = positionsUsed / totalPositions;
							var available = parseInt(storageConfig.lowRackSpaceWarning) / 100;
							if(used >= available) {
								Util.showInfo("Storage space is getting low, currently " + positionsUsed + " out of " + totalPositions + " posible positions are taken.", function() {}, true);
							}
						}
						//
						// Refresh Grid with the boxes
						//
						_this.setUserIds(userIds);
						_this._gridController.getModel().labels = boxes;
						_this._storageView.refreshGrid();
			}, false, true);
		});
	}
	
	//
	// Set the sample to bind before painting the view
	//
	this.bindSample = function(sample, isDisabled) {
		this._storageModel.sample = sample;
		this._storageModel.isDisabled = isDisabled;
		this._gridController.getModel().isDisabled = isDisabled;
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
	
	this.getView = function() {
		return this._storageView;
	}
	
	//
	// Validation
	//
	this.isValid = function(callback) {
		var _this = this;
		profile.getStorageConfiguation(this._storageModel.storageCode, function(storageConfig) {
			var validationLevel = (storageConfig)?storageConfig.validationLevel:ValidationLevel.BOX_POSITION;
			_this._isValidState(validationLevel, function(error0) {
				if(error0) {
					Util.showUserError(error0, function() {}, true);
					callback(false);
				} else if(validationLevel >= ValidationLevel.BOX){
					_this._isUserTypingExistingBox(function(error1) {
						if(error1) {
							Util.showUserError(error1, function() {}, true);
							callback(false);
						} else if(validationLevel >= ValidationLevel.BOX_POSITION){
							_this._isPositionAlreadyUsed(function(error2) {
								if(error2) {
									Util.showUserError(error2, function() {}, true);
									callback(false);
								} else {
									callback(true);
								}
							});
						} else {
							callback(true);
						}
					});
				} else {
					callback(true);
				}
			});
		});
	}
	
	this._isValidState = function(validationLevel, callback) {
		
		if( !this._storageModel.storageCode &&
			!this._storageModel.row && 
			!this._storageModel.column && 
			!this._storageModel.boxName && 
			!this._storageModel.boxSize && 
			!this._storageModel.boxPosition) { //Dirty delete case
			callback(null);
		} else if(!this._storageModel.storageCode && validationLevel >= ValidationLevel.RACK) {
			callback("Select a storage please.");
		} else if((!this._storageModel.row || !this._storageModel.column) && validationLevel >= ValidationLevel.RACK) {
			callback("Select a rack please.");
		} else if(!this._storageModel.boxName && validationLevel >= ValidationLevel.BOX) {
			callback("Select a box please.");
		} else if(!this._storageModel.boxSize && validationLevel >= ValidationLevel.BOX) {
			callback("Select a box size please.");
		} else if(!this._storageModel.boxPosition && validationLevel >= ValidationLevel.BOX_POSITION) {
			callback("Select a box position please.");
		} else {
			callback(null);
		}
	}
	
	this._isPositionAlreadyUsed = function(callback) {
		var _this = this;
		// Check user don't selects a position already selected by a sample that is not the binded one
		// ERROR: You selected a position already used by <SAMPLE_CODE>, please choose another.
		var boxPositions = this._storageModel.boxPosition.split(" ");
		var boxPositionsCalls = [];
		for(var bpIdx = 0; bpIdx < boxPositions.length; bpIdx++) {
			boxPositionsCalls.push({
				propertyTypeCodes : [this._storageModel.storagePropertyGroup.boxProperty, this._storageModel.storagePropertyGroup.positionProperty],
				propertyValues : [this._storageModel.boxName,boxPositions[bpIdx]]
			});
		}
		
		var validationFunc = null;
		validationFunc = function() {
			if(boxPositionsCalls.length > 0) {
				var validationParams = boxPositionsCalls.shift();
				mainController.serverFacade.searchWithProperties(validationParams.propertyTypeCodes, validationParams.propertyValues, function(samples) {
					var sampleCodes = [];
					var isBinded = false;
					for(var sIdx = 0; sIdx < samples.length; sIdx++) {
						sampleCodes.push(samples[sIdx].code);
						if(_this._storageModel.sample) {
							isBinded = isBinded || (samples[sIdx].code === _this._storageModel.sample.code);
						}
					}
					if(samples.length > 1) { //More than one sample in that position
						callback("There is more than one sample in that position, exactly " + sampleCodes + ", weird?, contact your administrator.");
					} else if(samples.length > 0 && !_this._storageModel.sample) { //Sample in that position
						callback("You selected a position already used by " + sampleCodes + ", please choose another.");
					} else if(samples.length === 1 && _this._storageModel.sample && !isBinded) {
						callback("You selected a position already used by " + sampleCodes + ", please choose another.");
					} else if(samples.length === 1 && _this._storageModel.sample && isBinded) {
						validationFunc();
					} else if(samples.length === 0) {
						validationFunc();
					}
				});
			} else {
				callback(null);
			}
		}
		
		validationFunc();
	}
	
	this._isUserTypingExistingBox = function(callback) {
		var _this = this;
		// Check user don't types by hand an existing box
		// ERROR: You typed by hand an already exiting box <BOX_CODE>, please click on it to auto fill correct size and available positions.
		if(this._storageView.isNewBoxName()) {
			var propertyTypeCodes = [this._storageModel.storagePropertyGroup.boxProperty];
			var propertyValues = [this._storageModel.boxName];
			mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues, function(samples) {
				if(samples.length > 0) { //Box already exists with same name
					if(samples[0].properties[_this._storageModel.storagePropertyGroup.nameProperty] === _this._storageModel.storageCode) {
						callback("You entered the name of an already existing box. Please click on the box itself to view the available positions, or select another box name.");
					} else {
						callback("You entered the name of a box already existing in another storage location. Please choose a different name.");
					}
					
				} else {
					callback(null);
				}
			});
		} else {
			callback(null);
		}
	}
}