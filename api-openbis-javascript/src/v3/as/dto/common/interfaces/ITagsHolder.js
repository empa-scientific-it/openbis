define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ITagsHolder = function() {
	};
	stjs.extend(ITagsHolder, null, [], function(constructor, prototype) {
		prototype.getTags = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return ITagsHolder;
})