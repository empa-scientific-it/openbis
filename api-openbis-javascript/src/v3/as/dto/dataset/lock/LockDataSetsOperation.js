define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var LockDataSetsOperation = function(dataSetIds, options) {
		this.dataSetIds = dataSetIds;
		this.options = options;
	};
	stjs.extend(LockDataSetsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.lock.LockDataSetsOperation';
		prototype.dataSetIds = null;
		prototype.options = null;
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "LockDataSetsOperation";
		};
	}, {
		dataSetIds : {
			name : "List",
			arguments : [ "IDataSetId" ]
		},
		options : "DataSetLockOptions"
	});
	return LockDataSetsOperation;
})
