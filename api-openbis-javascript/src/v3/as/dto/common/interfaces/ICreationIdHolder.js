define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ICreationIdHolder = function() {
	};
	stjs.extend(ICreationIdHolder, null, [], function(constructor, prototype) {
		prototype.getCreationId = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ICreationIdHolder;
})