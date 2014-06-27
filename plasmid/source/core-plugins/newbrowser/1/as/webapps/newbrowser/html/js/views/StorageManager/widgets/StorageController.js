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

function StorageController(configOverride) {
	var _this = this;
	//Dependent widgets
	this._gridController = new GridController();
	
	this._storageModel = new StorageModel(configOverride);
	this._storageView = new StorageView(this._storageModel, this._gridController.getView());
	
	if(this._storageModel.config.boxSelector === "on") {
		this._gridController.getView().setLabelClickedEventHandler(function(posX, posY, label) {
			alert(posX + " " + posY + " " + label);
		}); 
	}
	
	if(this._storageModel.config.rackSelector === "on") {
		this._gridController.getView().setPosClickedEventHandler(function(posX, posY) {
			alert(posX + " " + posY);
		}); 
	}

	this._storageView.getSelectStorageGroupDropdown().change(function(event) {
		var storageGroupName = _this._storageView.getSelectStorageGroupDropdown().val();
		_this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(storageGroupName);
	});
	
	this._storageView.getSelectStorageDropdown().change(function(event) {
		
		//
		// Obtain Storage Configuration
		//
		var selectedStorageCode = $(this).val();
		var storageConfig = profile.getStorageConfiguation(selectedStorageCode);
		
		if(storageConfig) {
			_this._gridController.getModel().reset(storageConfig.rowNum, storageConfig.colNum);
		} else {
			_this._gridController.getModel().reset(null, null);
		}
		
		//
		// Obtain Storage Boxes
		//
		var propertyTypeCodes = [_this._storageModel.storagePropertyGroup.nameProperty];
		var propertyValues = ["'" + selectedStorageCode + "'"];
		
		mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues,
				function(samples) {
					var boxes = [];
					
					samples.forEach(function(element, index, array) {
						var boxCode = element.properties[_this._storageModel.storagePropertyGroup.boxProperty];
						var boxRow  = element.properties[_this._storageModel.storagePropertyGroup.rowProperty];
						var boxCol  = element.properties[_this._storageModel.storagePropertyGroup.columnProperty];
						
						var boxesRow = boxes[boxRow];
						if(!boxesRow) {
							boxesRow = [];
							boxes[boxRow] = boxesRow;
						}
						
						var boxesCol = boxesRow[boxCol];
						if(!boxesCol) {
							boxesCol = {};
							boxesRow[boxCol] = boxesCol;
						}
						
						boxesCol[boxCode] = true;
					});
					
					//
					// Refresh Grid with the boxes
					//
					_this._gridController.getModel().labels = boxes;
					_this._gridController.getView().repaint(_this._storageView.getGridContainer());
		});
		
		
		
	});
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
	
	this.getView = function() {
		return this._storageView;
	}
}