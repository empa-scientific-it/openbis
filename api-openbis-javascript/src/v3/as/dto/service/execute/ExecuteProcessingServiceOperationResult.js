define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteProcessingServiceOperationResult = function() {
	};
	stjs.extend(ExecuteProcessingServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteProcessingServiceOperationResult';
		prototype.getMessage = function() {
			return "ExecuteProcessingServiceOperationResult";
		};
	}, {
	});
	return ExecuteProcessingServiceOperationResult;
})
