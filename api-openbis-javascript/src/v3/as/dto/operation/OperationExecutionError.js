/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationExecutionError" ], function(stjs, IOperationExecutionError) {
	var OperationExecutionError = function(message) {
		this.message = message;
	};
	stjs.extend(OperationExecutionError, null, [ IOperationExecutionError ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.OperationExecutionError';
		prototype.message = null;
		prototype.getMessage = function() {
			return this.message;
		};
		prototype.hashCode = function() {
			return 0;
		};
	}, {});
	return OperationExecutionError;
})