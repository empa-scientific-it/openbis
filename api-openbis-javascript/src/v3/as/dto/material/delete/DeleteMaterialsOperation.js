/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteMaterialsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteMaterialsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.delete.DeleteMaterialsOperation';
		prototype.getMessage = function() {
			return "DeleteMaterialsOperation";
		};
	}, {});
	return DeleteMaterialsOperation;
})