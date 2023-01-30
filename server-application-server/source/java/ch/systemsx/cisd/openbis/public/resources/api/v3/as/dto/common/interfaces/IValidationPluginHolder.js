define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IValidationPluginHolder = function() {
	};
	stjs.extend(IValidationPluginHolder, null, [], function(constructor, prototype) {
		prototype.getValidationPlugin = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IValidationPluginHolder;
})