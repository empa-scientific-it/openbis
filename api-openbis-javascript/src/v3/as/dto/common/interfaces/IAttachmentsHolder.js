define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IAttachmentsHolder = function() {
	};
	stjs.extend(IAttachmentsHolder, null, [], function(constructor, prototype) {
		prototype.getAttachments = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IAttachmentsHolder;
})