/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteSqlOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteSqlOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.ExecuteSqlOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteSqlOperationResult";
		};
	}, {
		result : "TableModel"
	});
	return ExecuteSqlOperationResult;
})