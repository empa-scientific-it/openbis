define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ISemanticAnnotationsHolder = function() {
	};
	stjs.extend(ISemanticAnnotationsHolder, null, [], function(constructor, prototype) {
		prototype.getSemanticAnnotations = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ISemanticAnnotationsHolder;
})