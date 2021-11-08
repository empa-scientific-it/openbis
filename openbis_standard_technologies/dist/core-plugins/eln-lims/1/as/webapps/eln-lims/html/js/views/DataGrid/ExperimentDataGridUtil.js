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
			if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
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
					label : propertyCodesDisplayNames[idx],
					property : propertyCodes[idx],
					isExportable: true,
					sortable : true
				});
			}
		}
		
		columns.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});
		propertyColumnsToSort.sort(function(propertyA, propertyB) {
			return propertyA.label.localeCompare(propertyB.label);
		});
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
			sortable : true
		});
		
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
			sortable : true
		});
		
		//Fill data model
		var getDataList = function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < entities.length; sIdx++) {
				var entity = entities[sIdx];
				var model = {		'code' : entity.code,
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
		return dataGridController;
	}

}