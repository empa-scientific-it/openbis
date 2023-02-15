define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IRegistrationDateHolder = function() {
	};
	stjs.extend(IRegistrationDateHolder, null, [], function(constructor, prototype) {
		prototype.getRegistrationDate = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IRegistrationDateHolder;
})