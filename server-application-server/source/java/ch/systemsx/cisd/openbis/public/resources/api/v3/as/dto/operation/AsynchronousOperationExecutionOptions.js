/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/AbstractOperationExecutionOptions" ], function(stjs, AbstractOperationExecutionOptions) {
	var AsynchronousOperationExecutionOptions = function() {
		AbstractOperationExecutionOptions.call(this);
	};
	stjs.extend(AsynchronousOperationExecutionOptions, AbstractOperationExecutionOptions, [ AbstractOperationExecutionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.AsynchronousOperationExecutionOptions';
	}, {});
	return AsynchronousOperationExecutionOptions;
})