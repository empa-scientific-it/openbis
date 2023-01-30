/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetQueriesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetQueriesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.get.GetQueriesOperationResult';
		prototype.getMessage = function() {
			return "GetQueriesOperationResult";
		};
	}, {});
	return GetQueriesOperationResult;
})