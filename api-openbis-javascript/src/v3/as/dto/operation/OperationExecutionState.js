/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var OperationExecutionState = function() {
		Enum.call(this, [ "NEW", "SCHEDULED", "RUNNING", "FINISHED", "FAILED" ]);
	};
	stjs.extend(OperationExecutionState, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new OperationExecutionState();
})