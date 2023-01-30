define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IEntityTypeHolder = function() {
	};
	stjs.extend(IEntityTypeHolder, null, [], function(constructor, prototype) {
		prototype.getType = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IEntityTypeHolder;
})