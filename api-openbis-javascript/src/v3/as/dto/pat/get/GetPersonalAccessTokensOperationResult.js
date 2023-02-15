/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetPersonalAccessTokensOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetPersonalAccessTokensOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.get.GetPersonalAccessTokensOperationResult';
		prototype.getMessage = function() {
			return "GetPersonalAccessTokensOperationResult";
		};
	}, {});
	return GetPersonalAccessTokensOperationResult;
})