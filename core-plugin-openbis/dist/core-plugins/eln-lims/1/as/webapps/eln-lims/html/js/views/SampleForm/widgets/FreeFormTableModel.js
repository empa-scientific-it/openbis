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

function FreeFormTableModel(sample, isEnabled) {
	this.samplePropertyCode = "FREEFORM_TABLE_STATE";
	this.defaultTable = {
			name : "Untitled",
			modelDetailed : [['1,1', '1,2']],
			modelMini : { rows : ['1'], columns : ['1', '2'] }
	};
	
	this.getDefaultTableToAdd = function() {
		return JSON.parse(JSON.stringify( this.defaultTable ));
	}
	
	this.sample = sample;
	this.isEnabled = isEnabled;
	
	this.tables = [this.getDefaultTableToAdd()];
	
	this.removeTable = function(tableData) {
		for(var i = 0; i < this.tables.length; i++) {
			if(this.tables[i] === tableData) {
				this.tables.splice(i, 1); //Removes from model
				break;
			}
		}
	}
	
	this.addTableAtEnd = function() {
		var newTableIndex = this.tables.length;
		this.tables.splice(newTableIndex, 0, this.getDefaultTableToAdd()); //Adds to model
		var newTableModel = this.tables[newTableIndex]; //New model
		return newTableModel;
	}
	
	this.addTableAfter = function(tableData) {
		for(var i = 0; i < this.tables.length; i++) {
			if(this.tables[i] === tableData) {
				var newTableIndex = i + 1;
				this.tables.splice(newTableIndex, 0, this.getDefaultTableToAdd()); //Adds to model
				var newTableModel = this.tables[newTableIndex]; //New model
				return newTableModel;
			}
		}
	}
	
	this.selectedField = null;
}