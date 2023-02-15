/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetSampleTypesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetSampleTypesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.get.GetSampleTypesOperation';
		prototype.getMessage = function() {
			return "GetSampleTypesOperation";
		};
	}, {});
	return GetSampleTypesOperation;
})