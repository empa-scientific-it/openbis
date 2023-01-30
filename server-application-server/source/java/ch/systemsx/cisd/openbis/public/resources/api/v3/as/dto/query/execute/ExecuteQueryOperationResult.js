/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteQueryOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteQueryOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.ExecuteQueryOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteQueryOperationResult";
		};
	}, {
		result : "TableModel"
	});
	return ExecuteQueryOperationResult;
})