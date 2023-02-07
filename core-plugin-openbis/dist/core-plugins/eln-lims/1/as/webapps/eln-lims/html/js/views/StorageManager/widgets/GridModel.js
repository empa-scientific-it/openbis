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

function GridModel(isSelectMultiple, isDragable, gridId) {
	this.isDisabled = false;
	this.isSelectMultiple = false;
	if(isSelectMultiple) {
		this.isSelectMultiple = true;
	}
	this.isDragable = false;
	if(isDragable) {
		this.isDragable = true;
	}
	
	this.numRows = null;
	this.numColumns = null;
	this.labels = null;
	this.labelsFilter = null;
	this.dataFilter = null;
	this.useLettersOnRows = false;
	this.gridId = null;
	if(gridId) {
        this.gridId = gridId;
    }
	
	this.reset = function(numRows, numColumns, labels) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.labels = labels;
	}
	
	this.getLabels = function(posX, posY) {
		if(this.labels) {
			if(this.labels[posX]) {
				if(this.labels[posX][posY]) {
					var labelsToReturn = this.labels[posX][posY];
					if(this.labelsFilter) {
						return this.labelsFilter(posX, posY, labelsToReturn);
					} else {
						return labelsToReturn;
					}
				}
			}
		}
		
		return null;
	}
	
	
	this.getLabelDataByLabelName = function(posX, posY, name) {
		var columnData = this.labels[posX][posY];
		for(var i = 0; i < columnData.length; i++) {
			var box = columnData[i];
			if(box.displayName === name) {
				if(this.dataFilter) {
					return this.dataFilter(posX, posY, box.data);
				} else {
					return box.data;
				}
			}
		}
		return null;
	}
	
	this.isValid = function() {
		return this.numRows !== null && this.numColumns !== null;
	}
}