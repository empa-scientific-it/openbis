/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/IOperationExecutionResults" ], function(stjs, IOperationExecutionResults) {
	var AsynchronousOperationExecutionResults = function(executionId) {
		this.executionId = executionId;
	};
	stjs.extend(AsynchronousOperationExecutionResults, null, [ IOperationExecutionResults ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.AsynchronousOperationExecutionResults';
		prototype.executionId = null;

		prototype.setExecutionId = function(executionId) {
			this.executionId = executionId;
		};
		prototype.getExecutionId = function() {
			return this.executionId;
		};

	}, {
		executionId : "OperationExecutionPermId"
	});
	return AsynchronousOperationExecutionResults;
})