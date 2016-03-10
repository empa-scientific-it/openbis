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

var DataSetViewerMode = {
    LIST : 0,
    TREE : 1
}

function DataSetViewerModel(containerId, profile, sample, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableOpenDataset) {
	this.containerId = containerId;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	
	this.profile = profile;
	this.serverFacade = serverFacade;
	
	this.sample = sample;
	this.datasets = datasets;
	
	this.enableUpload = enableUpload;
	this.enableOpenDataset = enableOpenDataset;
	this.sampleDataSets = {};
	this.datastoreDownloadURL = datastoreDownloadURL
	this.lastUsedPath = [];
	
	this.dataSetViewerMode = DataSetViewerMode.LIST;
	
	this.getDownloadLink = function(datasetCode, datasetFile, isShowSize) {
		var downloadUrl = this.datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFile.pathInDataSet) + "?sessionID=" + mainController.serverFacade.getSession();
		
		var size = null;
		if(parseInt(datasetFile.fileSize) / 1024 > 1024) {
			size = parseInt(datasetFile.fileSize) / 1024 / 1024;
			unit = "Mb";
		} else {
			size = parseInt(datasetFile.fileSize) / 1024;
			unit = "Kb";
		}
		var size = Math.floor(size * 10) / 10; //Rounded to one decimal
		
		var $link = $("<a>").attr("href", downloadUrl)
							.attr("target", "_blank")
							.append(datasetFile.pathInListing)
							.append(" ("+ size + unit +")")
							.click(function(event) {
								event.stopPropagation();
							});
		
		return $link;
	}
	
}