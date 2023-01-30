define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IDescriptionHolder = function() {
	};
	stjs.extend(IDescriptionHolder, null, [], function(constructor, prototype) {
		prototype.getDescription = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IDescriptionHolder;
})