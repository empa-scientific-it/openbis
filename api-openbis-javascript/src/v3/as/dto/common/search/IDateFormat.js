define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IDateFormat = function() {
	};
	stjs.extend(IDateFormat, null, [], function(constructor, prototype) {
		prototype.getFormat = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IDateFormat;
})