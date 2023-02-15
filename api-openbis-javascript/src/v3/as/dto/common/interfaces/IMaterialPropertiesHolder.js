define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IMaterialPropertiesHolder = function() {
	};
	stjs.extend(IMaterialPropertiesHolder, null, [], function(constructor, prototype) {
		prototype.getMaterialProperty = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setMaterialProperty = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getMaterialProperties = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.setMaterialProperties = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IMaterialPropertiesHolder;
})