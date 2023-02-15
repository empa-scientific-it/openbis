/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetOperationExecutionsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetOperationExecutionsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.get.GetOperationExecutionsOperation';
		prototype.getMessage = function() {
			return "GetOperationExecutionsOperation";
		};
	}, {});
	return GetOperationExecutionsOperation;
})