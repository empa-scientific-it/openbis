/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteSqlOperation = function(sql, options) {
		this.sql = sql;
		this.options = options;
	};
	stjs.extend(ExecuteSqlOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.execute.ExecuteSqlOperation';
		prototype.sql = null;
		prototype.options = null;
		prototype.getSql = function() {
			return this.sql;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteSqlOperation";
		};
	}, {
		options : "SqlExecutionOptions"
	});
	return ExecuteSqlOperation;
})