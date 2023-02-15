/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetMaterialTypesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetMaterialTypesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.get.GetMaterialTypesOperation';
		prototype.getMessage = function() {
			return "GetMaterialTypesOperation";
		};
	}, {});
	return GetMaterialTypesOperation;
})