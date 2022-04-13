var ExperimentDataGridUtil = new function() {
	this.getExperimentDataGrid = function(typeCode, entities, rowClick, heightPercentage) {
		var type = profile.getExperimentTypeForExperimentTypeCode(typeCode);
		var propertyCodes = profile.getAllPropertiCodesForExperimentTypeCode(typeCode);
		var propertyCodesDisplayNames = profile.getPropertiesDisplayNamesForExperimentTypeCode(typeCode, propertyCodes);
		
		//Fill Columns model
		var columns = [];

		columns.push({
			label : 'Code',
			property : 'code',
			isExportable: false,
			sortable : true,
			render : function(data, grid) {
				return FormUtil.getFormLink(data.code, "Experiment", data.identifier);
			},
			filter : function(data, filter) {
				return data.code.toLowerCase().indexOf(filter) !== -1;
			},
			sort : function(data1, data2, asc) {
				var value1 = data1.code;
				var value2 = data2.code;
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(value1, value2);
			}
		});

		if($.inArray("$NAME", propertyCodes) !== -1) {
			columns.push({
				label : 'Name',
				property : '$NAME',
				isExportable: true,
				sortable : true,
				render : function(data) {
					return FormUtil.getFormLink(data[profile.propertyReplacingCode], "Experiment", data.identifier);
				}
			});
		}
		
		var propertyColumnsToSort = [];
		for (var idx = 0; idx < propertyCodes.length; idx++) {
			var propertiesToSkip = ["$NAME", "$XMLCOMMENTS"];
			var propertyCode = propertyCodes[idx];
			if($.inArray(propertyCode, propertiesToSkip) !== -1) {
				continue;
			}
			var propertyType = profile.getPropertyType(propertyCode);
			if(propertyType.dataType === "BOOLEAN"){
				var getBooleanColumn = function(propertyType) {
					return {
						label : propertyCodesDisplayNames[idx],
						property : propertyCodes[idx],
						isExportable: true,
						filterable : true,
						sortable : true,
						renderFilter : function(params) {
							return FormUtil.renderBooleanGridFilter(params);
						}
					};
				}
				propertyColumnsToSort.push(getBooleanColumn(propertyType));
			} else if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
				var getVocabularyColumn = function(propertyType) {
					return function() {
						return {
							label : propertyCodesDisplayNames[idx],
							property : propertyCodes[idx],
							isExportable: true,
							sortable : true,
							render : function(data) {
								return FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
							},
							renderFilter: function(params){
								return FormUtil.renderVocabularyGridFilter(params, propertyType.vocabulary);
							},
							filter : function(data, filter) {
								return data[propertyType.code] === filter
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
			} else if (propertyType.dataType === "DATE" || propertyType.dataType === "TIMESTAMP") {
				var getDateColumn = function(propertyType, idx){
					return {
						label : propertyCodesDisplayNames[idx],
						property : propertyCodes[idx],
						isExportable: true,
						sortable : true,
						renderFilter : function(params) {
							return FormUtil.renderDateRangeGridFilter(params, propertyType.dataType);
						},
						filter : function(data, filter){
							return FormUtil.filterDateRangeGridColumn(data[propertyCodes[idx]], filter)
						}
					}
				}
				propertyColumnsToSort.push(getDateColumn(propertyType, idx))
			} else {
				var renderValue = null

				if(propertyType.dataType === "XML"){
					renderValue = (function(propertyType){
						return function(row, params){
							return FormUtil.renderXmlGridValue(row, params, propertyType)
						}
					})(propertyType)
				}else if(propertyType.dataType === "MULTILINE_VARCHAR"){
					renderValue = (function(propertyType){
						return function(row, params){
							return FormUtil.renderMultilineVarcharGridValue(row, params, propertyType)
						}
					})(propertyType)
				}
				
				propertyColumnsToSort.push({
					label : propertyCodesDisplayNames[idx],
					property : propertyCodes[idx],
					isExportable: true,
					sortable : true,
					truncate: true,
					render: renderValue
				});
			}
		}
		
		columns.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});

		FormUtil.sortPropertyColumns(propertyColumnsToSort, entities.map(function(entity){
            return {
                entityKind: "EXPERIMENT",
                entityType: typeCode
            }
        }))

		columns = columns.concat(propertyColumnsToSort);
		columns.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});

		columns.push({
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : true,
			render : function(data) {
				return FormUtil.getFormLink(data.identifier, "Experiment", data.identifier);
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

		columns.push({
			label : 'Registrator',
			property : 'registrator',
			isExportable: false,
			sortable : true
		});
		
		columns.push({
			label : 'Registration Date',
			property : 'registrationDate',
			isExportable: false,
			sortable : true,
			renderFilter : function(params) {
				return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP");
			},
			filter : function(data, filter){
				return FormUtil.filterDateRangeGridColumn(data.registrationDate, filter)
			}
		})

		columns.push({
			label : 'Modifier',
			property : 'modifier',
			isExportable: false,
			sortable : true
		});

		columns.push({
			label : 'Modification Date',
			property : 'modificationDate',
			isExportable: false,
			sortable : true,
			renderFilter : function(params) {
				return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP");
			},
			filter : function(data, filter){
				return FormUtil.filterDateRangeGridColumn(data.modificationDate, filter)
			}
		})
		
		//Fill data model
		var getDataList = function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < entities.length; sIdx++) {
				var entity = entities[sIdx];
				var model = {		
									'id' : entity.permId,
									'code' : entity.code,
									'identifier' : entity.identifier,
									'permId' : entity.permId,
									'registrator' : entity.registrationDetails.userId,
									'registrationDate' : Util.getFormatedDate(new Date(entity.registrationDetails.registrationDate)),
									'modifier' : entity.registrationDetails.modifierUserId,
									'modificationDate' : Util.getFormatedDate(new Date(entity.registrationDetails.modificationDate))
				};
				
				for (var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
					var propertyCode = propertyCodes[pIdx];
					model[propertyCode] = entity.properties[propertyCode];
				}
				
				dataList.push(model);
			}
			callback(dataList);
		};
			
		//Create and return a data grid controller
		var configKey = "ENTITY_TABLE_"+ typeCode;
		var dataGridController = new DataGridController(null, columns, [], null, getDataList, rowClick, false, configKey, null, heightPercentage);
		dataGridController.setId("experiment-grid")
		return dataGridController;
	}

}