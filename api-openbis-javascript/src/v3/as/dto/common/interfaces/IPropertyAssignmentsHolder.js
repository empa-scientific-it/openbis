define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IPropertyAssignmentsHolder = function() {
	};
	stjs.extend(IPropertyAssignmentsHolder, null, [], function(constructor, prototype) {
		prototype.getPropertyAssignments = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IPropertyAssignmentsHolder;
})