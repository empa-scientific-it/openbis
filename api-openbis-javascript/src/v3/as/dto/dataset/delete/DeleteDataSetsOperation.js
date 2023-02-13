/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteDataSetsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteDataSetsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DeleteDataSetsOperation';
		prototype.getMessage = function() {
			return "DeleteDataSetsOperation";
		};
	}, {});
	return DeleteDataSetsOperation;
})