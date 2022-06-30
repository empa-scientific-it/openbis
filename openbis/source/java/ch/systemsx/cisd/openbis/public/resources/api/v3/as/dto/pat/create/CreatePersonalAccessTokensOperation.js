/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreatePersonalAccessTokensOperation = function(creations) {
		CreatePersonalAccessTokensOperation.call(this, creations);
	};
	stjs.extend(CreatePersonalAccessTokensOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.create.CreatePersonalAccessTokensOperation';
		prototype.getMessage = function() {
			return "CreatePersonalAccessTokensOperation";
		};
	}, {});
	return CreatePersonalAccessTokensOperation;
})