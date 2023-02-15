/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var DeleteObjectsOperationResult = function() {
	};
	stjs.extend(DeleteObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.delete.DeleteObjectsOperationResult';
	}, {});
	return DeleteObjectsOperationResult;
})