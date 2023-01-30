/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteTagsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteTagsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.delete.DeleteTagsOperationResult';
		prototype.getMessage = function() {
			return "DeleteTagsOperationResult";
		};
	}, {});
	return DeleteTagsOperationResult;
})