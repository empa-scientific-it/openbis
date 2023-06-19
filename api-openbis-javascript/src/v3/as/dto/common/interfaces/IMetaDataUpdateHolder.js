define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IMetaDataUpdateHolder = function() {
	};
	stjs.extend(IMetaDataUpdateHolder, null, [], function(constructor, prototype) {
		prototype.getMetaData = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IMetaDataUpdateHolder;
})