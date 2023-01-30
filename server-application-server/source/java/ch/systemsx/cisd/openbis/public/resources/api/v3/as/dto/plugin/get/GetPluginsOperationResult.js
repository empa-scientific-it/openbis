define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetPluginsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetPluginsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.get.GetPluginsOperationResult';
		prototype.getMessage = function() {
			return "GetPluginsOperationResult";
		};
	}, {});
	return GetPluginsOperationResult;
})
