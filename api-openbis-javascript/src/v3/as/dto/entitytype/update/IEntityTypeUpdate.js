define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IEntityTypeUpdate = function() {
	};
	stjs.extend(IEntityTypeUpdate, null, [], function(constructor, prototype) {
		prototype.getTypeId = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setTypeId = function(typeId) {
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
	return IEntityTypeUpdate;
})
