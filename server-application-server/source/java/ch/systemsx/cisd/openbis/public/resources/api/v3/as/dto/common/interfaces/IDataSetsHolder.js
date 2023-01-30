define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IDataSetsHolder = function() {
	};
	stjs.extend(IDataSetsHolder, null, [], function(constructor, prototype) {
		prototype.getDataSets = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IDataSetsHolder;
})