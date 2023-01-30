define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IExperimentsHolder = function() {
	};
	stjs.extend(IExperimentsHolder, null, [], function(constructor, prototype) {
		prototype.getExperiments = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IExperimentsHolder;
})