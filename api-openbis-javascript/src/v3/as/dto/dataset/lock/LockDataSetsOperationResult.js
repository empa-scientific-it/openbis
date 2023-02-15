define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var LockDataSetsOperationResult = function() {
	};
	stjs.extend(LockDataSetsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.lock.LockDataSetsOperationResult';
		prototype.getMessage = function() {
			return "LockDataSetsOperationResult";
		};
	}, {});
	return LockDataSetsOperationResult;
})
