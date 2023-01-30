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

function StorageListController(sample, isDisabled) {
	this._storageListModel = new StorageListModel(sample, isDisabled);
	this._storageListView = new StorageListView(this, this._storageListModel);
	
	this.init = function($container) {
		var _this = this;
		profile.getStoragesConfiguation(function(configurations) {
			_this._storageListModel.storageLabels = {};
			if(configurations) {
				for(var idx = 0; idx < configurations.length; idx++) {
					_this._storageListModel.storageLabels[configurations[idx].code] = configurations[idx].label;
				}
			}
			_this._storageListView.repaint($container);
		});
	}
	
	this._saveState = function(sampleChild, storagePropGroup){
		delete this._storageListModel.savedState;
		var savedState = {};
		for(key in storagePropGroup) {
			var propertyKey = storagePropGroup[key];
			if(key != "groupDisplayName") {
				savedState[propertyKey] = sampleChild.properties[propertyKey];
			}
		}
		this._storageListModel.savedState = savedState;
	}
	
	this._restoreState = function(sampleChild) {
		for(key in this._storageListModel.savedState) {
			sampleChild.properties[key] = this._storageListModel.savedState[key];
		}
	}
}