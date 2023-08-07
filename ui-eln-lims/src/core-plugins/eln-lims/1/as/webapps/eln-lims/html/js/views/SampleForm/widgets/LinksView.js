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

function LinksView(linksController, linksModel) {
	var linksController = linksController;
	var linksModel = linksModel;
	var linksView = this;
	
	var sampleGridContainerByType = {};
	
	var $samplePicker = $("<div>");
	var $savedContainer = null;
	
	var dataGrids = [];
	
	//
	// External API
	//
	linksView.initContainerForType = function(sampleTypeCode, samples, sampleTypeLabel) {
		var $dataGridContainer = sampleGridContainerByType[sampleTypeCode];
		var samplesOnGrid = linksModel.samplesByType[sampleTypeCode];
		
		//Create Model if missing
		if(!samplesOnGrid) {
			samplesOnGrid = [];
		}
		 //This should happen only during the initalization
		if(samples) {
			samplesOnGrid = samplesOnGrid.concat(samples);
		}
		
		linksModel.samplesByType[sampleTypeCode] = samplesOnGrid;
		
		//Create Layout
		if(!$dataGridContainer) { //Create if is not there yet
			//Layout
			var $sampleTableContainer = $("<div>");
			var $samplePickerContainer = $("<div>");
			
			if(sampleTypeCode) {
				var sampleTableContainerLabel = (sampleTypeLabel)?sampleTypeLabel:sampleTypeCode;
				$sampleTableContainer.append($("<div>").append(sampleTableContainerLabel + ":")
						.append("&nbsp;")
						.append(linksView.getAddBtn($samplePickerContainer, sampleTypeCode, sampleTableContainerLabel))
						.append("&nbsp;")
						.append(linksView.getAddPasteAnyBtn($samplePickerContainer, sampleTypeCode, sampleTableContainerLabel))
						.css("margin","5px"));
			}
			
			$sampleTableContainer.append($samplePickerContainer);
			$dataGridContainer = $("<div>");
			$sampleTableContainer.append($dataGridContainer);
			
			sampleGridContainerByType[sampleTypeCode] = $dataGridContainer;
			
			$savedContainer.append($sampleTableContainer);
		}
	}
	
	this.updateSample = function(sample, isAdd, isInit) {
		var containerCode = null;
		
		if(!linksModel.isDisabled) {
			var sampleTypeCode = null;
			if(isInit) {
				sampleTypeCode = sample[0].sampleTypeCode;
			} else {
				sampleTypeCode = sample.sampleTypeCode;
			}
			containerCode = sampleTypeCode;
		}
		
		linksView.initContainerForType(containerCode, (isInit)?sample:null);
		
		var $dataGridContainer = sampleGridContainerByType[containerCode];
		
		var samplesOnGrid = linksModel.samplesByType[containerCode];
		
		//Check if the sample is already added
		var foundAtIndex = -1;
		if(!isInit) {
			for(var sIdx = 0; sIdx < samplesOnGrid.length; sIdx++) {
				if(samplesOnGrid[sIdx].permId === sample.permId) {
					foundAtIndex = sIdx;
					if(isAdd) {
						Util.showUserError(ELNDictionary.Sample + " " + sample.code + " already present, it will not be added again.");
						return;
					} else {
						linksModel.samplesRemoved.push(sample.identifier);
						break;
					}
				}
			}
		}
		
		if(isAdd && !isInit) {
			linksModel.samplesAdded.push(sample.identifier);
		}
		
		//Render Grid
		$dataGridContainer.empty();
		
		if(!isInit) {
			if(isAdd) {
				samplesOnGrid.push(sample);
			} else {
				samplesOnGrid.splice(foundAtIndex, 1);
			}
		}
		
		var customAnnotationColumnsByType = {};
		for(var sIdx = 0; sIdx < samplesOnGrid.length; sIdx++) {
			var sampleOnGrid = samplesOnGrid[sIdx];
			if(!customAnnotationColumnsByType[sampleOnGrid.sampleTypeCode]) {
				var customACols = linksView.getCustomAnnotationColumns(sampleOnGrid.sampleTypeCode);
				customAnnotationColumnsByType[sampleOnGrid.sampleTypeCode] = customACols;
			}
		}
		
		var allCustomAnnotations = [];
		for(type in customAnnotationColumnsByType) {
			var customACols = customAnnotationColumnsByType[type];
			for(var cIdx = 0; cIdx < customACols.length; cIdx++) {
				var customACol = customACols[cIdx];
				var isFound = false;
				for(aIdx = 0; aIdx < allCustomAnnotations.length; aIdx++) {
					if(allCustomAnnotations[aIdx].property == customACol.property) {
						isFound = true;
					}
				}
				if(!isFound) {
					allCustomAnnotations.push(customACol);
				}
			}
		}
		
		var postFix = null;
		if(containerCode) {
			postFix = "ANNOTATIONS";
		} else {
			containerCode = mainController.currentView._sampleFormModel.sample.sampleTypeCode;
			postFix = "ANNOTATIONS_ALL" + linksModel.title;
		}
		
        var dataGrid = SampleDataGridUtil.getSampleDataGrid(containerCode, samplesOnGrid, null, 
                linksView.getCustomOperationsForGrid(), allCustomAnnotations, postFix, linksModel.isDisabled, 
                false, false, false, false, 100);
		dataGrid.init($dataGridContainer);
		if(isInit) {
		    for(var sIdx = 0; sIdx < sample.length; sIdx++) {
		        linksModel.writeState(sample[sIdx], null, null, false);
            }
		} else {
		    linksModel.writeState(sample, null, null, false);
		}
		dataGrids.push(dataGrid);
	}
	
	this.refresh = function() {
		dataGrids.forEach(function(dataGrid) {
			dataGrid.refresh();
		});
	}
	
	this.repaint = function($container) {
		var $fieldsetOwner = $("<div>");
		var $legend = $("<legend>");
		    $legend.css({
		        'padding-top':'5px',
		        'padding-bottom':'5px'
		    });
		var $fieldset = $("<div>");
		$fieldsetOwner.append($legend).append($fieldset);
		
		$container.empty();
		$container.append($fieldsetOwner);
		$savedContainer = $fieldset;
		
		var addSearchAnyBtn = null;
		var addPasteAnyBtn = null;
		if(linksModel.disableAddAnyType) {
			addSearchAnyBtn = "";
			addPasteAnyBtn = "";
		} else {
			addSearchAnyBtn = linksView.getAddSearchAnyBtn();
			addPasteAnyBtn = linksView.getAddPasteAnyBtn();
		}
		
		$legend.append(linksModel.title).append("&nbsp;").append(addSearchAnyBtn).append("&nbsp;").append(addPasteAnyBtn); //.css("margin-top", "20px").css("margin-bottom", "20px");

		if(!linksModel.disableAddAnyType && profile.mainMenu.showBarcodes) {
			$legend.append("&nbsp;").append(linksView.getAddAnyBarcode());
		}

		$fieldset.append($samplePicker);
	}
	
	//
	// Internal API
	//
	
	linksView.showCopyProtocolPopUp = function(callback) {
		Util.blockUINoMessage();
		var component = "<div>"
			component += "<legend>Copy Protocol</legend>";
			component += "<div class='form-group'>";
			component += "<label class='control-label'>Code&nbsp;(*):</label>";
			component += "<div>";
			component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
			component += "</div>";
			component += "<div>";
			component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
			component += "</div>";
			component += "</div>";
			
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'auto'
		};
		
		Util.blockUI(component + "<a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", css);
		
		$("#newSampleCodeForCopy").on("keyup", function(event) {
			$(this).val($(this).val().toUpperCase());
		});
		
		$("#copyAccept").on("click", function(event) {
			var code = $("#newSampleCodeForCopy").val();
			if(code) {
				callback(code);
			} else {
				Util.showUserError("Code missing.");
			}
		});
		
		$("#copyCancel").on("click", function(event) { 
			Util.unblockUI();
		});
	}
	
	linksView.getCustomAnnotationColumns = function(sampleTypeCode) {
		var annotationDefinitions = linksModel.sampleTypeHints;
		
		var extraColumns = [];
		if(annotationDefinitions) {
			for(var aIdx = 0; aIdx < annotationDefinitions.length; aIdx++) {
				var annotationDefinition = annotationDefinitions[aIdx];
				if(annotationDefinition.TYPE === sampleTypeCode) {
					var annotationProperties = annotationDefinition.ANNOTATION_PROPERTIES;
					for(var pIdx = 0; pIdx < annotationProperties.length; pIdx++) {
						var annotationProperty = annotationProperties[pIdx];
						var propertyType = profile.getPropertyType(annotationProperty.TYPE);
						if(!propertyType) {
							Util.showError("Missing property found in configuration, contact support: " + annotationProperty.TYPE, function() {}, true, false, true, false);
							propertyType = {
									code : annotationProperty.TYPE,
									label : annotationProperty.TYPE
							}
						}
						extraColumns.push(linksView.getCustomField(propertyType));
					}
				}
			}
		}
		return extraColumns;
	}
	
	linksView.getCustomField = function(propertyType) {
		var propertyAnnotationCode = "$ANNOTATION::" + propertyType.code;
		return {
			label : propertyType.label,
			property : propertyAnnotationCode,
			isExportable: true,
			showByDefault: true,
			filterable: false,
			sortable : false,
			render : function(data) {
				var sample = data["$object"];
				var currentValue = linksModel.readState(sample.permId, propertyType.code);
				
				if(linksModel.isDisabled) {
					if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
							currentValue = FormUtil.getVocabularyLabelForTermCode(propertyType, currentValue);
					}
					return currentValue;
				} else {
					var $field = FormUtil.getFieldForPropertyType(propertyType);
					if (propertyType.dataType === "MULTILINE_VARCHAR") {
						$field.css({
							"height" : "100%",
							"width" : "100%"
						});
					}
					if(currentValue) {
						FormUtil.setFieldValue(propertyType, $field, currentValue);
					}

					var id = propertyType.label.split(" ").join("-").toLowerCase();
					id = id + "-" + sample.code.toLowerCase();
					$field.attr("id", id); //Fix for current summernote behaviour
					$field.change(function() {
						var $field = $(this);
						propertyTypeValue = FormUtil.getFieldValue(propertyType, $field);
						linksModel.writeState(sample, propertyType.code, propertyTypeValue, false);
					});
					return $field;
				}
				
			}
		};
	}
	
	linksView.getCustomOperationsForGrid = function() {
		return {
			label : "Operations",
			property : 'operations',
			isExportable: false,
			showByDefault: true,
			filterable: false,
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var codeId = data.code.toLowerCase() + "-operations-column-id";

				var $dropDownMenu = $("<span>", { class : 'dropdown table-options-dropdown' });
				var $caret = $("<a>", { 'href' : '#',
				                        'data-toggle' : 'dropdown',
				                        class : 'dropdown-toggle btn btn-default',
				                        'id' : codeId}).append("Operations ").append($("<b>", { class : 'caret' }));
				var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
				$dropDownMenu.append($caret);
				$dropDownMenu.append($list);
				
				var stopEventsBuble = function(event) {
						event.stopPropagation();
						event.preventDefault();
						$caret.dropdown('toggle');
				};
				$dropDownMenu.dropdown();
				$dropDownMenu.click(stopEventsBuble);
				
				if(profile.isSampleTypeProtocol(data["$object"].sampleTypeCode)) {
				    var id = codeId + "-use-as-template";
					var $copyAndLink = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'id' : id, 'title' : 'Copy to Experiment'}).append("Copy to Experiment"));
					$copyAndLink.click(function(e) {
						stopEventsBuble(e);
						var copyAndLink = function(code) {
							var newSampleIdentifier = IdentifierUtil.getSampleIdentifier(mainController.currentView._sampleFormModel.sample.spaceCode, 
																			   mainController.currentView._sampleFormModel.sample.projectCode,
																			   code);
							Util.blockUI();
							mainController.serverFacade.customELNApi({
								"method" : "copyAndLinkAsParent",
								"newSampleIdentifier" : newSampleIdentifier,
								"sampleIdentifierToCopyAndLinkAsParent" : data["$object"].identifier,
								"experimentIdentifierToAssignToCopy" : mainController.currentView._sampleFormModel.sample.experimentIdentifierOrNull
							}, function(error, result) {
								if(error) {
									Util.showError(error);
								} else {
									var searchUntilFound = null;
								    searchUntilFound = function() {
										mainController.serverFacade.searchWithIdentifiers([newSampleIdentifier], function(results) {
											if(results.length > 0) {
												linksView.updateSample(data["$object"], false);
												linksView.updateSample(results[0], true);
												Util.unblockUI();
											} else {
												searchUntilFound();
											}
										});
									};
									
									searchUntilFound();
								}
							});
						};
						
						linksView.showCopyProtocolPopUp(copyAndLink);
					});
					$list.append($copyAndLink);
				}
				
				var $delete = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Remove'}).append("Remove"));
				
				var getDeleteFunc = function(sample) {
					return function(e) {
						stopEventsBuble(e);
						linksView.updateSample(sample, false);
					};
				}
				
				$delete.click(getDeleteFunc(data["$object"]));
				$list.append($delete);
				
				if(linksModel.isDisabled) {
					return "";
				} else {
					return $dropDownMenu;
				}
			}
		}
	}
	
	linksView.showSamplePicker = function($container, sampleTypeCode) {
		$container.empty().show();
		$container.css({
			"margin" : "5px",
			"padding" : "5px",
			"background-color" : "rgb(248, 248, 248)",
			"border-radius" : "4px"
		});

		//Grid Layout
		var $gridContainer = $("<div>");
		$container.append($gridContainer);

        var $closeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
            $container.empty().hide();
        });
        var $closeBtnContainer = $("<div>").append($closeBtn).css({"text-align" : "right", "margin-bottom" : "5px"});
        $gridContainer.append($closeBtnContainer);

        var $searchDropdownContainer = $("<div>");
        $gridContainer.append($searchDropdownContainer);

        var typeId = sampleTypeCode.toLowerCase()

        // Search Dropdown
		var searchDropdown = new AdvancedEntitySearchDropdown(true, true, "Code or Name of the Object", false, true, false, false, false);
        searchDropdown.onChange(function(selected){
            for(var sIdx = 0; sIdx < selected.length; sIdx++) {
                linksController.addSample({ identifier : selected[sIdx].identifier.identifier });
            }
            if (selected.length > 0 ) {
                console.log(selected.length);
                searchDropdown.clearSelection();
            }
        });
		searchDropdown.setGetSelectsSamplesCriteria(function() {
                var advancedSampleSearchCriteria = {
                    entityKind : "SAMPLE",
                    logicalOperator : "OR",
                    rules : {},
                    subCriteria : {
                        "1": {
                                        entityKind : "SAMPLE",
                                        logicalOperator : "AND",
                                        rules : {
                                            "1-1": { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeCode },
                                            "1-2": { type: "Property/Attribute", 	name: "ATTR.CODE", operator : "thatContains", 		value: searchDropdown.getParams().data.q }
                                        }
                        },
                        "2": {
                                        entityKind : "SAMPLE",
                                        logicalOperator : "AND",
                                        rules : {
                                            "2-1": { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeCode },
                                            "2-2": { type: "Property/Attribute", 	name: "PROP.$NAME", operator : "thatContainsString", value: searchDropdown.getParams().data.q }
                                        }
                        }
                    }
                }
        		if(sampleTypeCode === "REQUEST") {
        			advancedSampleSearchCriteria.subCriteria["1"].rules["1-3"] = { type : "Property/Attribute", name : "PROP.$ORDERING.ORDER_STATUS", operator : "thatEqualsString", value : "NOT_YET_ORDERED" };
        			advancedSampleSearchCriteria.subCriteria["2"].rules["2-3"] = { type : "Property/Attribute", name : "PROP.$ORDERING.ORDER_STATUS", operator : "thatEqualsString", value : "NOT_YET_ORDERED" };
        		}
                if (["ORGANIZATION_UNIT", "REQUEST", "PRODUCT", "SUPPLIER"].indexOf(sampleTypeCode) >= 0) {
                    var spaceCodePrefix = mainController.currentView._sampleFormModel.sample.spaceCode.split("_")[0];
                    var rule = { type : "Property/Attribute", name : "ATTR.SPACE_PREFIX", value : spaceCodePrefix };
                    advancedSampleSearchCriteria.subCriteria["1"].rules["1-4"] = rule;
                    advancedSampleSearchCriteria.subCriteria["2"].rules["2-4"] = rule;
                }
                return advancedSampleSearchCriteria;
            });
		searchDropdown.init($searchDropdownContainer);
	}

	linksView.showSamplePaster = function($container, sampleTypeCode) {
		$container.empty().show();
		$container.css({
			"margin" : "5px",
			"padding" : "5px",
			"background-color" : "rgb(248, 248, 248)",
			"border-radius" : "4px"
		});

		//Grid Layout
		var $gridContainer = $("<div>");
		$container.append($gridContainer);

        var $closeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
            $container.empty().hide();
        });
        var $closeBtnContainer = $("<div>").append($closeBtn).css({"text-align" : "right", "margin-bottom" : "5px"});
        $gridContainer.append($closeBtnContainer);

        var $pasteContainer = $("<div>");
        $gridContainer.append($pasteContainer);

        var $textArea = FormUtil._getTextBox(null, "Object identifiers or codes separated by space or comma", false);
        $textArea.css( { 'width' : '100%', "height" : "20%", "min-height" : "100px"});
        $pasteContainer.append($textArea);

        var $addObjectsBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
            var maybeIdentifiersOrCodes = null;
            if($textArea.val().indexOf(",") > -1) {
                maybeIdentifiersOrCodes = $textArea.val().trim().split(",");
            } else {
                maybeIdentifiersOrCodes = $textArea.val().trim().split(" ");
            }

            var maybeIdentifiers = [];
            var maybeCodes = [];
            var maybePermIds = []
            var permIdPattern = new RegExp('^[0-9]{17}-[0-9]{1,}');
            for(var vIdx = 0; vIdx < maybeIdentifiersOrCodes.length; vIdx++) {
                var maybeBeId = maybeIdentifiersOrCodes[vIdx].trim();
                if(maybeBeId.indexOf("/") > -1) {
                    maybeIdentifiers.push(maybeBeId);
                } else {
                    if(permIdPattern.test(maybeBeId)) {
                        maybePermIds.push(maybeBeId);
                    } else {
                        maybeCodes.push(maybeBeId);
                    }
                }
            }

            var requestedObjects = (maybeIdentifiers.length + maybePermIds.length + maybeCodes.length);
            if(requestedObjects == 0) {
                Util.showError("Nothing to paste was found.");
                return;
            }
            Util.blockUI();

            var finalResults = [];
            var added = 0;
            var incorrectType = 0;
            // Search with identifiers
            mainController.serverFacade.searchWithIdentifiers(maybeIdentifiers, function(identifierResults) {
                for(var sIdx = 0; sIdx < identifierResults.length; sIdx++) {
                    if(sampleTypeCode === undefined || (identifierResults[sIdx].sampleTypeCode === sampleTypeCode)) {
                        finalResults.push(identifierResults[sIdx]);
                        added++;
                    } else {
                        incorrectType++;
                    }
                }
                //Search with permIds
                mainController.serverFacade.searchWithSamplePermIds(maybePermIds, function(permIdResults) {
                    for(var sIdx = 0; sIdx < permIdResults.length; sIdx++) {
                        if(sampleTypeCode === undefined || (permIdResults[sIdx].sampleTypeCode === sampleTypeCode)) {
                            finalResults.push(permIdResults[sIdx]);
                            added++;
                        } else {
                            incorrectType++;
                        }
                    }
                    // Search with codes
                    mainController.serverFacade.searchWithCodes(maybeCodes, function(codeResults) {
                        for(var sIdx = 0; sIdx < codeResults.length; sIdx++) {
                            if(sampleTypeCode === undefined || (codeResults[sIdx].sampleTypeCode === sampleTypeCode)) {
                                finalResults.push(codeResults[sIdx]);
                                added++;
                            } else {
                                incorrectType++;
                            }
                        }
                        // Summary
                        if(requestedObjects != finalResults.length) {
                            Util.unblockUI();
                            Util.showError("Requested " + requestedObjects + " but found " + finalResults.length + ", check your ids!");
                        } else {
                            for(var sIdx = 0; sIdx < finalResults.length; sIdx++) {
                                linksView.updateSample(finalResults[sIdx], true);
                            }
                            Util.unblockUI();
                            var message = "Pasted " + added + " " + ((added === 1)?ELNDictionary.Sample:ELNDictionary.Samples) + "."
                            Util.showInfo(message);
                            $container.empty().hide();
                        }
                    });
                });
           });
        }, "Add");
        $addObjectsBtn.css({"margin-top" : "5px"});
        $pasteContainer.append($addObjectsBtn);
	}

	linksView.getAddBtn = function($container, sampleTypeCode, sampleTableContainerLabel) {
		var enabledFunction = function() {
			linksView.showSamplePicker($container, sampleTypeCode);
		};

		var id = "search-btn-" + sampleTableContainerLabel.toLowerCase().split(" ").join("-");
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-search", (linksModel.isDisabled)?null:enabledFunction, "Search", null, id);
		if(linksModel.isDisabled) {
			return "";
		} else {
			return $addBtn;
		}
	}

	linksView.getAddPasteAnyBtn = function($container, sampleTypeCode, sampleTableContainerLabel) {
    	var enabledFunction = function() {
    	    if(sampleTypeCode) {
                linksView.showSamplePaster($container, sampleTypeCode);
    	    } else {
    	        linksView.showSamplePaster($samplePicker, sampleTypeCode);
    	    }
    	};

        var id = "paste-btn-";
        var label = null;
    	if(sampleTypeCode) {
            id += sampleTableContainerLabel.toLowerCase().split(" ").join("-");
            label = "Paste";
    	} else {
            id += linksModel.title.split(" ").join("-").toLowerCase();
            label = "Paste Any";
    	}

    	var $addBtn = FormUtil.getButtonWithIcon("glyphicon-paste", (linksModel.isDisabled)?null:enabledFunction, label, null, id);
    	if(linksModel.isDisabled) {
    		return "";
    	} else {
    		return $addBtn;
    	}
    }
	
	linksView.getAddSearchAnyBtn = function() {
		var enabledFunction = function() {
			var $sampleTypesDropdown = FormUtil.getSampleTypeDropdown("sampleTypeSelector", true, null, null, linksModel.spaceCode);
			Util.showDropdownAndBlockUI("sampleTypeSelector", $sampleTypesDropdown);
			
			$("#sampleTypeSelector").on("change", function(event) {
				var sampleTypeCode = $(this).val();
				linksView.showSamplePicker($samplePicker, sampleTypeCode);
				Util.unblockUI();
			});
			
			$("#sampleTypeSelectorCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		};
		var id = "plus-btn-" + linksModel.title.split(" ").join("-").toLowerCase() + "-type-selector";
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-search", (linksModel.isDisabled)?null:enabledFunction, "Search Any", null, id);
		
		if(linksModel.isDisabled) {
			return "";
		} else {
			return $addBtn;
		}
	}

	linksView.getAddAnyBarcode = function() {
	    var $addBtn = FormUtil.getButtonWithIcon("glyphicon-barcode", null, null, "Scan Barcode/QR code");
        $addBtn.click(function() {
            BarcodeUtil.readBarcodeMulti("Add Objects", function(objects) {
                for(var oIdx = 0; oIdx < objects.length; oIdx++) {
                    linksController.addSample({
                        identifier : objects[oIdx].identifier.identifier
                    });
                }
            });
        });
        if(linksModel.isDisabled) {
            return "";
        } else {
            return $addBtn;
        }
	}
}