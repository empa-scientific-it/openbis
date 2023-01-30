define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetPluginsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetPluginsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.get.GetPluginsOperation';
		prototype.getMessage = function() {
			return "GetPluginsOperation";
		};
	}, {});
	return GetPluginsOperation;
})
