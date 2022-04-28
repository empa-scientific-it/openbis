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

function LinksModel(title, sampleTypeHints, isDisabled, showInfo, disableAddAnyType, sampleTypeCode, stateObj, spaceCode) {
	this.title = title;
	this.sampleTypeHints = sampleTypeHints;
	this.isDisabled = isDisabled;
	this.showInfo = showInfo;
	this.disableAddAnyType = disableAddAnyType;
	this.samplesRemoved = [];
	this.samplesAdded = [];
	this.samplesByType = {};
	this.stateObj = stateObj;
	this.spaceCode = spaceCode;
	this.sampleTypeCode = sampleTypeCode;

	//
	// These functions should only by used by the View
	//

 	this.writeState = function(sample, propertyTypeCode, propertyValue, isDelete) {
		if(isDelete) {
			FormUtil.deleteAnnotationsFromPermId(this.stateObj, sample.permId);
		} else if(!propertyTypeCode && !propertyValue) { 
			FormUtil.addAnnotationSlotForSample(this.stateObj, sample);
		} else {
			FormUtil.writeAnnotationForSample(this.stateObj, sample, propertyTypeCode, propertyValue);
		}
	}
	
	this.readState = function(permId, propertyTypeCode) {
		if(this.stateObj[permId] && this.stateObj[permId][propertyTypeCode]) {
			return this.stateObj[permId][propertyTypeCode];
		}
		return null;
	}
}