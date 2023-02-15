define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdatePluginsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdatePluginsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.update.UpdatePluginsOperation';
		prototype.getMessage = function() {
			return "UpdatePluginsOperation";
		};
	}, {});
	return UpdatePluginsOperation;
})
