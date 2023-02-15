define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdatePluginsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdatePluginsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.update.UpdatePluginsOperationResult';
		prototype.getMessage = function() {
			return "UpdatePluginsOperationResult";
		};
	}, {});
	return UpdatePluginsOperationResult;
})
