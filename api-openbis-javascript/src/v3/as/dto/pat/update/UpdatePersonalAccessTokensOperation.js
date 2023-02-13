/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdatePersonalAccessTokensOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdatePersonalAccessTokensOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.update.UpdatePersonalAccessTokensOperation';
		prototype.getMessage = function() {
			return "UpdatePersonalAccessTokensOperation";
		};
	}, {});
	return UpdatePersonalAccessTokensOperation;
})