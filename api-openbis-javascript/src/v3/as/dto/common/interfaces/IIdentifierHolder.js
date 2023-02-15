define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IIdentifierHolder = function() {
	};
	stjs.extend(IIdentifierHolder, null, [], function(constructor, prototype) {
		prototype.getIdentifier = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IIdentifierHolder;
})