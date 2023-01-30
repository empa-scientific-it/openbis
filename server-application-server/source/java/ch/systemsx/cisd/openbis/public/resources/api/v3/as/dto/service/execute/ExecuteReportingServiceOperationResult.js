define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteReportingServiceOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteReportingServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteReportingServiceOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteReportingServiceOperationResult";
		};
	}, {
		result : "TableModel"
	});
	return ExecuteReportingServiceOperationResult;
})
