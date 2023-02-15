define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IRegistratorHolder = function() {
	};
	stjs.extend(IRegistratorHolder, null, [], function(constructor, prototype) {
		prototype.getRegistrator = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IRegistratorHolder;
})