define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IExperimentHolder = function() {
	};
	stjs.extend(IExperimentHolder, null, [], function(constructor, prototype) {
		prototype.getExperiment = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IExperimentHolder;
})