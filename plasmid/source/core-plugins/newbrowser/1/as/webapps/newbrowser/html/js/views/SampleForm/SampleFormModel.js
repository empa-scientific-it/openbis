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

function SampleFormModel(mode, sample) {
	this.mode = mode;
	this.sample = sample;
	
	this.isFormDirty = false;
	this.isFormLoaded = false;
	
	if(this.mode === FormMode.CREATE && sample.experimentIdentifierOrNull) {
		this.isELNSubExperiment = true;
	} else if(!(this.mode === FormMode.CREATE)) {
		this.isELNSubExperiment = $.inArray(sample.spaceCode, profile.inventorySpaces) === -1 && profile.inventorySpaces.length > 0;;
	} else {
		this.isELNSubExperiment = false;
	}
	
	
	this.storages = [];
	this.dataSetViewer = null;
	
	//
	// TO-DO: Legacy code to be refactored
	//
	this.sampleLinksParents = null;
	this.sampleLinksChildren = null;
}