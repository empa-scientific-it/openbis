define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ISamplesHolder = function() {
	};
	stjs.extend(ISamplesHolder, null, [], function(constructor, prototype) {
		prototype.getSamples = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ISamplesHolder;
})