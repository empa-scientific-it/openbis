/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteExternalDmsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteExternalDmsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.delete.DeleteExternalDmsOperation';
		prototype.getMessage = function() {
			return "DeleteExternalDmsOperation";
		};
	}, {});
	return DeleteExternalDmsOperation;
})