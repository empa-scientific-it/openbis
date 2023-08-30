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
	
	this.init = function(views, loadFromTemplate) {
		// Loading datasets
		var _this = this;
		_this._sampleFormModel.views = views;

        mainController.serverFacade.getSampleType(sample.sampleTypeCode, function(sampleType) {
        _this._sampleFormModel.sampleType = sampleType;
		if(mode !== FormMode.CREATE) {
			require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/id/SampleIdentifier",
                "as/dto/dataset/id/DataSetPermId", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
                function(SamplePermId, SampleIdentifier, DataSetPermId, SampleFetchOptions) {
					var id = new SamplePermId(sample.permId);
					var fetchOptions = new SampleFetchOptions();
					fetchOptions.withSpace();
					fetchOptions.withProject();
					fetchOptions.withExperiment();
					fetchOptions.withProperties();
					fetchOptions.withParents();
					fetchOptions.withChildren();
                    fetchOptions.withDataSets().withType();
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
                            var dummyDataSetId = new DataSetPermId(IdentifierUtil.createDummyDataSetIdentifierFromExperimentIdentifier(expeId));
                        }

						mainController.openbisV3.getRights([ id, dummySampleId, dummyDataSetId ], null).done(function(rightsByIds) {
							_this._sampleFormModel.rights = rightsByIds[id];

							if(dummySampleId) {
							    _this._sampleFormModel.sampleRights = rightsByIds[dummySampleId];
                                _this._sampleFormModel.dataSetRights = rightsByIds[dummyDataSetId];
							} else {
							     _this._sampleFormModel.sampleRights = {};
							     _this._sampleFormModel.sampleRights.rights = [];
                                 _this._sampleFormModel.dataSetRights = {};
                                 _this._sampleFormModel.dataSetRights.rights = [];
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
		});
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
		    if (!sample.code) {
		        Util.showUserError("Code is undefined.");
		        return;
		    }
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
						storagePosition.properties[storagePropertyGroup.userProperty] = mainController.serverFacade.getUserId();
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
			
            if(newChangesToDo) {
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
                    "changesToDo" : changesToDo,
                    //Callback parameters
                    "isCopyWithNewCode": isCopyWithNewCode,
                    "samplesToDelete": samplesToDelete,
                    "parentsAnnotationsState": parentsAnnotationsState,
                    "childrenAnnotationsState": childrenAnnotationsState
			};
			
			//
			// Copy override - This part modifies what is done for a create/update and adds a couple of extra parameters needed to copy to the bench correctly
			//
			if(isCopyWithNewCode) {
				parameters["method"] = "copySample";
				parameters["sampleCode"] = isCopyWithNewCode;
				parameters["sampleCodeOrig"] = sampleCode;
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
			
            _this._createUpdateSample(parameters);
		}
		
		profile.sampleFormOnSubmit(sample, continueSampleCreation);
		return false;
	}
	
	this._createUpdateCopySampleCallbackOld = function(_this, isCopyWithNewCode, response, samplesToDelete, parentsAnnotationsState, childrenAnnotationsState, copyChildrenOnCopy) {
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
            _this._createUpdateCopySampleCallback(_this, isCopyWithNewCode, permId, samplesToDelete, parentsAnnotationsState, childrenAnnotationsState, copyChildrenOnCopy)
        } else { //This should never happen
            Util.showError("Unknown Error.", function() {Util.unblockUI();});           
        }
	}
	
    this._createUpdateSample = function(parameters) {
        var _this = this;
        var samplesToGet = parameters["sampleChildrenNew"].map(child => child.identifier);
        if (parameters["copyChildrenOnCopy"] && parameters["copyChildrenOnCopy"] != false) {
            samplesToGet = samplesToGet.concat(parameters["sampleChildren"]);
        }
        if (samplesToGet.length > 0) {
            require(["as/dto/sample/id/SampleIdentifier", "as/dto/sample/fetchoptions/SampleFetchOptions"], 
                    function(SampleIdentifier, SampleFetchOptions) {
                var sampleIds = samplesToGet.map(id => new SampleIdentifier(id));
                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withType();
                fetchOptions.withExperiment();
                fetchOptions.withProperties();
                mainController.openbisV3.getSamples(sampleIds, fetchOptions).done(function(samples) {
                    var existingSamples = {}
                    for (id in samples) {
                        existingSamples[id] = samples[id];
                    }
                    _this._createUpdateSampleStepPerformCreationsAndUpdates(parameters, existingSamples);
                }).fail(function(result) {
                    Util.showFailedServerCallError(result);
                });
            });
        } else {
            this._createUpdateSampleStepPerformCreationsAndUpdates(parameters, {});
        }
        
    }

    this._createUpdateSampleStepPerformCreationsAndUpdates = function(parameters, existingSamples) {
        var _this = this;
        require([ "as/dto/operation/SynchronousOperationExecutionOptions",
                  "as/dto/sample/create/CreateSamplesOperation", "as/dto/sample/create/SampleCreation", 
                  "as/dto/sample/update/UpdateSamplesOperation", "as/dto/sample/update/SampleUpdate", 
                  "as/dto/entitytype/id/EntityTypePermId", 
                  "as/dto/entitytype/EntityKind", "as/dto/space/id/SpacePermId", 
                  "as/dto/project/id/ProjectIdentifier", "as/dto/experiment/id/ExperimentIdentifier",
                  "as/dto/sample/id/SampleIdentifier", "as/dto/common/id/CreationId"],
                  function(SynchronousOperationExecutionOptions, CreateSamplesOperation, SampleCreation, 
                          UpdateSamplesOperation, SampleUpdate,
                          EntityTypePermId, EntityKind, 
                          SpacePermId, ProjectIdentifier, ExperimentIdentifier, SampleIdentifier, CreationId) {
            // Define helper functions
            var createSampleCreation = function(parameters) {
                var creation = new SampleCreation();
                setBasics(creation, parameters);
                creation.setTypeId(new EntityTypePermId(parameters["sampleType"], EntityKind.SAMPLE));
                var sampleCode = parameters["sampleCode"]
                if (sampleCode) {
                    creation.setCode(sampleCode);
                }
                return creation;
            }
            var setBasics = function(object, parameters) {
                var space = parameters["sampleSpace"];
                object.setSpaceId(new SpacePermId(space));
                var sampleIdentifier = "/" + space;
                var project = parameters["sampleProject"];
                if (project != null) {
                    object.setProjectId(new ProjectIdentifier("/" + space + "/" + project));
                    if(IdentifierUtil.isProjectSamplesEnabled) {
                        sampleIdentifier += "/" + project;
                    }
                    var experiment = parameters["sampleExperiment"]
                    if (experiment != null) {
                        object.setExperimentId(new ExperimentIdentifier("/" + space + "/" + project + "/" + experiment));
                    }
                }
                if (object.setSampleId) {
                    object.setSampleId(new SampleIdentifier(sampleIdentifier + "/" + parameters["sampleCode"]));
                }
                var sampleProperties = parameters["sampleProperties"];
                var properties = {};
                Object.keys(sampleProperties).forEach(function(key) {
                    var sampleProperty = sampleProperties[key];
                    if (sampleProperty == "") {
                        sampleProperty = null;
                    }
                    properties[key] = sampleProperty;
                });
                object.setProperties(properties);
            }
            var createRelatedSampleCreation = function(definition) {
                var creation = new SampleCreation();
                creation.setTypeId(new EntityTypePermId(definition["sampleTypeCode"], EntityKind.SAMPLE));
                var splitted = definition["identifier"].split("/");
                creation.setSpaceId(new SpacePermId(splitted[1]));
                if (splitted.length == 4) {
                    creation.setProjectId(new ProjectIdentifier("/" + splitted[1] + "/" + splitted[2]))
                }
                var experimentIdentifier = definition["experimentIdentifier"];
                if (experimentIdentifier) {
                    creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
                }
                var experimentIdentifier = definition["experimentIdentifierOrNull"];
                if (experimentIdentifier) {
                    creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
                }
                creation.setCode(definition["code"]);
                creation.setProperties(definition["properties"]);
                creation.setCreationId(new CreationId(definition[["identifier"]]));
                return creation;
            };
            var getParents = function() {
                var parents = null;
                var sampleParents = parameters["sampleParents"];
                if (sampleParents) {
                    parents = sampleParents.map(p => new SampleIdentifier(p));
                }
                var sampleParentsNew = parameters["sampleParentsNew"];
                if (sampleParentsNew) {
                    sampleParentsNew.forEach(function(newSampleParent) {
                        var identifier = newSampleParent["identifier"];
                        var parentCreation = createRelatedSampleCreation(newSampleParent);
                        sampleCreations.push(parentCreation);
                        if (!parents) {
                            parents = [];
                        }
                        parents.push(parentCreation.getCreationId());
                        var parentsIdentifiers = newSampleParent["parentsIdentifiers"];
                        parentCreation.setParentIds(parentsIdentifiers.map(p => new SampleIdentifier(p)));
                    });
                }
                return parents;
            };
            var getChildren = function() {
                var children = [];
                sampleChildrenNewIdentifiers = [];
                var sampleChildrenNew = parameters["sampleChildrenNew"];
                if (sampleChildrenNew) {
                    sampleChildrenNew.forEach(function(newSampleChild) {
                        var identifier = newSampleChild["identifier"];
                        sampleChildrenNewIdentifiers.push(identifier);
                        if (!existingSamples[identifier]) {
                            var childCreation = createRelatedSampleCreation(newSampleChild);
                            sampleCreations.push(childCreation);
                            children.push(childCreation.getCreationId());
                        } else {
                            children.push(new SampleIdentifier(identifier));
                        }
                    });
                }
                var sampleChildrenAdded = parameters["sampleChildrenAdded"];
                if (sampleChildrenAdded) {
                    sampleChildrenAdded.forEach(function(sampleChildIdentifier) {
                        if (sampleChildrenNewIdentifiers.includes(sampleChildIdentifier) == false) {
                            children.push(new SampleIdentifier(sampleChildIdentifier));
                        }
                    });
                }
                return children;
            };
            // end of helper functions
            var sampleCreations = [];
            var sampleUpdates = [];
            var method = parameters["method"];
            if (method === "insertSample") {
                var creation = createSampleCreation(parameters);
                sampleCreations.push(creation);
                var parents = getParents();
                if (parents) {
                    creation.setParentIds(parents);
                }
                creation.setChildIds(getChildren());
                // End of 'insertSample' section
            } else if (method === "updateSample") {
                var update = new SampleUpdate();
                sampleUpdates.push(update);
                setBasics(update, parameters);
                var parents = getParents();
                if (parents) {
                    update.getParentIds().set(parents);
                }
                var children = getChildren();
                if (children.length > 0) {
                    update.getChildIds().add(children);
                }
                var sampleChildrenRemoved = parameters["sampleChildrenRemoved"];
                if (sampleChildrenRemoved) {
                    update.getChildIds().remove(sampleChildrenRemoved.map(c => new SampleIdentifier(c)));
                }
                // End of 'updateSample' section
            } else if (method === "copySample") {
                var creation = createSampleCreation(parameters);
                sampleCreations.push(creation);
                var parents = getParents();
                if (parents) {
                    creation.setParentIds(parents);
                }
                var creationId = new CreationId(":::copy:::");
                creation.setCreationId(creationId);
                var sampleChildren = parameters["sampleChildren"];
                var copyChildrenOnCopy = parameters["copyChildrenOnCopy"];
                if (sampleChildren && copyChildrenOnCopy != false) {
                    sampleChildren.forEach(function(sampleId) {
                        var child = existingSamples[sampleId];
                        var copyChildCode = parameters["sampleCode"];
                        var index = child.getCode().indexOf("_");
                        if (index >= 0) {
                            copyChildCode += child.getCode().substring(index);
                        } else {
                            copyChildCode += "_" + child.getCode();
                        }
                        var copyChildParameters = {};
                        copyChildParameters["sampleCode"] = copyChildCode;
                        copyChildParameters["sampleType"] = child.getType().getPermId().getPermId();
                        copyChildParameters["sampleProperties"] = child.getProperties();
                        copyChildParameters["sampleSpace"] = parameters["sampleSpace"];
                        copyChildParameters["sampleProject"] = parameters["sampleProject"];
                        copyChildParameters["sampleExperiment"] = parameters["sampleExperiment"];
                        var copyChildCreation = createSampleCreation(copyChildParameters);
                        sampleCreations.push(copyChildCreation);
                        copyChildCreation.setParentIds([creationId]);
                    });
                }
                // End of 'copySample' section
            }
            parameters["sampleChildrenNew"].forEach(function(newSampleChild) {
                var identifier = newSampleChild["identifier"];
                if (existingSamples[identifier]) {
                    var update = new SampleUpdate();
                    sampleUpdates.push(update);
                    update.setSampleId(new SampleIdentifier(identifier));
                    update.setProperties(newSampleChild["properties"]);
                }
            });
            parameters["changesToDo"].forEach(function(change) {
                var update = new SampleUpdate();
                sampleUpdates.push(update);
                update.setSampleId(new SampleIdentifier(change["identifier"]));
                update.setProperties(change["properties"]);
            });

            var operations = [];
            if (sampleCreations.length > 0) {
                operations.push(new CreateSamplesOperation(sampleCreations));
            }
            if (sampleUpdates.length > 0) {
                operations.push(new UpdateSamplesOperation(sampleUpdates));
            }
            var operationOptions = new SynchronousOperationExecutionOptions();
            mainController.openbisV3.executeOperations(operations, operationOptions).done(function(result) {
                var permId = null;
                result.results.forEach(function(operationResult) {
                    if (operationResult["@type"] === "as.dto.sample.create.CreateSamplesOperationResult"
                            && (method == "insertSample" || method == "copySample")) {
                        permId = operationResult.getObjectIds()[0].getPermId();
                    }
                    if (operationResult["@type"] === "as.dto.sample.update.UpdateSamplesOperationResult"
                            && method == "updateSample") {
                        permId = operationResult.getObjectIds()[0].getPermId();
                    }
                });
                _this._createUpdateCopySampleCallback(_this, parameters["isCopyWithNewCode"], permId, 
                        parameters["samplesToDelete"], parameters["parentsAnnotationsState"], 
                        parameters["childrenAnnotationsState"], parameters["copyChildrenOnCopy"]);
            }).fail(function(result) {
                Util.showFailedServerCallError(result);
            });
        });
    }
    
    this._createUpdateCopySampleCallback = function(_this, isCopyWithNewCode, permId, samplesToDelete, parentsAnnotationsState, childrenAnnotationsState, copyChildrenOnCopy) {
        
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
                } else if(_this._sampleFormModel.mode === FormMode.EDIT || isCopyWithNewCode) {
                    mainController.sideMenu.refreshNodeParentByPermId("SAMPLE", _this._sampleFormModel.sample.permId);
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
                            if(!Util.isMapEmpty(childAnnotation)) { // Storage positions don't have annotations
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