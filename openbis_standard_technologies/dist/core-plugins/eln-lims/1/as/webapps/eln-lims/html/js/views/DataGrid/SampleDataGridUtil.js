var SampleDataGridUtil = new function() {
    this.getSampleDataGrid = function(mandatoryConfigPostKey, samplesOrCriteria, rowClick, customOperations,
            customColumns, optionalConfigPostKey, isOperationsDisabled, isLinksDisabled, isMultiselectable,
            showParentsAndChildren, withExperiment, heightPercentage) {
		var _this = this;
		var isDynamic = samplesOrCriteria.entityKind && samplesOrCriteria.rules;
		
		//Fill Columns model
		var columnsFirst = [];

		columnsFirst.push({
			label : 'Code',
			property : 'code',
			isExportable: false,
			sortable : true,
			render : function(data, grid) {
				var paginationInfo = null;
				if(isDynamic) {
					var indexFound = null;
					for(var idx = 0; idx < grid.lastReceivedData.objects.length; idx++) {
						if(grid.lastReceivedData.objects[idx].permId === data.permId) {
							indexFound = idx + (grid.lastUsedOptions.pageIndex * grid.lastUsedOptions.pageSize);
							break;
						}
					}

					if(indexFound !== null) {
						paginationInfo = {
								pagFunction : _this.getDataListDynamic(samplesOrCriteria, false),
								pagOptions : grid.lastUsedOptions,
								currentIndex : indexFound,
								totalCount : grid.lastReceivedData.totalCount
						}
					}
				}
				return (isLinksDisabled)?data.code:FormUtil.getFormLink(data.code, "Sample", data.permId, paginationInfo);
			},
			filter : function(data, filter) {
				return data.identifier.toLowerCase().indexOf(filter) !== -1;
			},
			sort : function(data1, data2, asc) {
				var value1 = data1.identifier;
				var value2 = data2.identifier;
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(value1, value2);
			}
		});

		columnsFirst.push({
			label : 'Name',
			property : '$NAME',
			isExportable: true,
			sortable : true,
			render : function(data) {
				var nameToUse = "";
                if(data[profile.propertyReplacingCode]) {
                    nameToUse = data[profile.propertyReplacingCode];
                }
				return (isLinksDisabled) ? nameToUse : FormUtil.getFormLink(nameToUse, "Sample", data.permId);
			}
		});

		columnsFirst.push({
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : true,
			render : function(data, grid) {
				var paginationInfo = null;
				if(isDynamic) {
					var indexFound = null;
					for(var idx = 0; idx < grid.lastReceivedData.objects.length; idx++) {
						if(grid.lastReceivedData.objects[idx].permId === data.permId) {
							indexFound = idx + (grid.lastUsedOptions.pageIndex * grid.lastUsedOptions.pageSize);
							break;
						}
					}

					if(indexFound !== null) {
						paginationInfo = {
								pagFunction : _this.getDataListDynamic(samplesOrCriteria, false),
								pagOptions : grid.lastUsedOptions,
								currentIndex : indexFound,
								totalCount : grid.lastReceivedData.totalCount
						}
					}
				}
				return (isLinksDisabled)?data.identifier:FormUtil.getFormLink(data.identifier, "Sample", data.permId, paginationInfo);
			},
			filter : function(data, filter) {
				return data.identifier.toLowerCase().indexOf(filter) !== -1;
			},
			sort : function(data1, data2, asc) {
				var value1 = data1.identifier;
				var value2 = data2.identifier;
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(value1, value2);
			}
		});

		if(customColumns) {
			columnsFirst = columnsFirst.concat(customColumns);
		}

		columnsFirst.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});
		
		var dynamicColumnsFunc = function(samples) {
			var foundPropertyCodes = {};
			var foundSampleTypes = {};
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				var sample = samples[sIdx];
				if(!foundSampleTypes[sample.sampleTypeCode]) {
					foundSampleTypes[sample.sampleTypeCode] = true;
					var propertyCodes = profile.getAllPropertiCodesForTypeCode(sample.sampleTypeCode);
					for(var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
						foundPropertyCodes[propertyCodes[pIdx]] = true;
					}
				}
			}
			
			var propertyColumnsToSort = [];
			for (propertyCode in foundPropertyCodes) {
				var propertiesToSkip = ["$NAME", "$XMLCOMMENTS", "$ANNOTATIONS_STATE"];
				if($.inArray(propertyCode, propertiesToSkip) !== -1) {
					continue;
				}
				var propertyType = profile.getPropertyType(propertyCode);
				if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
					var getVocabularyColumn = function(propertyType) {
						return function() {
							return {
								label : propertyType.label,
								property : propertyType.code,
								isExportable: true,
								sortable : true,
								render : function(data) {
									return FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
								},
								filter : function(data, filter) {
									var value = FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
									return value && value.toLowerCase().indexOf(filter) !== -1;
								},
								sort : function(data1, data2, asc) {
									var value1 = FormUtil.getVocabularyLabelForTermCode(propertyType, data1[propertyType.code]);
									if(!value1) {
										value1 = ""
									};
									var value2 = FormUtil.getVocabularyLabelForTermCode(propertyType, data2[propertyType.code]);
									if(!value2) {
										value2 = ""
									};
									var sortDirection = (asc)? 1 : -1;
									return sortDirection * naturalSort(value1, value2);
								}
							};
						}
					}
					
					var newVocabularyColumnFunc = getVocabularyColumn(propertyType);
					propertyColumnsToSort.push(newVocabularyColumnFunc());
				} else if (propertyType.dataType === "HYPERLINK") {
					var getHyperlinkColumn = function(propertyType) {
						return {
							label : propertyType.label,
							property : propertyType.code,
							isExportable: true,
							sortable : true,
							render : function(data) {
								return FormUtil.asHyperlink(data[propertyType.code]);
							}
						};
					}
					propertyColumnsToSort.push(getHyperlinkColumn(propertyType));
				} else {			
					propertyColumnsToSort.push({
						label : propertyType.label,
						property : propertyType.code,
						isExportable: true,
						sortable : true
					});
				}
			}

			propertyColumnsToSort.sort(function(propertyA, propertyB) {
				return propertyA.label.localeCompare(propertyB.label);
			});
			return propertyColumnsToSort;
		}

		var columnsLast = [];
		columnsLast.push({
        			label : '---------------',
        			property : null,
        			isExportable: false,
        			sortable : false
        });
		columnsLast.push({
			label : 'Type',
			property : 'sampleTypeCode',
			isExportable: false,
			sortable : true,
		    render : function(data, grid) {
                return Util.getDisplayNameFromCode(data.sampleTypeCode);
            },
		});

		columnsLast.push({
			label : 'Space',
			property : 'default_space',
			isExportable: true,
			sortable : false
		});

		if(withExperiment) {
			columnsLast.push({
				label : ELNDictionary.getExperimentDualName(),
				property : 'experiment',
				isExportable: true,
				sortable : false
			});
		}

        if (showParentsAndChildren) {
            columnsLast.push({
                label : 'Parents',
                property : 'parents',
                isExportable: true,
                sortable : false,
                render : function(data, grid) {
                    return _this.renderRelatedSamples(data.parents, isLinksDisabled);
                }
            });

            columnsLast.push({
                label : 'Children',
                property : 'children',
                isExportable: false,
                sortable : false,
                render : function(data, grid) {
                    return _this.renderRelatedSamples(data.children, isLinksDisabled);
                }
            });
        }

		columnsLast.push({
			label : 'Storage',
			property : 'storage',
			isExportable: false,
			sortable : false,
			render : function(data) {
				var storage = $("<span>");
				if(data["$object"].children) {
					var isFirst = true;
					for (var cIdx = 0; cIdx < data['$object'].children.length; cIdx++) {
						if(data['$object'].children[cIdx].sampleTypeCode == "STORAGE_POSITION") {
							var sample = data['$object'].children[cIdx];
							var displayName = Util.getStoragePositionDisplayName(sample);
							if(!isFirst) {
								storage.append(",<br>");
							}
							storage.append(FormUtil.getFormLink(displayName, "Sample", sample.permId));
							isFirst = false;
						}
					}
				}
				return storage;
			}
		});

		columnsLast.push({
			label : 'Preview',
			property : 'preview',
			isExportable: false,
			sortable : false,
			render : function(data) {
				var previewContainer = $("<div>");
				mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [data.permId], function(data) {
					data.result.forEach(function(dataset) {
						var listFilesForDataSetCallback = function(dataFiles) {
							for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
								if(!dataFiles.result[pathIdx].isDirectory) {
									var downloadUrl = profile.allDataStores[0].downloadUrl + '/' + dataset.code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
									var previewImage = $("<img>", { 'src' : downloadUrl, 'class' : 'zoomableImage', 'style' : 'width:100%;' });
									previewImage.click(function(event) {
										Util.showImage(downloadUrl);
										event.stopPropagation();
									});
									previewContainer.append(previewImage);
									break;
								}
							}
						};
						mainController.serverFacade.listFilesForDataSet(dataset.code, "/", true, listFilesForDataSetCallback);
					});
				});
				return previewContainer;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		});

		columnsLast.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});
		
		columnsLast.push({
			label : 'Registrator',
			property : 'registrator',
			isExportable: false,
			sortable : false
		});
		
		columnsLast.push({
			label : 'Registration Date',
			property : 'registrationDate',
			isExportable: false,
			sortable : true
		});
		
		columnsLast.push({
			label : 'Modifier',
			property : 'modifier',
			isExportable: false,
			sortable : false
		});
		
		columnsLast.push({
			label : 'Modification Date',
			property : 'modificationDate',
			isExportable: false,
			sortable : true
		});
		
		if(!isOperationsDisabled && customOperations) {
			columnsLast.push(customOperations);
		} else if(!isOperationsDisabled) {
			columnsLast.push(this.createOperationsColumn());
		}
		
		//Fill data model
		var getDataList = null;
		if(isDynamic) {
			getDataList = SampleDataGridUtil.getDataListDynamic(samplesOrCriteria, withExperiment); //Load on demand model
		} else {
			getDataList = SampleDataGridUtil.getDataList(samplesOrCriteria); //Static model
		}
			
		//Create and return a data grid controller
		var configKey = "SAMPLE_TABLE_" + mandatoryConfigPostKey;
		if(optionalConfigPostKey) {
			configKey += "_" + optionalConfigPostKey;
		}
		
		var dataGridController = new DataGridController(null, columnsFirst, columnsLast, dynamicColumnsFunc, getDataList, rowClick, false, configKey, isMultiselectable, heightPercentage);
		return dataGridController;
	}
	
    this.renderRelatedSamples = function(samples, isLinksDisabled) {
        var output = $("<span>");
        if (samples) {
            var elements = samples.split(", ");
            for (var eIdx = 0; eIdx < elements.length; eIdx++) {
                var element = elements[eIdx];
                var eIdentifier = element.split(" (")[0];
                var eComponent = isLinksDisabled ? element : FormUtil.getFormLink(element, "Sample", eIdentifier, null);
                if(eIdx != 0) {
                    output.append(", ");
                }
                output.append(eComponent);
            }
        }
        return output;
    }
	
	this.getDataListDynamic = function(criteria, withExperiment) {
		return function(callback, options) {
			var callbackForSearch = function(result) {
				var dataList = [];
				
				for(var sIdx = 0; sIdx < result.objects.length; sIdx++) {
					var sample = mainController.serverFacade.getV3SampleAsV1(result.objects[sIdx]);
					
					var registrator = null;
					if(sample.registrationDetails && sample.registrationDetails.userId) {
						registrator = sample.registrationDetails.userId;
					}
					
					var registrationDate = null;
					if(sample.registrationDetails && sample.registrationDetails.registrationDate) {
						registrationDate = Util.getFormatedDate(new Date(sample.registrationDetails.registrationDate));
					}
					
					var modifier = null;
					if(sample.registrationDetails && sample.registrationDetails.modifierUserId) {
						modifier = sample.registrationDetails.modifierUserId;
					}
					
					var modificationDate = null;
					if(sample.registrationDetails && sample.registrationDetails.modificationDate) {
						modificationDate = Util.getFormatedDate(new Date(sample.registrationDetails.modificationDate));
					}
					
					var sampleModel = { 
										'$object' : sample,
										'identifier' : sample.identifier, 
										'code' : sample.code,
										'sampleTypeCode' : sample.sampleTypeCode,
										'default_space' : sample.spaceCode,
										'permId' : sample.permId,
										'experiment' : sample.experimentIdentifierOrNull,
										'registrator' : registrator,
										'registrationDate' : registrationDate,
										'modifier' : modifier,
										'modificationDate' : modificationDate
									};
					
					if(sample.properties) {
						for(var propertyCode in sample.properties) {
							sampleModel[propertyCode] = sample.properties[propertyCode];
						}
					}
					
					var parents = "";
                    var sampleParents = result.objects[sIdx].parents;
                    if (sampleParents) {
                        for (var paIdx = 0; paIdx < sampleParents.length; paIdx++) {
                            if(paIdx !== 0) {
                                parents += ", ";
                            }
//                            parents += Util.getDisplayNameForEntity2(sampleParents[paIdx]);
                            parents += sampleParents[paIdx].identifier;
                        }
                    }
					
					sampleModel['parents'] = parents;
					
					var children = "";
                    var sampleChildren = result.objects[sIdx].children;
                    if(sampleChildren) {
                        var isFirst = true;
                        for (var caIdx = 0; caIdx < sampleChildren.length; caIdx++) {
                            if(sampleChildren[caIdx].sampleTypeCode === "STORAGE_POSITION") {
                                continue;
                            }
                            
                            if(!isFirst) {
                                children += ", ";
                            }
//                            children += Util.getDisplayNameForEntity2(sampleChildren[caIdx]);
                            children += sampleChildren[caIdx].identifier;
                            isFirst = false;
                        }
                    }
					
					sampleModel['children'] = children;
					
					dataList.push(sampleModel);
				}
				
				callback({
					objects : dataList,
					totalCount : result.totalCount
				});
			}
			
			var fetchOptions = {
					minTableInfo : true,
					withExperiment : withExperiment,
					withChildrenInfo : true,
					withParentInfo : true
			};
			
			var optionsSearch = null;
			if(options) {
				fetchOptions.count = options.pageSize;
				fetchOptions.from = options.pageIndex * options.pageSize;
				optionsSearch = options.search;
			}
			
			if(!criteria.cached || (criteria.cachedSearch !== optionsSearch)) {
				fetchOptions.cache = "RELOAD_AND_CACHE";
				criteria.cachedSearch = optionsSearch;
				criteria.cached = true;
			} else {
				fetchOptions.cache = "CACHE";
			}
			
			var criteriaToSend = $.extend(true, {}, criteria);

			if(options && options.searchOperator && options.search) {
				criteriaToSend.logicalOperator = options.searchOperator;
			    var filter = options.search.toLowerCase().split(/[ ,]+/); //Split by regular space or comma

				if(criteriaToSend.logicalOperator === "AND") {
                    for(var fIdx = 0; fIdx < filter.length; fIdx++) {
                        var fKeyword = filter[fIdx];
                        criteriaToSend.rules[Util.guid()] = { type : "All", name : "", value : fKeyword };
                    }
				} else if(criteriaToSend.logicalOperator === "OR") { // Using sub criteria
				    criteriaToSend.rules = {};
				    criteriaToSend.subCriteria = {};

				    for(var fIdx = 0; fIdx < filter.length; fIdx++) {
                        var subCriteria = $.extend(true, {}, criteria);
                        delete subCriteria.cached;
                        delete subCriteria.cachedSearch;
                        subCriteria.logicalOperator = "AND";
                        var fKeyword = filter[fIdx];
                        subCriteria.rules[Util.guid()] = { type : "All", name : "", value : fKeyword };
                        criteriaToSend.subCriteria[Util.guid()] = subCriteria;
                    }

				}
			}

			if(options && options.sortProperty && options.sortDirection) {
				fetchOptions.sort = { 
						type : null,
						name : null,
						direction : options.sortDirection
				}
				
				switch(options.sortProperty) {
					case "code":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "code";
						break;
					case "identifier":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "identifier";
						break;
					case "sampleTypeCode":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "type";
						break;
					case "registrationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "registrationDate"
						break;
					case "modificationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "modificationDate";
						break;
					default: //Properties
						fetchOptions.sort.type = "Property";
						fetchOptions.sort.name = options.sortProperty;
						break;
				}
			}
			
//			Util.blockUI();
//			mainController.serverFacade.searchForSamplesAdvanced(criteriaToSend, fetchOptions, function(result) {
//				callbackForSearch(result);
//				Util.unblockUI();
//			});
			mainController.serverFacade.searchForSamplesAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
		}
	}
	
	this.getDataList = function(samples) {
		return function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				var sample = samples[sIdx];
				
				var registrator = null;
				if(sample.registrationDetails && sample.registrationDetails.userId) {
					registrator = sample.registrationDetails.userId;
				}
				
				var registrationDate = null;
				if(sample.registrationDetails && sample.registrationDetails.registrationDate) {
					registrationDate = Util.getFormatedDate(new Date(sample.registrationDetails.registrationDate));
				}
				
				var modifier = null;
				if(sample.registrationDetails && sample.registrationDetails.modifierUserId) {
					modifier = sample.registrationDetails.modifierUserId;
				}
				
				var modificationDate = null;
				if(sample.registrationDetails && sample.registrationDetails.modificationDate) {
					modificationDate = Util.getFormatedDate(new Date(sample.registrationDetails.modificationDate));
				}
				
				var sampleModel = { '$object' : sample,
									'identifier' : sample.identifier, 
									'code' : sample.code,
									'sampleTypeCode' : sample.sampleTypeCode,
									'default_space' : sample.spaceCode,
									'permId' : sample.permId,
									'experiment' : sample.experimentIdentifierOrNull,
									'registrator' : registrator,
									'registrationDate' : registrationDate,
									'modifier' : modifier,
									'modificationDate' : modificationDate
								};
				
				if(sample.properties) {
					for(var propertyCode in sample.properties) {
						sampleModel[propertyCode] = sample.properties[propertyCode];
					}
				}
				
				var parents = "";
				if(sample.parents) {
					for (var paIdx = 0; paIdx < sample.parents.length; paIdx++) {
						if(paIdx !== 0) {
							parents += ", ";
						}
						parents += sample.parents[paIdx].identifier;
					}
				}
				
				sampleModel['parents'] = parents;
				
				dataList.push(sampleModel);
			}
			callback(dataList);
		};
	}
	
	this.createOperationsColumn = function() {
		return {
			label : "Operations",
			property : 'operations',
			isExportable: false,
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown table-options-dropdown' });
				var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
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
				
				var $upload = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'File Upload'}).append("File Upload"));
				$upload.click(function(event) {
					stopEventsBuble(event);
					mainController.changeView('showCreateDataSetPageFromPermId', data.permId, true);
				});
				$list.append($upload);
				
				var $move = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Move'}).append("Move"));
				$move.click(function(event) {
					stopEventsBuble(event);
					var moveSampleController = new MoveSampleController(data.permId, function() {
						mainController.refreshView();
					});
					moveSampleController.init();
				});
				$list.append($move);

                if(profile.mainMenu.showBarcodes) {
                    var $updateBarcode = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Update Barcode'}).append("Update Barcode"));
                    $updateBarcode.click(function(event) {
                        stopEventsBuble(event);
                        BarcodeUtil.readBarcode([data]);
                    });
                    $list.append($updateBarcode);
                }

				var $hierarchyGraph = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Open Hierarchy'}).append("Open Hierarchy"));
				$hierarchyGraph.click(function(event) {
					stopEventsBuble(event);
					mainController.changeView('showSampleHierarchyPage', data.permId, true);
				});
				$list.append($hierarchyGraph);

				var $hierarchyTable = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Open Hierarchy Table'}).append("Open Hierarchy Table"));
				$hierarchyTable.click(function(event) {
					stopEventsBuble(event);
					mainController.changeView('showSampleHierarchyTablePage', data.permId, true);
				});
				$list.append($hierarchyTable);
				
				return $dropDownMenu;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		}
	}
	
}