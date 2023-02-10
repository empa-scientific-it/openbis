/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function DataSetViewerView(dataSetViewerController, dataSetViewerModel) {
	this._dataSetViewerController = dataSetViewerController;
	this._dataSetViewerModel = dataSetViewerModel;
	this._level = 3;
	this._imagePreviewIconLoader = new ImagePreviewIconLoader();
	
	this.repaintDatasets = function() {
		var _this = this;
		
		// Container
		var $mainContainer = $("#"+this._dataSetViewerModel.containerId);
		
		// Title / Upload Button
		var $containerTitle = $("<div>", {"id" : this._dataSetViewerModel.containerIdTitle });
		var $uploadButton = "";
		if(this._dataSetViewerModel.enableUpload) {
			$uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" })).append(" Upload New Dataset");
			$uploadButton.click(function() {
			    Util.blockUI();
				if(_this._dataSetViewerModel.isExperiment()) {
					mainController.changeView('showCreateDataSetPageFromExpPermId',_this._dataSetViewerModel.entity.permId.permId);
				} else {
					mainController.changeView('showCreateDataSetPageFromPermId',_this._dataSetViewerModel.entity.permId);
				}
			});
		}
		
		$containerTitle.append($("<div>").append($uploadButton));
		
		// Container Content
		var $containerContent = $("<div>", {"id" : this._dataSetViewerModel.containerIdContent });
		$mainContainer.append($containerTitle).append($containerContent);
		
		var $filesContainer = $("<div>");
		$containerContent.append($filesContainer);
		if (this._dataSetViewerModel.enableDeepUnfolding) {
			var expandCollapseAll = FormUtil.getButtonWithIcon("glyphicon-chevron-down", function() {
				var icon = $($(this).children()[0]);
				
				if(icon.hasClass("glyphicon-chevron-down")) {
					_this._expandAll();
					icon.removeClass("glyphicon-chevron-down");
					icon.addClass("glyphicon-chevron-up");
				} else if(icon.hasClass("glyphicon-chevron-up")) {
					
					$("#filestree").fancytree("getRootNode").visit(function(node) {
					    node.setExpanded(false);
					});
					
					icon.removeClass("glyphicon-chevron-up");
					icon.addClass("glyphicon-chevron-down");
				}
				
			}, null, "Expand/Collapse all");
			$filesContainer.append(expandCollapseAll);
			
			var $treeContainer = $("<div>");
			$filesContainer.append($treeContainer);
			$filesContainer = $treeContainer;
		}
		this.repaintFilesAsTree($filesContainer);
	}
	
	this._expandAll = function() {
		var _this = this;
		var tree = $("#filestree").fancytree("getTree");
		_this._expandDeep(tree.getRootNode());
	}
	
	this._expandDeep = function(node) {
		var _this = this;
		node.setExpanded(true).done(function() {
			node.visit(function(n) {_this._expandDeep(n);});
		})
	}
	
	this.repaintFilesAsTree = function($container) {
		$container.empty();
		var _this = this;
		var $tree = $("<div>", { "id" : "filestree" });
		$container.append($tree);
		
		var treeModel = [];
		var dataSetPosInTree = 0;
		for(var datasetCode in this._dataSetViewerModel.entityDataSets) {
			var displayName = this._dataSetViewerModel.entityDataSets[datasetCode].properties[profile.propertyReplacingCode];
			if(!displayName) {
				displayName = datasetCode;
			} else {
				displayName = String(displayName).replace(/<(?:.|\n)*?>/gm, ''); //Clean any HTML tags
			}
			
			var dataset = this._dataSetViewerModel.entityDataSets[datasetCode];
			var onClick = "mainController.changeView('showViewDataSetPageFromPermId', '" + datasetCode + "');";
			var dataSetTitle = "<span id=\"dataSetPosInTree-" + dataSetPosInTree + "\" onclick=\"" + onClick + "\">"
					+ dataset.dataSetTypeCode + " : " + displayName + "</span>";
			treeModel.push({ title : dataSetTitle, key : "/", folder : true, lazy : true, datasetCode : datasetCode });
			dataSetPosInTree += 1;
		}
		
		var glyph_opts = {
			map: {
				doc: "glyphicon glyphicon-file",
				docOpen: "glyphicon glyphicon-file",
				checkbox: "glyphicon glyphicon-unchecked",
				checkboxSelected: "glyphicon glyphicon-check",
				checkboxUnknown: "glyphicon glyphicon-share",
				dragHelper: "glyphicon glyphicon-play",
				dropMarker: "glyphicon glyphicon-arrow-right",
				error: "glyphicon glyphicon-warning-sign",
				expanderClosed: "glyphicon glyphicon-plus-sign",
				expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
				expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
				folder: "glyphicon glyphicon-folder-close",
				folderOpen: "glyphicon glyphicon-folder-open",
				loading: "glyphicon glyphicon-refresh"
			}
		};
		
		var onClick = function(event, data) {

		};
		
		var onLazyLoad = function(event, data) {
			var dfd = new $.Deferred();
			data.result = dfd.promise();
			
			var pathToLoad = data.node.key;
			var parentDatasetCode = data.node.data.datasetCode;
			
			var repaintEvent = function(code, files) {
				if (!files.result) {
					Util.showError("Files can't be found, most probably the DSS is down, contact your admin.");
				} else if (_this._isSingleFolder(files)) {
					var file = files.result[0];
					_this.updateDirectoryView(parentDatasetCode, file.pathInDataSet, true, repaintEvent)
				} else {
					var results = [];
					for (var fIdx = 0; fIdx < files.result.length; fIdx++) {
						var file = files.result[fIdx];
						
						var titleValue = null;
						var imageUrl = null;
						var imageIconUrl = null;
						if (file.isDirectory) {
							titleValue = file.pathInListing;
							var directLink = _this._dataSetViewerModel.getDirectDirectoryLink(code, file.pathInDataSet);
							if (directLink) {
								titleValue = directLink + " " + titleValue;
							}
						} else {
							var $fileLink = _this._dataSetViewerModel.getDownloadLink(code, file, true);
							titleValue = $fileLink[0].outerHTML;
							if (_this._dataSetViewerModel.isAvailable(code)) {
								var previewLink = _this._dataSetViewerModel.getPreviewLink(code, file);
								imageUrl = _this._dataSetViewerModel.getImageUrl(code, file);
								imageIconUrl = _this._dataSetViewerModel.getImageIconUrl(code, file);
								if (previewLink) {
									titleValue = previewLink + " " + titleValue;
								}
							}
							var notebookLink = _this._dataSetViewerModel.getJupyterNotebookLink(code, file);
							if (profile.jupyterEndpoint && notebookLink) {
								titleValue = notebookLink + " " + titleValue;
							}
						}
						results.push({
							// node properties
							title : titleValue,
							key : file.pathInDataSet,
							folder : file.isDirectory,
							lazy : file.isDirectory,
							// custom data
							fileSize : file.fileSize,
							datasetCode : parentDatasetCode,
							imageUrl : imageUrl,
							imageIconUrl : imageIconUrl,
						});
					}
					
					dfd.resolve(results);
				}
			};
			
			_this.updateDirectoryView(parentDatasetCode, pathToLoad, true, repaintEvent);
		};
		
		var onCreateNode = function(event, data) {
			var nodePath = data.node.key;
			if (nodePath === "/") {
				var code = data.node.data.datasetCode;
				_this._handleFolderToStart(code, nodePath, function(dataSetCode, path) {
					var directLink = _this._dataSetViewerModel.getDirectDirectoryLink(dataSetCode, path);
					if (directLink) {
						data.node.setTitle(directLink + " " + data.node.title);
					}
				});
			}
		};

		var size = profile.datasetViewerSize;

		/**
		 * adds tooltip and image icon
		 */
		var onRenderNode = function(event, data) {

	        // add tooltip for images
			if ( ! data.node.data.tooltipLoaded && data.node.data.imageUrl != null) {
	            var $img = $("<img>", { src : data.node.data.imageUrl });
	            var $tooltip = $("<div>", { class : "tooltip_templates" }).append($("<span>")
	                .append($img));

	            var $span = $(data.node.span);
	            $span.tooltipster({
	                content : $tooltip,
	                position : "left",
	                functionFormat : function(instance, helper, content) {
	                    var containerWidth = $(helper.origin).offset().left;
	                    if (containerWidth < 200) {
	                    	containerWidth = $(helper.origin).width();
	                    } 
	                    containerWidth *= 0.9;
	                    var containerHeight = $(window).height() * 0.9;

	                    var $img = content.find("img");
	                    var imageSize = Util.getImageSize(containerWidth, containerHeight, $img[0].width, $img[0].height);

	                    $img.css({
	                        width : "" + imageSize.width + "px",
	                        height : "" + imageSize.height + "px",
	                        "background-color" : "white",
	                    });

	                    return content;
	                }
	            });

	            data.node.data.tooltipLoaded = true;
			}

            // add preview icon
	         if (data.node.data.imageIconUrl != null) {
	             _this._imagePreviewIconLoader.loadImagePreviewIfNotAlreadyLoaded(data.node);	             
	         }
		}

		$tree.fancytree({
			extensions: ["dnd", "edit", "glyph"], //, "wide"
			glyph: glyph_opts,
			source: treeModel,
			createNode: onCreateNode,
			click: onClick,
			lazyLoad : onLazyLoad,
			renderNode : onRenderNode,
		});
	}
	
	this._handleFolderToStart = function(dataSetCode, path, handle) {
		var _this = this;
		mainController.serverFacade.listFilesForDataSet(dataSetCode, path, false, function(files) {
			if (_this._isSingleFolder(files)) {
				var file = files.result[0];
				_this._handleFolderToStart(dataSetCode, file.pathInDataSet, handle);
			} else {
				handle(dataSetCode, path);
			}
		});
	}
	
	this.downloadLink = new function(dataSetCode, path) {
	}
	
	this.updateDirectoryView = function(code, path, notAddPath, repaintEvent) {
		var _this = this;
		mainController.serverFacade.listFilesForDataSet(code, path, false, function(files) {
			repaintEvent(code, files);
		});
	}
	
	this._isSingleFolder = function(files) {
		if (files.result.length != 1) {
			return false;
		}
		var file = files.result[0];
		if (file.isDirectory == false) {
			return false;
		}
		var currentLevel = file.pathInDataSet.split('/').length;
		if(currentLevel === 2 && 
				file.pathInListing.toUpperCase() !== "DEFAULT" &&
				!profile.isDatasetTypeCode(file.pathInListing.toUpperCase())) {
			return false;
		}
		return currentLevel < this._level
	}
}
