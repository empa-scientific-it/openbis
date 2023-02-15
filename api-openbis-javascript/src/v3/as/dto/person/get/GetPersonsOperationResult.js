define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetPersonsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetPersonsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.get.GetPersonsOperationResult';
		prototype.getMessage = function() {
			return "GetPersonsOperationResult";
		};
	}, {});
	return GetPersonsOperationResult;
})
