var ExperimentDataGridUtil = new function() {
	this.getExperimentDataGrid = function(entities, rowClick, heightPercentage) {
		//Fill Columns model
		var columns = [];

		columns.push({
			label : 'Code',
			property : 'code',
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
        columns.push({
            label : 'Name',
            property : '$NAME',
            sortable : true,
            render : function(data) {
                var nameToUse = "";
                if(data[profile.propertyReplacingCode]) {
                    nameToUse = data[profile.propertyReplacingCode];
                }
                return FormUtil.getFormLink(nameToUse, "Experiment", data.identifier);
            }
        });

		columns.push({
			label : 'Identifier',
			property : 'identifier',
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
            label : 'Type',
            property : 'type',
            sortable : true
        });
        
		columns.push({
			label : 'Registrator',
			property : 'registrator',
			sortable : true
		});
		
		columns.push({
			label : 'Registration Date',
			property : 'registrationDate',
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
			sortable : true
		});

		columns.push({
			label : 'Modification Date',
			property : 'modificationDate',
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
									'exportableId' : {
										exportable_kind: 'EXPERIMENT',
										perm_id: entity.permId,
										type_perm_id: entity.experimentTypeCode
									},
									'code' : entity.code,
									'identifier' : entity.identifier,
									'permId' : entity.permId,
									'type' : entity.experimentTypeCode,
									'registrator' : entity.registrationDetails.userId,
									'registrationDate' : Util.getFormatedDate(new Date(entity.registrationDetails.registrationDate)),
									'modifier' : entity.registrationDetails.modifierUserId,
									'modificationDate' : Util.getFormatedDate(new Date(entity.registrationDetails.modificationDate))
				};
				if(entity.properties) {
                    for(var propertyCode in entity.properties) {
                        model[propertyCode] = entity.properties[propertyCode];
                    }
                }
				dataList.push(model);
			}
			callback(dataList);
		};

        var dynamicColumnsFunc = function(experiments) {
            var foundPropertyCodes = {};
            var foundExperimentTypes = {};
            for(var idx = 0; idx < experiments.length; idx++) {
                var experiment = experiments[idx];
                if(!foundExperimentTypes[experiment.type]) {
                    foundExperimentTypes[experiment.type] = true;
                    var propertyCodes = profile.getAllPropertiCodesForExperimentTypeCode(experiment.type);
                    for(var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
                        foundPropertyCodes[propertyCodes[pIdx]] = true;
                    }
                }
            }
            var propertyColumnsToSort = SampleDataGridUtil.createPropertyColumns(foundPropertyCodes);
            FormUtil.sortPropertyColumns(propertyColumnsToSort, entities.map(function(entity){
                return {
                    entityKind: "EXPERIMENT",
                    entityType: entity.type
                }
            }))

            return propertyColumnsToSort;
        }
			
		//Create and return a data grid controller
        var configKey = "EXPERIMENT_TABLE";
        var dataGridController = new DataGridController(null, columns, [], dynamicColumnsFunc, getDataList, rowClick, false, configKey, null, {
            fileFormat: 'XLS',
            filePrefix: 'collections',
            fileContent: 'ENTITIES'
        }, heightPercentage);
		dataGridController.setId("experiment-grid")
		return dataGridController;
	}

}