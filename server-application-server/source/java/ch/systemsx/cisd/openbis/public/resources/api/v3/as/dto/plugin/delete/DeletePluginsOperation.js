define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeletePluginsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeletePluginsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.delete.DeletePluginsOperation';
		prototype.getMessage = function() {
			return "DeletePluginsOperation";
		};
	}, {});
	return DeletePluginsOperation;
})
