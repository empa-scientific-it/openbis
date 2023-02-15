define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IPermIdHolder = function() {
	};
	stjs.extend(IPermIdHolder, null, [], function(constructor, prototype) {
		prototype.getPermId = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IPermIdHolder;
})