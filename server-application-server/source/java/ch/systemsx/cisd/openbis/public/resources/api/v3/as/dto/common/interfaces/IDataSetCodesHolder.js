define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IDataSetCodesHolder = function() {
	};
	stjs.extend(IDataSetCodesHolder, null, [], function(constructor, prototype) {
		prototype.getDataSetCodes = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IDataSetCodesHolder;
})