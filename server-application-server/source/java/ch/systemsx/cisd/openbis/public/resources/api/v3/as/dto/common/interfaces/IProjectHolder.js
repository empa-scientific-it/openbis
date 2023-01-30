define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IProjectHolder = function() {
	};
	stjs.extend(IProjectHolder, null, [], function(constructor, prototype) {
		prototype.getProject = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IProjectHolder;
})