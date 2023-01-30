/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateExternalDmsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateExternalDmsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.update.UpdateExternalDmsOperationResult';
		prototype.getMessage = function() {
			return "UpdateExternalDmsOperationResult";
		};
	}, {});
	return UpdateExternalDmsOperationResult;
})