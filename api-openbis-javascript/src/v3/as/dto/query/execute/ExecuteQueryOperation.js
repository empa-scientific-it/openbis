/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteQueryOperation = function(queryId, options) {
		this.queryId = queryId;
		this.options = options;
	};
	stjs.extend(ExecuteQueryOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.ExecuteQueryOperation';
		prototype.queryId = null;
		prototype.options = null;
		prototype.getQueryId = function() {
			return this.queryId;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteQueryOperation";
		};
	}, {
		queryId : "IQueryId",
		options : "QueryExecutionOptions"
	});
	return ExecuteQueryOperation;
})