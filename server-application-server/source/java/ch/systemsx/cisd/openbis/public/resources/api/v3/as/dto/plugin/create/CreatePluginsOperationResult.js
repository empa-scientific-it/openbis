define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreatePluginsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreatePluginsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.create.CreatePluginsOperationResult';
		prototype.getMessage = function() {
			return "CreatePluginsOperationResult";
		};
	}, {});
	return CreatePluginsOperationResult;
})
