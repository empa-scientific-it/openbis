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

var ChangeLogType = {
		Sample : ELNDictionary.Sample
}

function StorageManagerModel() {
	this.changeLog = [];
	this.updateChangeLog = function(newChange) {
		if(newChange.type === ChangeLogType.Sample) {
			var idxToDelete = null;
			for(var cIdx = 0; cIdx < this.changeLog.length; cIdx++) {
				var item = this.changeLog[cIdx];
				if(item.data.permId === newChange.data.permId) {
					idxToDelete = cIdx;
					break;
				}
			}
			
			if(idxToDelete !== null) {
				this.changeLog.splice(idxToDelete, 1);
			}
		}
		this.changeLog.push(newChange);
	}
}