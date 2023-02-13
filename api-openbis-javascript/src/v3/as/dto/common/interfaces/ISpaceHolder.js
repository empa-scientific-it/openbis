define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ISpaceHolder = function() {
	};
	stjs.extend(ISpaceHolder, null, [], function(constructor, prototype) {
		prototype.getSpace = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ISpaceHolder;
})