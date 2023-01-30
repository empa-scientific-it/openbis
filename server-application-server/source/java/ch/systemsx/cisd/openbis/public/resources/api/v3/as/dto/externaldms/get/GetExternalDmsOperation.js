/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetExternalDmsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetExternalDmsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.get.GetExternalDmsOperation';
		prototype.getMessage = function() {
			return "GetExternalDmsOperation";
		};
	}, {});
	return GetExternalDmsOperation;
})