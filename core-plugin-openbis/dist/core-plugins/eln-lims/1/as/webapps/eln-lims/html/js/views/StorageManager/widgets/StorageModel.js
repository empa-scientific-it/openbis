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

function StorageModel(configOverride, spaceCode) {
    this.spaceCode = spaceCode;
	if(configOverride) {
		this.config = configOverride;
	} else { //Default configuration, not used anywhere, given as example
		this.config = {
				title : "",
				storagePropertyGroupSelector : "on",
				storageSelector : "on",
				userSelector : "off",
				boxSelector: "on",
				boxSizeSelector: "on",
				rackSelector: "on",
				rackPositionMultiple: "off",
				rackBoxDragAndDropEnabled: "off",
				rackBoxDropEventHandler : null,
				positionSelector: "off",
				positionDropEventHandler: null,
				boxPositionMultiple: "off",
				positionDragAndDropEnabled: "off",
				storageId : "storage-drop-down-id"
		};
	}
	
	//Used to edit a sample storage
	this.sample = null; //If this sample is set, his properties are binded to the view values.
	this.isDisabled = false;
	
	this.storageConfig = null;
	this.storagePropertyGroup = null; //Selected Storage Property Group
	this.storageCode = null; //Selected Storage
	this.userIds = null;
	this.userIdsSelected = null; //Selected user Being filtered (creator or latest modifier of the box)
	this.row = null; //Selected Row
	this.column = null; //Selected Column
	this.boxName = null; //Selected Box
	this.boxSize = null; //Selected Box Size
	this.boxPosition = null;
	
	this.cleanSample = function(setUser) {
		if(this.sample) {
			this.sample.properties[this.storagePropertyGroup.rowProperty] = "";
			this.sample.properties[this.storagePropertyGroup.columnProperty] = "";
			this.sample.properties[this.storagePropertyGroup.boxProperty] = "";
			this.sample.properties[this.storagePropertyGroup.boxSizeProperty] = "";
			this.sample.properties[this.storagePropertyGroup.positionProperty] = "";
			var userId = "";
			if(setUser) {
				userId = mainController.serverFacade.getUserId();
			}
			this.sample.properties[this.storagePropertyGroup.userProperty] = userId;
		}
	}
	
	this.resetBoxInfo = function(row, column, boxName, boxSize, boxContents, boxPosition) {
		this.row = row; //Selected Row
		this.column = column; //Selected Column
		this.boxName = boxName; //Selected Box
		this.boxSize = boxSize; //Selected Box
		this.boxPosition = boxPosition;
	}
}