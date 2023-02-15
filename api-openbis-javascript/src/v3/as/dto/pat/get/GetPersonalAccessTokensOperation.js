/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetPersonalAccessTokensOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetPersonalAccessTokensOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.get.GetPersonalAccessTokensOperation';
		prototype.getMessage = function() {
			return "GetPersonalAccessTokensOperation";
		};
	}, {});
	return GetPersonalAccessTokensOperation;
})