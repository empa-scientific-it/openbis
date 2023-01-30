define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetPropertyTypesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetPropertyTypesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.get.GetPropertyTypesOperation';
		prototype.getMessage = function() {
			return "GetPropertyTypesOperation";
		};
	}, {});
	return GetPropertyTypesOperation;
})
