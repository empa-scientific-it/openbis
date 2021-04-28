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

function SampleFormController(mainController, mode, sample, paginationInfo) {
	this._mainController = mainController;
	this._sampleFormModel = new SampleFormModel(mode, sample, paginationInfo);
	this._sampleFormView = new SampleFormView(this, this._sampleFormModel);
//	this._storageControllers = [];
	this._plateController = null;
	
	this.init = function(views, loadFromTemplate) {
		// Loading datasets
		var _this = this;
		_this._sampleFormModel.views = views;

		if(mode !== FormMode.CREATE) {
			require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/id/SampleIdentifier", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
					function(SamplePermId, SampleIdentifier, SampleFetchOptions) {
					var id = new SamplePermId(sample.permId);
					var fetchOptions = new SampleFetchOptions();
					fetchOptions.withSpace();
					fetchOptions.withProject();
					fetchOptions.withExperiment();
					fetchOptions.withParents();
					fetchOptions.withChildren();
					mainController.openbisV3.getSamples([ id ], fetchOptions).done(function(map) {
						_this._sampleFormModel.v3_sample = map[id];

						var hasExperiment = false;
						if(_this._sampleFormModel.v3_sample.getExperiment()) {
						    hasExperiment = true;
						}

                        var dummySampleId = null;

                        if(hasExperiment) {
						    var expeId = _this._sampleFormModel.v3_sample.getExperiment().getIdentifier().getIdentifier();
						    var dummySampleId = new SampleIdentifier(IdentifierUtil.createDummySampleIdentifierFromExperimentIdentifier(expeId));
                        }

						mainController.openbisV3.getRights([ id, dummySampleId ], null).done(function(rightsByIds) {
							_this._sampleFormModel.rights = rightsByIds[id];

							if(dummySampleId) {
							    _this._sampleFormModel.sampleRights = rightsByIds[dummySampleId];
							} else {
							     _this._sampleFormModel.sampleRights = {};
							     _this._sampleFormModel.sampleRights.rights = [];
							}


							mainController.serverFacade.listDataSetsForSample(_this._sampleFormModel.sample, true, function(datasets) {
								if(!datasets.error) {
									_this._sampleFormModel.datasets = datasets.result;
								}
								
								//Load view
								_this._sampleFormView.repaint(views);
								Util.unblockUI();
							});
						});
					});
			});
		} else {
//			if(sample.sampleTypeCode === "ORDER") {
//				mainController.serverFacade.searchWithIdentifiers(["/ELN_SETTINGS/ORDER_TEMPLATE"], function(data) {
//					if(data[0]) { //Template found
//						sample.properties = data[0].properties;
//					}
//					//Load view
//					_this._sampleFormView.repaint(views, true);
//					Util.unblockUI();
//				});
//			} else {
//				//Load view
//				_this._sampleFormView.repaint(views);
//				Util.unblockUI();
//			}
			//Load view
            _this._sampleFormView.repaint(views, loadFromTemplate);
            Util.unblockUI();
		}
		
	}
		
	this.isDirty = function() {
		return this._sampleFormModel.isFormDirty;
	}
	
	this.setDirty = function() {
		this._sampleFormModel.isFormDirty = true;
	}
	
	this.isLoaded = function() {
		return this._sampleFormModel.isFormLoaded;
	}
	
	this._addCommentsWidget = function($container) {
		var commentsController = new CommentsController(this._sampleFormModel.sample, this._sampleFormModel.mode, this._sampleFormModel);
		if(this._sampleFormModel.mode !== FormMode.VIEW || 
			this._sampleFormModel.mode === FormMode.VIEW && !commentsController.isEmpty()) {
			commentsController.init($container);
			return true;
		} else {
			return false;
		}
	}
	
	this.getLastStorageController = function() {
		return this._storageControllers[this._storageControllers.length-1];
	}
	
	this.getNextCopyCode = function(callback) {
		var _this = this;
		mainController.serverFacade.searchWithType(
				this._sampleFormModel.sample.sampleTypeCode,
				this._sampleFormModel.sample.code + "_*",
				false,
				function(results) {
					callback(_this._sampleFormModel.sample.code + "_" + (results.length + 1));
				});
	}
	
	this.deleteSample = function(reason, deleteDescendants) {
        var _this = this;
        var doDelete = function(samplesToDelete, reason) {
            console.log("deleteDescendants:"+deleteDescendants+", reason:"+reason+", samples:"+samplesToDelete);
            Util.blockUI();
            mainController.serverFacade.deleteSamples(samplesToDelete, reason, function(response) {
                if(response.error) {
                    Util.showError(response.error.message);
                } else {
                    Util.showSuccess("" + ELNDictionary.Sample + "(s) Deleted");
                    if(_this._sampleFormModel.isELNSample) {
                        mainController.sideMenu.deleteNodeByEntityPermId(_this._sampleFormModel.sample.permId, true);
                    } else {
                        mainController.changeView('showSamplesPage', _this._sampleFormModel.sample.experimentIdentifierOrNull);
                    }
                }
            });
        };

        var sampleId = this._sampleFormModel.sample.permId;
        if (deleteDescendants) {
            require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
                function(SamplePermId, SampleFetchOptions) {
                    var id = new SamplePermId(sampleId);
                    var fetchOptions = new SampleFetchOptions();
                    fetchOptions.withChildrenUsing(fetchOptions);
                    mainController.openbisV3.getSamples([id], fetchOptions).done(function(map) {
                        var samplesToDelete = [];
                        _this.gatherAllDescendants(samplesToDelete, map[id]);
                        doDelete(samplesToDelete, reason);
                    });
                });
        } else {
            var samplesToDelete = [sampleId];
            for(var idx = 0; idx < this._sampleFormModel.sample.children.length; idx++) {
                var child = this._sampleFormModel.sample.children[idx];
                if (child.sampleTypeCode === "STORAGE_POSITION") {
                    samplesToDelete.push(child.permId);
                }
            }
            doDelete(samplesToDelete, reason);
        }
    }

    this.gatherAllDescendants = function(samplePermIds, sample) {
        samplePermIds.push(sample.getPermId().getPermId());
        sample.getChildren().forEach(child => this.gatherAllDescendants(samplePermIds, child));
    }
	
	this.createUpdateCopySample = function(isCopyWithNewCode, linkParentsOnCopy, copyChildrenOnCopy, copyCommentsLogOnCopy) {
		Util.blockUI();
		var _this = this;
		
		//
		// Parents/Children Links
		//
		if(!isCopyWithNewCode && sample.sampleTypeCode !== "REQUEST") { // REQUESTS are validated below
			if(!_this._sampleFormModel.sampleLinksParents.isValid()) {
				return;
			}
			if(!_this._sampleFormModel.sampleLinksChildren.isValid()) {
				return;
			}
		}
		
		var sampleParentsFinal = _this._sampleFormModel.sampleLinksParents.getSamplesIdentifiers();
		
		var sampleParentsRemovedFinal = _this._sampleFormModel.sampleLinksParents.getSamplesRemovedIdentifiers();
		var sampleParentsAddedFinal = _this._sampleFormModel.sampleLinksParents.getSamplesAddedIdentifiers();
		
		var sampleChildrenFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesIdentifiers();
		
		var sampleChildrenRemovedFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesRemovedIdentifiers();
		var sampleChildrenAddedFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesAddedIdentifiers();
		
		//
		// Check that the same sample is not a parent and a child at the same time
		//
		var intersect_safe = function(a, b) {
		  var ai=0, bi=0;
		  var result = new Array();
		  
		  while( ai < a.length && bi < b.length )
		  {
		     if      (a[ai] < b[bi] ){ ai++; }
		     else if (a[ai] > b[bi] ){ bi++; }
		     else /* they're equal */
		     {
		       result.push(a[ai]);
		       ai++;
		       bi++;
		     }
		  }

		  return result;
		}
		
		sampleParentsFinal.sort();
		sampleChildrenFinal.sort();
		var intersection = intersect_safe(sampleParentsFinal, sampleChildrenFinal);
		if(intersection.length > 0) {
			Util.showUserError("The same entity can't be a parent and a child, please check: " + intersection);
			return;
		}
		
		//On Submit
		sample.parents = _this._sampleFormModel.sampleLinksParents.getSamples();
		var continueSampleCreation = function(sample, newSampleParents, samplesToDelete, newChangesToDo) {
			
			//
			// TODO : Remove this hack without removing the New Producs Widget 
			//
			if(sample.sampleTypeCode === "REQUEST") {
				var maxProducts;
				var minProducts;
				if(profile.sampleTypeDefinitionsExtension && 
					profile.sampleTypeDefinitionsExtension["REQUEST"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]) {
					maxProducts = profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]["MAX_COUNT"];
					minProducts = profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]["MIN_COUNT"];
				}
				
				if(maxProducts && (sampleParentsFinal.length + newSampleParents.length) > maxProducts) {
					Util.showUserError("There is more than " + maxProducts + " product.");
					return;
				}
                if (minProducts && (sampleParentsFinal.length + newSampleParents.length) < minProducts) {
                    Util.showUserError("There is less than " + minProducts + " product.");
                    return;
                }
                if (sample.parents) {
                    annotations = _this._sampleFormModel.sampleLinksParents.getAnnotations()
                    if (!annotations) {
                        Util.showUserError("Products do not have quantities specified.");
                        return;
                    }
                    for (var idx = 0; idx < sample.parents.length; idx++) {
                        parent = sample.parents[idx]
                        parentAnnotations = annotations[parent.permId]
                        if (!parentAnnotations || !parentAnnotations["ANNOTATION.REQUEST.QUANTITY_OF_ITEMS"]) {
                            Util.showUserError("Product " + parent.code + " does not have a quantity specified.");
                            return;
                        }
                    }
                }
            } else if (sample.sampleTypeCode === "ORDER") {
                if (!sample.properties["$ORDERING.ORDER_STATUS"]) {
                    Util.showUserError("Order status is undefined.");
                    return;
                }
            }
			
			//
			//Identification Info
			//
			var sampleSpace = sample.spaceCode;
			var sampleProject = null;
			var sampleExperiment = null;
			var sampleCode = sample.code;
			var properties = $.extend(true, {}, sample.properties); //Deep copy that can be modified before sending to the server and gets discarded in case of failure / simulates a rollback.
			
			//
			// Annotations
			//

			var parentsAnnotationsState = _this._sampleFormModel.sampleLinksParents.getAnnotations();
			var childrenAnnotationsState = _this._sampleFormModel.sampleLinksChildren.getAnnotations();

			if(newSampleParents) {
				var writeNew = false;
				for(var pIdx = 0; pIdx < newSampleParents.length; pIdx++) {
					var newSampleParent = newSampleParents[pIdx];
					if(newSampleParent.annotations) {
						for(var annotationKey in newSampleParent.annotations) {
							if (newSampleParent.annotations.hasOwnProperty(annotationKey)) {
								FormUtil.writeAnnotationForSample(parentsAnnotationsState, newSampleParent, annotationKey, newSampleParent.annotations[annotationKey]);
								writeNew = true;
							}
						}
					}
				}
			}

            var mergedAnnotationsState = {};
            for(var key in parentsAnnotationsState) {
                if(key in mergedAnnotationsState) {
                   throw 'Error merging annotations: Do you have the same object as parent or children?';
                }
                mergedAnnotationsState[key] = parentsAnnotationsState[key];
            }
            for(var key in childrenAnnotationsState) {
                if(key in mergedAnnotationsState) {
                   throw 'Error merging annotations: Do you have the same object as parent or children?';
                }
                mergedAnnotationsState[key] = childrenAnnotationsState[key];
            }

            if(!profile.enableNewAnnotationsBackend) { // Used by openBIS 19.X
			    properties["$ANNOTATIONS_STATE"] = FormUtil.getXMLFromAnnotations(mergedAnnotationsState);
            }

			//
			
			var experimentIdentifier = sample.experimentIdentifierOrNull;
			if(experimentIdentifier) { //If there is a experiment detected, the sample should be attached to the experiment completely.
				sampleSpace = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
				sampleProject = IdentifierUtil.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
				sampleExperiment = IdentifierUtil.getCodeFromIdentifier(experimentIdentifier);
			}
			
			//Children to create
			var samplesToCreate = [];
			_this._sampleFormModel.sampleLinksChildren.getSamples().forEach(function(child) {
				if(child.newSample) {
				  child.experimentIdentifier = experimentIdentifier;
				  child.properties = {};
				  child.children = [];
					if(profile.storagesConfiguration["isEnabled"]) {
						var uuid = Util.guid();
						var storagePosition = {
								newSample : true,
								code : uuid,
								identifier : "/STORAGE/" + uuid,
								sampleTypeCode : "STORAGE_POSITION",
								properties : {}
						};
						
						var storagePropertyGroup = profile.getStoragePropertyGroup();
						storagePosition.properties[storagePropertyGroup.nameProperty] = $("#childrenStorageSelector").val();
						storagePosition.properties[storagePropertyGroup.rowProperty] = 1;
						storagePosition.properties[storagePropertyGroup.columnProperty] = 1;
						storagePosition.properties[storagePropertyGroup.boxSizeProperty] = "1X1";
						var boxProperty = sample.code + "_EXP_RESULTS";
						if (experimentIdentifier) {
							boxProperty = experimentIdentifier.replace(/\//g,'\/') + "_" + boxProperty;
						}
						storagePosition.properties[storagePropertyGroup.boxProperty] = boxProperty;
						storagePosition.properties[storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
						storagePosition.properties[storagePropertyGroup.positionProperty] = "A1";
					
						child.children.push(storagePosition);
					}
					samplesToCreate.push(child);
				}
			});
			
			if(_this._sampleFormModel.sample.children) {
				_this._sampleFormModel.sample.children.forEach(function(child) {
					if(child.newSample) {
						samplesToCreate.push(child);
					} else if(child.deleteSample) {
						if(!samplesToDelete) {
							samplesToDelete = [];
						}
						sampleChildrenRemovedFinal.push(child.identifier);
						samplesToDelete.push(child.permId);
					}
				});
			}
			
			//Method
			var method = "";
			if(_this._sampleFormModel.mode === FormMode.CREATE) {
				method = "insertSample";
			} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
				method = "updateSample";
			}
			
			var changesToDo = [];
			
			if(_this._plateController) {
				changesToDo = _this._plateController.getChangesToDo();
			} else if(newChangesToDo) {
				changesToDo = newChangesToDo;
			}
			
			var parameters = {
					//API Method
					"method" : method,
					//Identification Info
					"sampleSpace" : sampleSpace,
					"sampleProject" : sampleProject,
					"sampleExperiment" : sampleExperiment,
					"sampleCode" : sampleCode,
					"sampleType" : sample.sampleTypeCode,
					//Other Properties
					"sampleProperties" : properties,
					//Parent links
					"sampleParents": (sampleParentsRemovedFinal.length === 0 && sampleParentsAddedFinal.length === 0)?null:sampleParentsFinal,
					"sampleParentsNew": newSampleParents,
					//Children links
					"sampleChildrenNew": samplesToCreate,
					"sampleChildrenAdded": sampleChildrenAddedFinal,
					"sampleChildrenRemoved": sampleChildrenRemovedFinal,
					//Other Samples
					"changesToDo" : changesToDo
			};
			
			//
			// Copy override - This part modifies what is done for a create/update and adds a couple of extra parameters needed to copy to the bench correctly
			//
			if(isCopyWithNewCode) {
				parameters["method"] = "copySample";
				parameters["sampleCode"] = isCopyWithNewCode;
				parameters["sampleCodeOrig"] = sampleCode;
				parameters["notCopyProperties"] = [];
				parameters["defaultBenchPropertyList"] = [];
				
				if(!copyCommentsLogOnCopy && parameters["sampleProperties"]["$XMLCOMMENTS"]) {
					delete parameters["sampleProperties"]["$XMLCOMMENTS"];
				}
				
				parameters["sampleParents"] = sampleParentsFinal;
				if(!linkParentsOnCopy) {
					parameters["sampleParents"] = [];
				}
				
				parameters["sampleChildren"] = sampleChildrenFinal;
				parameters["copyChildrenOnCopy"] = copyChildrenOnCopy;
				parameters["sampleChildrenNew"] = [];
				parameters["sampleChildrenRemoved"] = [];
			}
			
			//
			// Sending the request to the server
			//
			if(profile.getDefaultDataStoreCode()) {
				
				mainController.serverFacade.createReportFromAggregationService(profile.getDefaultDataStoreCode(), parameters, function(response) {
					_this._createUpdateCopySampleCallback(_this, isCopyWithNewCode, response, samplesToDelete, parentsAnnotationsState, childrenAnnotationsState, parameters["copyChildrenOnCopy"]);
				});
				
			} else {
				Util.showError("No DSS available.", function() {Util.unblockUI();});
			}
		}
		
		profile.sampleFormOnSubmit(sample, continueSampleCreation);
		return false;
	}
	
	this._createUpdateCopySampleCallback = function(_this, isCopyWithNewCode, response, samplesToDelete, parentsAnnotationsState, childrenAnnotationsState, copyChildrenOnCopy) {
		if(response.error) { //Error Case 1
			Util.showError(response.error.message, function() {Util.unblockUI();});
		} else if (response.result.columns[1].title === "Error") { //Error Case 2
			var stacktrace = response.result.rows[0][1].value;
			Util.showStacktraceAsError(stacktrace);
		} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
			var permId = null;
			if(response.result.columns[2].title === "RESULT" && response.result.rows[0][2].value) {
				permId = response.result.rows[0][2].value;
			}
			
			var sampleType = profile.getSampleTypeForSampleTypeCode(_this._sampleFormModel.sample.sampleTypeCode);
			var sampleTypeDisplayName = sampleType.description;
			if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = _this._sampleFormModel.sample.sampleTypeCode;
			}
			
			var message = "";
			if(isCopyWithNewCode) {
				message = "" + ELNDictionary.Sample + " copied with new code: " + isCopyWithNewCode + ".";
			} else if(_this._sampleFormModel.mode === FormMode.CREATE) {
				message = "" + ELNDictionary.Sample + " Created.";
			} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
				message = "" + ELNDictionary.Sample + " Updated.";
			}
			
			var callbackOk = function() {
				if((isCopyWithNewCode || _this._sampleFormModel.mode === FormMode.CREATE || _this._sampleFormModel.mode === FormMode.EDIT) && _this._sampleFormModel.isELNSample) {
					if(_this._sampleFormModel.mode === FormMode.CREATE) {
						mainController.sideMenu.refreshCurrentNode();
					} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
						mainController.sideMenu.refreshNodeParent(_this._sampleFormModel.sample.permId);
					}
				}
				
				var searchUntilFound = null;
				    searchUntilFound = function() {
					mainController.serverFacade.searchWithUniqueId(permId, function(data) {
						if(data && data.length > 0) {
							mainController.changeView('showViewSamplePageFromPermId',data[0].permId);
							Util.unblockUI();
						} else { // Recursive call, only if not found yet due to reindexing
							setTimeout(searchUntilFound, 100);
						}
					});
				}
				searchUntilFound(); //First call
			}

			if(profile.enableNewAnnotationsBackend) { // Branch for openBIS 20.X
	            require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/id/SampleIdentifier", "as/dto/sample/update/SampleUpdate" ],
                    function(SamplePermId, SampleIdentifier, SampleUpdate) {
                        var sampleUpdate = new SampleUpdate();
                        sampleUpdate.setSampleId(new SamplePermId(permId));
                        if(parentsAnnotationsState) {
                            // Add annotations
                            for(var parentPermId in parentsAnnotationsState) {
                                var parentAnnotation = parentsAnnotationsState[parentPermId];
                                var parentIdentifier = parentAnnotation["identifier"]; // When creating new parents, annotations should be added by identifier, permIds are not easily obtainable
                                delete parentAnnotation["identifier"];
                                delete parentAnnotation["sampleType"];
                                for(var annotationKey in parentAnnotation) {
                                    sampleUpdate.relationship(new SampleIdentifier(parentIdentifier)).addParentAnnotation(annotationKey, parentAnnotation[annotationKey]);
                                }
                            }
                            // Update annotations
                            // Adding an existing annotation, overrides the old one, updating it.
                            // Remove annotations
                            // Adding an emtpy string on exiting annotation, effectively deletes the value.
                        }
                        if(
                            (childrenAnnotationsState && !isCopyWithNewCode) // Standard Sample Case
                            ||
                            (childrenAnnotationsState && isCopyWithNewCode && copyChildrenOnCopy) // Copy Children Case
                        ) {
                            // Add annotations
                            for(var childPermId in childrenAnnotationsState) {
                                var childAnnotation = childrenAnnotationsState[childPermId];
                                var childIdentifier = childAnnotation["identifier"]; // When creating new children (copy function), annotations should be added by identifier, permIds are not easily obtainable
                                if(isCopyWithNewCode) {
                                    // The copied children identifier follow the pattern /<PARENT_SPACE>/<PARENT_PROJECT>/<COPYED_SAMPLE_CODE>_<ORIGINAL_CHILDREN_CODE>
                                    var originalSampleIdentifier = _this._sampleFormModel.sample.identifier;
                                    var parentSampleSpaceCode = IdentifierUtil.getSpaceCodeFromIdentifier(originalSampleIdentifier);
                                    var parentSampleProjectCode = IdentifierUtil.getProjectCodeFromSampleIdentifier(originalSampleIdentifier);
                                    var copiedParentSampleCode = isCopyWithNewCode;
                                    var childrenToCopyCode = IdentifierUtil.getCodeFromIdentifier(childIdentifier);
                                    childIdentifier = IdentifierUtil.getSampleIdentifier(parentSampleSpaceCode, parentSampleProjectCode, copiedParentSampleCode + "_" + childrenToCopyCode);
                                }
                                delete childAnnotation["identifier"];
                                delete childAnnotation["sampleType"];
                                for(var annotationKey in childAnnotation) {
                                    sampleUpdate.relationship(new SampleIdentifier(childIdentifier)).addChildAnnotation(annotationKey, childAnnotation[annotationKey]);
                                }
                            }
                            // Update annotations
                            // Adding an existing annotation, overrides the old one, updating it.
                            // Remove annotations
                            // Adding an emtpy string on exiting annotation, effectively deletes the value.
                        }
                        mainController.openbisV3.updateSamples([sampleUpdate]).done(function() {
                            if(samplesToDelete) {
                                mainController.serverFacade.trashStorageSamplesWithoutParents(samplesToDelete,
                                "Deleted to trashcan from eln sample form " + _this._sampleFormModel.sample.identifier,
                                function(response) {
                                    Util.showSuccess(message, callbackOk);
                                });
                            } else {
                                Util.showSuccess(message, callbackOk);
                                _this._sampleFormModel.isFormDirty = false;
                            }
                        }).fail(function(result) {
                            Util.showError("Failed to save annotations: " + console.log(JSON.stringify(result)), function() {Util.unblockUI();});
                        });
                });
			} else { // Branch for openBIS 19.X
                if(samplesToDelete) {
                    mainController.serverFacade.trashStorageSamplesWithoutParents(samplesToDelete,
                        "Deleted to trashcan from eln sample form " + _this._sampleFormModel.sample.identifier,
                        function(response) {
                            Util.showSuccess(message, callbackOk);
                    });
                } else {
                    Util.showSuccess(message, callbackOk);
                    _this._sampleFormModel.isFormDirty = false;
                }
			}
			
		} else { //This should never happen
			Util.showError("Unknown Error.", function() {Util.unblockUI();});
		}
	}

	this.getAnnotationsState = function(type) {
        // Used by openBIS 19.X : Check if annotations can't be saved
        if(!profile.enableNewAnnotationsBackend) {
            var isStateFieldAvailable = false;
            if(this._sampleFormModel.sample) {
                var availableFields = profile.getAllPropertiCodesForTypeCode(this._sampleFormModel.sample.sampleTypeCode);
                var pos = $.inArray("$ANNOTATIONS_STATE", availableFields);
                isStateFieldAvailable = (pos !== -1);
            }
            if(!isStateFieldAvailable && this.sampleTypeHints && this.sampleTypeHints.length !== 0) { //Indicates annotations are needed
                Util.showError("You need a property with code ANNOTATIONS_STATE on this entity to store the state of the annotations.");
                return;
            }
        }

        var typeAnnotations = {};
        if(this._sampleFormModel.mode === FormMode.CREATE) {
            // Nothing to load
        } else {
            typeAnnotations = FormUtil.getAnnotationsFromSample(this._sampleFormModel.v3_sample, type);
        }
        return typeAnnotations;
	}
}