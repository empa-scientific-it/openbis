/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateOperationExecutionsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateOperationExecutionsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.update.UpdateOperationExecutionsOperationResult';
		prototype.getMessage = function() {
			return "UpdateOperationExecutionsOperationResult";
		};
	}, {});
	return UpdateOperationExecutionsOperationResult;
})