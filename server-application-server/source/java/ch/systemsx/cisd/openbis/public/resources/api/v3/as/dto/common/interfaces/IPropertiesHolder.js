define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IPropertiesHolder = function() {
	};
	stjs.extend(IPropertiesHolder, null, [], function(constructor, prototype) {
		prototype.getProperty = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setProperty = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getProperties = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setProperties = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IPropertiesHolder;
})