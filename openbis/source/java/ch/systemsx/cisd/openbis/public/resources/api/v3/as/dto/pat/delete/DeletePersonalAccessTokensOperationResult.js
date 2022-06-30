/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeletePersonalAccessTokensOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeletePersonalAccessTokensOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.delete.DeletePersonalAccessTokensOperationResult';
		prototype.getMessage = function() {
			return "DeletePersonalAccessTokensOperationResult";
		};
	}, {});
	return DeletePersonalAccessTokensOperationResult;
})