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

function DataSetViewerModel(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, 
        enableUpload, enableDeepUnfolding, formMode) {
    this.formMode = formMode;
	this.containerId = containerId;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	
	this.profile = profile;
	this.serverFacade = serverFacade;
	
	this.entity = entity;
	this.isExperiment = function() {
		return this.entity && this.entity["@type"] === "as.dto.experiment.Experiment";
	}
	
	this.datasets = datasets;
	this.v3Datasets = [];
	
	this.enableUpload = enableUpload;
	this.enableDeepUnfolding = enableDeepUnfolding;
	this.entityDataSets = {};
	this.datastoreDownloadURL = datastoreDownloadURL;
	
	this.getDownloadLink = function(datasetCode, datasetFile, isShowSize) {
		var downloadUrl = null;
		
		if(this.isLinkDataset(datasetCode)) {
			var cc = this.getDownloadableContentCopy(datasetCode);
			if(cc) {
				downloadUrl = profile.EDMSs[cc.externalDms.code] + "?sessionToken=" + encodeURIComponent(mainController.serverFacade.getSession())
													+ "&datasetPermId=" + encodeURIComponent(datasetCode)
													+ "&externalDMSCode=" + encodeURIComponent(cc.externalDms.code)
													+ "&contentCopyPath=" + encodeURIComponent(cc.path)
													+ "&datasetPathToFile=" + encodeURIComponent(datasetFile.pathInDataSet);
			}
		} else {
			downloadUrl = this.datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFile.pathInDataSet) + "?sessionID=" + mainController.serverFacade.getSession();
		}
		
		var $link = null;
		if(downloadUrl) {
			$link = $("<a>").attr("href", downloadUrl)
							.attr("target", "_blank")
							.append(datasetFile.pathInListing)
							.append(" ("+ PrintUtil.renderNumberOfBytes(datasetFile.fileSize) +")")
							.click(function(event) {
								event.stopPropagation();
							});
		} else {
			$link = $("<span>").text(datasetFile.pathInListing);
		}
		
		return $link;
	}

    this._isPreviewableImage = function(pathInDataSet) {
        return this._hasExtension(pathInDataSet, ["jpg", "jpeg", "png", "gif", "svg"]);
    }

	this._isIconableImage = function(pathInDataSet) {
        var filename = pathInDataSet.split('/').slice(-1)[0];
        return !filename.startsWith('.') && this._hasExtension(pathInDataSet, ["jpg", "jpeg", "png", "gif"]);
    }

	this._isJupyterNotebook = function(pathInDataSet) {
        return this._hasExtension(pathInDataSet, ["ipynb"]);
    }

	this._hasExtension = function(pathInDataSet, extensions) {
        var haveExtension = pathInDataSet.lastIndexOf(".");
        if( haveExtension !== -1 && (haveExtension + 1 < pathInDataSet.length)) {
            var extension = pathInDataSet.substring(haveExtension + 1, pathInDataSet.length).toLowerCase();
            return ($.inArray(extension, extensions) !== -1);
        }
        return false;	    
	}

	this.isAvailable = function(datasetCode) {
		var dataset = this._getDataset(datasetCode);
		return dataset && dataset.physicalData 
			&& ["AVAILABLE", "LOCKED", "ARCHIVE_PENDING", "BACKUP_PENDING"].indexOf(dataset.physicalData.status) >= 0;
	}

	this.isLinkDataset = function(datasetCode) {
		var dataset = this._getDataset(datasetCode);
		return dataset && dataset.linkedData;
	}

	this.isHistoryDataset = function(datasetCode) {
		var dataset = this._getDataset(datasetCode);	
		return dataset && dataset.properties["HISTORY_ID"];
	}
	
	this.getDownloadableContentCopy = function(datasetCode) {
		var dataset = this._getDataset(datasetCode);	
		if(dataset && dataset.linkedData) {
			var contentCopies = dataset.linkedData.contentCopies;
			for(var ccIdx = 0; ccIdx < contentCopies.length; ccIdx++) {
				var contentCopy = contentCopies[ccIdx];
				if(profile.EDMSs[contentCopy.externalDms.code]) {
					return contentCopy;
				}
			}
		}
		return null;
	}
	
	this.getDirectDirectoryLink = function(datasetCode, pathInDataSet) {
		var directLinkComponent = "";
		var isLinked = false;
		
		if(this.isLinkDataset(datasetCode)) {
			directLinkComponent = "<span onclick=\"mainController.changeView('showDatasetHierarchyTablePage', '" + datasetCode + "')\" class='glyphicon glyphicon-link'></span>";
			isLinked = true;
		}
		
		if(this.isHistoryDataset(datasetCode)) {
			directLinkComponent = "<span onclick=\"mainController.changeView('showDatasetHierarchyTablePage', '" + datasetCode + "')\" class='glyphicon glyphicon-time'></span>";
		}
		
		if(!isLinked && profile.directLinkEnabled && profile.sftpFileServer) {
			var path = null;
			
			if(this.isExperiment()) {
				path = this.entity.identifier.identifier.substring(1) + "/" + datasetCode + "/" + pathInDataSet + "/";
			} else {
				path = this.entity.experimentIdentifierOrNull.substring(1) + "/" + datasetCode + "/" + pathInDataSet + "/";
			}
				
			directLinkComponent += "<span onclick=\"" + "Util.showDirectLink('DEFAULT/" + path + "')" + "\" class='glyphicon glyphicon-hdd'></span>";
		}
		return directLinkComponent;
	}

	this.getJupyterNotebookLink = function(datasetCode, datasetFile) {
		if(this._isJupyterNotebook(datasetFile.pathInDataSet)) {
	        var notebookURL = profile.getDefaultDataStoreURL() + "/" + datasetCode + "/" + datasetFile.pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
	        var onclick = "JupyterUtil.copyNotebook(\"" + datasetCode + "\",\"" + notebookURL + "\");"
	        return "<span onclick='" + onclick + "'><img src='./img/jupyter-icon.png' style='width:17px; height:17px;' /></span>";
		}
		return null;
	}
	
	this.getPreviewLink = function(datasetCode, datasetFile) {
		if(this._isPreviewableImage(datasetFile.pathInDataSet)) {
	        var imageURLAsString = this.getImageUrl(datasetCode, datasetFile);
	        var onclick = "Util.showImage(\"" + imageURLAsString + "\");"
	        return "<span onclick='" + onclick + "' class='glyphicon glyphicon-search'></span>";
		}
		return null;
	}

	this.getImageUrl = function(datasetCode, datasetFile) {
		if(this._isPreviewableImage(datasetFile.pathInDataSet)) {
			return profile.getDefaultDataStoreURL() + "/" + datasetCode + "/" + datasetFile.pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
		}
		return null;
	}

	this.getImageIconUrl = function(datasetCode, datasetFile) {
        if(this._isIconableImage(datasetFile.pathInDataSet)) {
            return this.getImageUrl(datasetCode, datasetFile) + "&mode=thumbnail";
        }
        return null;
	}

	this._getDataset = function(datasetCode) {
		for(var idx = 0; idx < this.v3Datasets.length; idx++) {
			if (this.v3Datasets[idx].code == datasetCode) {
				return this.v3Datasets[idx];
			}
		}
	}

}
