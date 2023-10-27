/**
 */
define([ "stjs", "dss/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteCustomDSSServiceOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteCustomDSSServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.service.execute.ExecuteCustomDSSServiceOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteCustomDSSServiceOperationResult";
		};
	}, {
		result : "Object"
	});
	return ExecuteCustomDSSServiceOperationResult;
})