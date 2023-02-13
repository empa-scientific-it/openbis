define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IEntityTypeCreation = function() {
	};
	stjs.extend(IEntityTypeCreation, null, [], function(constructor, prototype) {
		prototype.getCode = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setCode = function(code) {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getDescription = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setDescription = function(description) {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getValidationPluginId = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setValidationPluginId = function(pluginId) {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getPropertyAssignments = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setPropertyAssignments = function(propertyAssignments) {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IEntityTypeCreation;
})
