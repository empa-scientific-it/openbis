define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IOperationExecutionOptions = function() {
	};
	stjs.extend(IOperationExecutionOptions, null, [], function(constructor, prototype) {
		prototype.getDescription = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getNotification = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getAvailabilityTime = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getSummaryAvailabilityTime = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getDetailsAvailabilityTime = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IOperationExecutionOptions;
})