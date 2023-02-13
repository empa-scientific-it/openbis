/**
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/common/Enum" ], function(stjs, exceptions, Enum) {
	var OperationExecutionAvailability = function() {
		Enum.call(this, [ "AVAILABLE", "DELETE_PENDING", "DELETED", "TIME_OUT_PENDING", "TIMED_OUT" ]);
	};
	stjs.extend(OperationExecutionAvailability, Enum, [ Enum ], function(constructor, prototype) {
		prototype.hasPrevious = function(availability) {
			throw new exceptions.RuntimeException("Unsupported method.");
		};
	}, {});
	return new OperationExecutionAvailability();
})