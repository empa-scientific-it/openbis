define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IModifierHolder = function() {
	};
	stjs.extend(IModifierHolder, null, [], function(constructor, prototype) {
		prototype.getModifier = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IModifierHolder;
})