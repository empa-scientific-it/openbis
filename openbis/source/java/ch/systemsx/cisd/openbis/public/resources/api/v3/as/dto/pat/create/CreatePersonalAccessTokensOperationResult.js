/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreatePersonalAccessTokensOperationResult = function(objectIds) {
		CreatePersonalAccessTokensOperationResult.call(this, objectIds);
	};
	stjs.extend(CreatePersonalAccessTokensOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.create.CreatePersonalAccessTokensOperationResult';
		prototype.getMessage = function() {
			return "CreatePersonalAccessTokensOperationResult";
		};
	}, {});
	return CreatePersonalAccessTokensOperationResult;
})