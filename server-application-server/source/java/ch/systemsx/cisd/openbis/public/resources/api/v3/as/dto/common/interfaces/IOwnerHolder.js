define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IOwnerHolder = function() {
	};
	stjs.extend(IOwnerHolder, null, [], function(constructor, prototype) {
		prototype.getOwner = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IOwnerHolder;
})