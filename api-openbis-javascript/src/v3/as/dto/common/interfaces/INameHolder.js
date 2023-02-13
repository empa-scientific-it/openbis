define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var INameHolder = function() {
	};
	stjs.extend(INameHolder, null, [], function(constructor, prototype) {
		prototype.getName = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return INameHolder;
})