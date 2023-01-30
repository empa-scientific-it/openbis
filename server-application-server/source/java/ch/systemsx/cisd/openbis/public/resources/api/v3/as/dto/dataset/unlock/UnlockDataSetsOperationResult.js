define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var UnlockDataSetsOperationResult = function() {
	};
	stjs.extend(UnlockDataSetsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.unlock.UnlockDataSetsOperationResult';
		prototype.getMessage = function() {
			return "UnlockDataSetsOperationResult";
		};
	}, {});
	return UnlockDataSetsOperationResult;
})
