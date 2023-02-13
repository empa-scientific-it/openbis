/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdatePersonalAccessTokensOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdatePersonalAccessTokensOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.update.UpdatePersonalAccessTokensOperationResult';
		prototype.getMessage = function() {
			return "UpdatePersonalAccessTokensOperationResult";
		};
	}, {});
	return UpdatePersonalAccessTokensOperationResult;
})