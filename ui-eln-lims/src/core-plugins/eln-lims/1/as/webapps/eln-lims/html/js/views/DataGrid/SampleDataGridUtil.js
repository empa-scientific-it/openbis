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
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.CODE,
			filterable: true,
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
				var codeId = data.code.toLowerCase() + "-column-id";
				return (isLinksDisabled)?data.code:FormUtil.getFormLink(data.code, "Sample", data.permId, paginationInfo, codeId);
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
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY("$NAME"),
			filterable: true,
			sortable : true,
			render : function(data) {
				var nameToUse = "";
                if(data[profile.propertyReplacingCode]) {
                    nameToUse = data[profile.propertyReplacingCode];
                }
                var nameId = data.code.toLowerCase() + "-name-id";
				return (isLinksDisabled) ? nameToUse : FormUtil.getFormLink(nameToUse, "Sample", data.permId, null, nameId);
			}
		});

        if(profile.mainMenu.showBarcodes || true) {
            var permIdLabel = "PermId";
            if(profile.mainMenu.showBarcodes) {
                permIdLabel += " / Default Barcode/QR Code";
            }
            columnsFirst.push({
                label : permIdLabel,
                property : 'permId',
                exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PERM_ID,
                filterable: true,
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
                    var codeId = data.permId.toLowerCase() + "-column-id";
                    return (isLinksDisabled)?data.code:FormUtil.getFormLink(data.permId, "Sample", data.permId, paginationInfo, codeId);
                },
                filter : function(data, filter) {
                    return data.permId.toLowerCase().indexOf(filter) !== -1;
                },
                sort : function(data1, data2, asc) {
                    var value1 = data1.permId;
                    var value2 = data2.permId;
                    var sortDirection = (asc)? 1 : -1;
                    return sortDirection * naturalSort(value1, value2);
                }
            });
        }

		columnsFirst.push({
			label : 'Identifier',
			property : 'identifier',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.IDENTIFIER,
			filterable : true,
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
			
			var propertyColumnsToSort = SampleDataGridUtil.createPropertyColumns(foundPropertyCodes);
			FormUtil.sortPropertyColumns(propertyColumnsToSort, samples.map(function(sample){
				return {
					entityKind: "SAMPLE",
					entityType: sample.sampleTypeCode
				}
			}))

			return propertyColumnsToSort;
		}

		var columnsLast = [];
		columnsLast.push({
        			label : '---------------',
        			property : null,
        			sortable : false
        });
		columnsLast.push({
			label : 'Type',
			property : 'sampleTypeCode',
			filterable : true,
			sortable : true,
		    render : function(data, grid) {
                return Util.getDisplayNameFromCode(data.sampleTypeCode);
            },
		});

		columnsLast.push({
			label : 'Space',
			property : 'default_space',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.SPACE,
			filterable: true,
			sortable : true
		});

		if(withExperiment) {
			columnsLast.push({
				label : ELNDictionary.getExperimentDualName(),
				property : 'experiment',
				exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.EXPERIMENT,
				filterable: true,
				sortable : false
			});
		}

        if (showParentsAndChildren) {
            columnsLast.push({
                label : 'Parents',
                property : 'parents',
                exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PARENTS,
                filterable: true,
                sortable : false,
                truncate: true,
                getValue : function(params) {
                    return _this.getRelatedSamples(params.row.parents, params);
                },
                filter : function(data, filter) {
                    return _this.filterRelatedSamples(data.parents, filter);
                },
                render : function(data, grid) {
                    return _this.renderRelatedSamples(data.parents, isLinksDisabled);
                }
            });

            columnsLast.push({
                label : 'Children',
                property : 'children',
                exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.CHILDREN,
                filterable: true,
                sortable : false,
                truncate: true,
                getValue : function(params) {
                    return _this.getRelatedSamples(params.row.children, params);
                },
                filter : function(data, filter) {
                    return _this.filterRelatedSamples(data.children, filter);
                },
                render : function(data, grid) {
                    return _this.renderRelatedSamples(data.children, isLinksDisabled);
                }
            });
        }

		columnsLast.push({
			label : 'Storage',
			property : 'storage',
			filterable: false,
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
			filterable: false,
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
			sortable : false
		});
		
		columnsLast.push({
			label : 'Registrator',
			property : 'registrator',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATOR,
			filterable: true,
			sortable : false
		});
		
		columnsLast.push({
			label : 'Registration Date',
			property : 'registrationDate',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATION_DATE,
			filterable: true,
			sortable : true,
			renderFilter : function(params) {
				return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP");
			}
		});
		
		columnsLast.push({
			label : 'Modifier',
			property : 'modifier',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.MODIFIER,
			filterable: true,
			sortable : false,
		});
		
		columnsLast.push({
			label : 'Modification Date',
			property : 'modificationDate',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.MODIFICATION_DATE,
			filterable: true,
			sortable : true,
			renderFilter : function(params) {
				return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP");
			}
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
		
		var dataGridController = new DataGridController(null, columnsFirst, columnsLast, dynamicColumnsFunc, getDataList, rowClick, false, configKey, isMultiselectable, {
			fileFormat: DataGridExportOptions.FILE_FORMAT.XLS,
			filePrefix: 'objects',
			fileContent: DataGridExportOptions.FILE_CONTENT.ENTITIES
		}, heightPercentage);
		dataGridController.setId("sample-grid")
		return dataGridController;
	}

    this.getRelatedSamples = function(samples, params) {
        if (params.operation === 'export') {
            if (params.exportOptions.values === 'RICH_TEXT') {
                return samples ? samples.map(s => s.getIdentifier().getIdentifier()).join(", ") : "";
            }
            return this.renderRelatedSamples(samples, true).text();
        }
        return samples;
    }

    this.renderRelatedSamples = function(samples, isLinksDisabled) {
        var output = $("<span>");
        if (samples) {
            for (var idx = 0; idx < samples.length; idx++) {
                var sample = samples[idx];
                var id = sample.getIdentifier().getIdentifier();
                var rendered = Util.getDisplayNameForEntity2(sample);
                var eComponent = isLinksDisabled ? rendered : FormUtil.getFormLink(rendered, "Sample", id, null);
                if (idx != 0) {
                    output.append(", ");
                }
                output.append(eComponent);
            }
        }
        return output;
    }

    this.filterRelatedSamples = function(samples, filter) {
        var isMatch = false;
        if (samples) {
            for (var idx = 0; idx < samples.length; idx++) {
                var sample = samples[idx];
                var code = sample.getCode();
                var name = sample.properties[profile.propertyReplacingCode];
                isMatch = isMatch || code.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
                isMatch = isMatch || name.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
            }
        }
        return isMatch;
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
										'id' : sample.permId,
										'exportableId' : {
											exportable_kind: DataGridExportOptions.EXPORTABLE_KIND.SAMPLE,
											perm_id: sample.permId,
											type_perm_id: sample.sampleTypeCode
										},
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
					
                    sampleModel['parents'] = result.objects[sIdx].parents;;
					
					var children = [];
                    var sampleChildren = result.objects[sIdx].children;
                    if(sampleChildren) {
                        for (var caIdx = 0; caIdx < sampleChildren.length; caIdx++) {
                            if(sampleChildren[caIdx].sampleTypeCode === "STORAGE_POSITION") {
                                continue;
                            }
                            children.push(sampleChildren[caIdx]);
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
                optionsSearch = JSON.stringify({
                   searchMode: options.searchMode,
                   searchMap: options.searchMap,
                   globalSearch: options.globalSearch 
                })
			}
			
			if(!criteria.cached || (criteria.cachedSearch !== optionsSearch)) {
				fetchOptions.cache = "RELOAD_AND_CACHE";
				criteria.cachedSearch = optionsSearch;
				criteria.cached = true;
			} else {
				fetchOptions.cache = "CACHE";
			}
			
            var mainSubcriteria = $.extend(true, {}, criteria)

            var gridSubcriteria = {
                logicalOperator: "AND",
                rules: [],
            }

            var criteriaToSend = {
                logicalOperator: "AND",
                rules: [],
                subCriteria: [mainSubcriteria, gridSubcriteria]
            }

            if(options) {
                if(options.searchMode === "GLOBAL_FILTER") {
                    if(options.globalSearch.text !== null) {
                        gridSubcriteria.logicalOperator = options.globalSearch.operator

                        var tokens = options.globalSearch.text.toLowerCase().split(/[ ,]+/)
                        tokens.forEach(function(token) {
                            gridSubcriteria.rules[Util.guid()] = { type : "All", name : "", value : token };
                        })
                    }
                } else if (options.searchMode === "COLUMN_FILTERS") {
                    for (var field in options.searchMap) {
                        var search = options.searchMap[field] || ""

                        if (_.isString(search)) {
                            search = search.trim()
                        }
                        if (field === "sampleTypeCode") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "SAMPLE_TYPE", value : search, operator: "thatContains" };
                        } else if (field === "default_space") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "SPACE", value : search, operator: "thatContains" };
                        } else if(field === "experiment") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "EXPERIMENT_IDENTIFIER", value : search, operator: "thatContains" };
                        } else if (field === "permId") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "PERM_ID", value : search, operator: "thatContains" };
                        } else if (field === "code") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "CODE", value : search, operator: "thatContains" };
                        } else if (field === "identifier") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "IDENTIFIER", value : search, operator: "thatContains" };
                        } else if (field === "registrator") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "REGISTRATOR", value : search, operator: "thatContainsUserId" };
                        } else if (field === "registrationDate") {
                            if (search.from && search.from.dateObject) {
                                gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "REGISTRATION_DATE", value : search.from.dateString, operator: "thatIsLaterThanOrEqualToDate" };
                            }
                            if (search.to && search.to.dateObject) {
                                gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "REGISTRATION_DATE", value : search.to.dateString, operator: "thatIsEarlierThanOrEqualToDate" };
                            }
                        } else if (field === "modifier") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "MODIFIER", value : search, operator: "thatContainsUserId" };
                        } else if (field === "modificationDate") {
                            if (search.from && search.from.dateObject) {
                                gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "MODIFICATION_DATE", value : search.from.dateString, operator: "thatIsLaterThanOrEqualToDate" };
                            }
                            if (search.to && search.to.dateObject) {
                                gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "MODIFICATION_DATE", value : search.to.dateString, operator: "thatIsEarlierThanOrEqualToDate" };
                            }
                        } else if (field === "parents") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "PARENTS", value : search, operator: "thatContains" };
                        } else if (field === "children") {
                            gridSubcriteria.rules[Util.guid()] = { type : "Attribute", name : "CHILDREN", value : search, operator: "thatContains" };
                        } else {
                            var column = options.columnMap[field]
                            var dataType = null

                            if (column && column.metadata) {
                                dataType = column.metadata.dataType
                            }

                            if (dataType === "DATE" || dataType === "TIMESTAMP") {
                                if (search.from && search.from.dateObject) {
                                    gridSubcriteria.rules[Util.guid()] = { type : "Property", name : "PROP." + field, value : search.from.dateString, operator: "thatIsLaterThanOrEqualToDate" };
                                }
                                if (search.to && search.to.dateObject) {
                                    gridSubcriteria.rules[Util.guid()] = { type : "Property", name : "PROP." + field, value : search.to.dateString, operator: "thatIsEarlierThanOrEqualToDate" };
                                }
                            } else {
                                var operator = null

                                if (dataType === "INTEGER" || dataType === "REAL") {
                                    operator = "thatEqualsNumber"
                                } else if (dataType === "BOOLEAN") {
                                    operator = "thatEqualsBoolean"
                                } else if (dataType === "CONTROLLEDVOCABULARY") {
                                    operator = "thatEqualsString"
                                } else {
                                    operator = "thatContainsString"
                                }
    
                                gridSubcriteria.rules[Util.guid()] = { type : "Property", name : "PROP." + field, value : search, operator: operator };
                            }
                        }
                    }
                }
            }

            if(options && options.sortings) {
                var sortings = []
                options.sortings.forEach(function(optionsSorting){
                    var sorting = {
                        direction: optionsSorting.sortDirection
                    }
                    switch(optionsSorting.columnName) {
                        case "permId":
                            sorting.type = "Attribute";
                            sorting.name = "permId";
                            break;
                        case "code":
                            sorting.type = "Attribute";
                            sorting.name = "code";
                            break;
                        case "identifier":
                        case "default_space":
                            sorting.type = "Attribute";
                            sorting.name = "identifier";
                            break;
                        case "sampleTypeCode":
                            sorting.type = "Attribute";
                            sorting.name = "type";
                            break;
                        case "registrationDate":
                            sorting.type = "Attribute";
                            sorting.name = "registrationDate"
                            break;
                        case "modificationDate":
                            sorting.type = "Attribute";
                            sorting.name = "modificationDate";
                            break;
                        default: //Properties
                            sorting.type = "Property";
                            sorting.name = optionsSorting.columnName;
                            break;
                    }
                    sortings.push(sorting)
                })
                fetchOptions.sortings = sortings
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
				
				var sampleModel = {
									'id' : sample.permId,
									'exportableId' : {
										exportable_kind: DataGridExportOptions.EXPORTABLE_KIND.SAMPLE,
										perm_id: sample.permId,
										type_perm_id: sample.sampleTypeCode
									},
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
			sortable : false,
            filterable: false,
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
                    var $updateBarcode = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Update Barcode/QR Code'}).append("Update Barcode/QR Code"));
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

    this.createPropertyColumns = function(foundPropertyCodes) {
        var _this = this;
        var propertyColumnsToSort = [];
        for (propertyCode in foundPropertyCodes) {
            var propertiesToSkip = ["$NAME", "$XMLCOMMENTS", "$ANNOTATIONS_STATE"];
            if($.inArray(propertyCode, propertiesToSkip) !== -1) {
                continue;
            }
            var propertyType = profile.getPropertyType(propertyCode);

            if(propertyType.dataType === "BOOLEAN"){
                var getBooleanColumn = function(propertyType) {
                    return {
                        label : propertyType.label,
                        property : propertyType.code,
                        exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyType.code),
                        filterable : true,
                        sortable : false,
                        metadata: {
                            dataType: propertyType.dataType
                        },
                        getValue : (function(propertyType) {
                            return function(params) {
                                return _this.getBooleanValue(params, propertyType);
                            };
                        })(propertyType),
                        renderFilter : function(params) {
                            return FormUtil.renderBooleanGridFilter(params);
                        },
                        render : function(row, params){
                            return FormUtil.renderBooleanGridValue(params)
                        }
                    };
                }
                propertyColumnsToSort.push(getBooleanColumn(propertyType));
            } else if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
                var getVocabularyColumn = function(propertyType) {
                    return function() {
                        return {
                            label : propertyType.label,
                            property : propertyType.code,
                            exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyType.code),
                            filterable: true,
                            sortable : true,
                            metadata: {
                                dataType: propertyType.dataType
                            },
                            getValue : (function(propertyType) {
                                return function(params) {
                                    return _this.getTerm(params, propertyType);
                                };
                            })(propertyType),
                            render : function(params, data) {
                                return FormUtil.getVocabularyLabelForTermCode(propertyType, data.value);
                            },
                            renderFilter: function(params){
                                return FormUtil.renderVocabularyGridFilter(params, propertyType.vocabulary);
                            },
                            filter : function(data, filter) {
                                var value = FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
                                return value && value.toLowerCase().indexOf(filter ? filter.toLowerCase() : filter) !== -1;
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
                        exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyType.code),
                        filterable : true,
                        sortable : true,
                        metadata: {
                            dataType: propertyType.dataType
                        },
                        render : function(data) {
                            return FormUtil.asHyperlink(data[propertyType.code]);
                        }
                    };
                }
                propertyColumnsToSort.push(getHyperlinkColumn(propertyType));
            } else if (propertyType.dataType === "DATE" || propertyType.dataType === "TIMESTAMP") {
                var getDateColumn = function(propertyType){
                    return {
                        label : propertyType.label,
                        property : propertyType.code,
                        exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyType.code),
                        filterable : true,
                        sortable : true,
                        metadata: {
                            dataType: propertyType.dataType
                        },
                        renderFilter : function(params) {
                            return FormUtil.renderDateRangeGridFilter(params, propertyType.dataType)
                        },
                        filter : function(data, filter){
                            return FormUtil.filterDateRangeGridColumn(data[propertyType.code], filter)
                        }
                    }
                }
                propertyColumnsToSort.push(getDateColumn(propertyType));
            } else {
                var renderValue = null;

                if(propertyType.dataType === "XML"){
                    renderValue = (function(propertyType){
                        return function(row, params){
                            return FormUtil.renderXmlGridValue(row, params, propertyType)
                        }
                    })(propertyType)
                } else if(propertyType.dataType === "MULTILINE_VARCHAR"){
                    renderValue = (function(propertyType){
                        return function(row, params){
                            return FormUtil.renderMultilineVarcharGridValue(row, params, propertyType)
                        }
                    })(propertyType)
                } else if(propertyType.dataType === "SAMPLE") {
                    renderValue = (function(propertyType){
                          return function(row, params){
                            if(Array.isArray(params.value)) {
                               var result = [];
                               for (var singleValue of params.value) {
                                   if(result.length > 0) {
                                       result.push(', ')
                                   }
                                   result.push(FormUtil.getFormLink(singleValue, "Sample", singleValue));
                               }
                               return result;
                            } else {
                               return FormUtil.getFormLink(params.value, "Sample", params.value);
}
                          }
                      })(propertyType)
                }

                propertyColumnsToSort.push({
                    label : propertyType.label,
                    property : propertyType.code,
                    exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyType.code),
                    filterable : true,
                    sortable : propertyType.dataType !== "XML",
                    truncate: true,
                    metadata: {
                        dataType: propertyType.dataType
                    },
                    render: renderValue
                });
            }
        }
        return propertyColumnsToSort;
    }

    this.getBooleanValue = function(params, propertyType) {
        var value = params.row[propertyType.code]

        if(value === null || value === undefined || (_.isString(value) && value.trim() === "")){
            return null
        } else if (value === "true") {
            return true
        } else {
            return false
        }
    }

    this.getTerm = function(params, propertyType) {
        var value = params.row[propertyType.code]
//        if(Array.isArray(value)) {
//            return value.sort().toString();
//        } else {
            return value ? value : "";
//        }
    }
}