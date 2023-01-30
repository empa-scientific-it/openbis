define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ILabelHolder = function() {
	};
	stjs.extend(ILabelHolder, null, [], function(constructor, prototype) {
		prototype.getLabel = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ILabelHolder;
})