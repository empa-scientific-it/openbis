var ExperimentDataGridUtil = new function() {
	this.getExperimentDataGrid = function(entities, rowClick, multiselectable, heightPercentage) {
		//Fill Columns model
		var columns = [];

		columns.push({
			label : 'Code',
			property : 'code',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.CODE,
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
            exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY("$NAME"),
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
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.IDENTIFIER,
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
			label : 'PermId',
			property : 'permId',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PERM_ID,
			sortable : true,
			render : function(data) {
				return FormUtil.getFormLink(data.permId, "Experiment", data.identifier);
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
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATOR,
			sortable : true
		});
		
		columns.push({
			label : 'Registration Date',
			property : 'registrationDate',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATION_DATE,
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
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.MODIFIER,
			sortable : true
		});

		columns.push({
			label : 'Modification Date',
			property : 'modificationDate',
			exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.MODIFICATION_DATE,
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
                    'id' : entity.permId.permId,
                    'exportableId' : {
                        exportable_kind: DataGridExportOptions.EXPORTABLE_KIND.EXPERIMENT,
                        perm_id: entity.permId.permId,
                        type_perm_id: entity.type.code
                    },
                    '$object' : entity,
                    'code' : entity.code,
                    'identifier' : entity.identifier.identifier,
                    'permId' : entity.permId.permId,
                    'type' : entity.type.code,
                    'registrator' : (entity.registrator)?entity.registrator.userId:null,
                    'registrationDate' : (entity.registrationDate)?Util.getFormatedDate(new Date(entity.registrationDate)):null,
                    'modifier' : (entity.modifier)?entity.modifier.userId:null,
                    'modificationDate' : (entity.modificationDate)?Util.getFormatedDate(new Date(entity.modificationDate)):null
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
        var dataGridController = new DataGridController(null, columns, [], dynamicColumnsFunc, getDataList, rowClick, false, configKey, multiselectable, {
            fileFormat: DataGridExportOptions.FILE_FORMAT.XLS,
            filePrefix: 'collections',
            fileContent: DataGridExportOptions.FILE_CONTENT.ENTITIES
        }, heightPercentage);
		dataGridController.setId("experiment-grid")
		return dataGridController;
	}

}