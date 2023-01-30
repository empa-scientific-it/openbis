define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ICodeHolder = function() {
	};
	stjs.extend(ICodeHolder, null, [], function(constructor, prototype) {
		prototype.getCode = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ICodeHolder;
})