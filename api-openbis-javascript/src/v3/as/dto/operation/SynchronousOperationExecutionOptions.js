/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/AbstractOperationExecutionOptions" ], function(stjs, AbstractOperationExecutionOptions) {
	var SynchronousOperationExecutionOptions = function() {
		AbstractOperationExecutionOptions.call(this);
	};
	stjs.extend(SynchronousOperationExecutionOptions, AbstractOperationExecutionOptions, [ AbstractOperationExecutionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.SynchronousOperationExecutionOptions';
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
	return SynchronousOperationExecutionOptions;
})