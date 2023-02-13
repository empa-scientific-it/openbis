define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ISampleHolder = function() {
	};
	stjs.extend(ISampleHolder, null, [], function(constructor, prototype) {
		prototype.getSample = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ISampleHolder;
})