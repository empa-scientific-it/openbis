/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var UnarchiveDataSetsOperation = function(dataSetIds, options) {
		this.dataSetIds = dataSetIds;
		this.options = options;
	};
	stjs.extend(UnarchiveDataSetsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.unarchive.UnarchiveDataSetsOperation';
		prototype.dataSetIds = null;
		prototype.options = null;
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "UnarchiveDataSetsOperation";
		};
	}, {
		dataSetIds : {
			name : "List",
			arguments : [ "IDataSetId" ]
		},
		options : "DataSetUnarchiveOptions"
	});
	return UnarchiveDataSetsOperation;
})