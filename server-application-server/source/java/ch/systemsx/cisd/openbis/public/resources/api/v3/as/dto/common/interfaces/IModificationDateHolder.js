define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IModificationDateHolder = function() {
	};
	stjs.extend(IModificationDateHolder, null, [], function(constructor, prototype) {
		prototype.getModificationDate = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IModificationDateHolder;
})