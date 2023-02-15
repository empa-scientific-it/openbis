define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IPropertyTypeHolder = function() {
	};
	stjs.extend(IPropertyTypeHolder, null, [], function(constructor, prototype) {
		prototype.getPropertyType = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IPropertyTypeHolder;
})