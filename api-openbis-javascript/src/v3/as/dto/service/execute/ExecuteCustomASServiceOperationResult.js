/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteCustomASServiceOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteCustomASServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteCustomASServiceOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteCustomASServiceOperationResult";
		};
	}, {
		result : "Object"
	});
	return ExecuteCustomASServiceOperationResult;
})