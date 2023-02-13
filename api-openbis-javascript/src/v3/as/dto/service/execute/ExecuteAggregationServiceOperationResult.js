define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteAggregationServiceOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteAggregationServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteAggregationServiceOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteAggregationServiceOperationResult";
		};
	}, {
		result : "TableModel"
	});
	return ExecuteAggregationServiceOperationResult;
})
