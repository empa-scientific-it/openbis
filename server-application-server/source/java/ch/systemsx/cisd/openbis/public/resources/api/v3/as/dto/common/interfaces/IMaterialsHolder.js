define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IMaterialsHolder = function() {
	};
	stjs.extend(IMaterialsHolder, null, [], function(constructor, prototype) {
		prototype.getMaterials = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IMaterialsHolder;
})