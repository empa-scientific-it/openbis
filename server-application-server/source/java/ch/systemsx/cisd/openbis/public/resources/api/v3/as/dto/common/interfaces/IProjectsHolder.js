define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IProjectsHolder = function() {
	};
	stjs.extend(IProjectsHolder, null, [], function(constructor, prototype) {
		prototype.getProjects = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IProjectsHolder;
})