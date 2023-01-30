/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteTagsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteTagsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.delete.DeleteTagsOperation';
		prototype.getMessage = function() {
			return "DeleteTagsOperation";
		};
	}, {});
	return DeleteTagsOperation;
})