/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeletePersonalAccessTokensOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeletePersonalAccessTokensOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.delete.DeletePersonalAccessTokensOperation';
		prototype.getMessage = function() {
			return "DeletePersonalAccessTokensOperation";
		};
	}, {});
	return DeletePersonalAccessTokensOperation;
})