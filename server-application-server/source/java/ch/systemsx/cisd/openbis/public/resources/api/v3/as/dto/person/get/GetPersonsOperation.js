define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetPersonsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetPersonsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.get.GetPersonsOperation';
		prototype.getMessage = function() {
			return "GetPersonsOperation";
		};
	}, {});
	return GetPersonsOperation;
})
