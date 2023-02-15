/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetDataSetTypesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetDataSetTypesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.get.GetDataSetTypesOperation';
		prototype.getMessage = function() {
			return "GetDataSetTypesOperation";
		};
	}, {});
	return GetDataSetTypesOperation;
})