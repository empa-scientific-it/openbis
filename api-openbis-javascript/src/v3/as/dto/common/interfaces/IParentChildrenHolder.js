define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IParentChildrenHolder = function() {
	};
	stjs.extend(IParentChildrenHolder, null, [], function(constructor, prototype) {
		prototype.getChildren = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
		prototype.getParents = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IParentChildrenHolder;
})