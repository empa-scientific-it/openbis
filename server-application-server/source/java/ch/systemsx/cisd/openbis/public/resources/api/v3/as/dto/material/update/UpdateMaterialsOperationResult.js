/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateMaterialsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateMaterialsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.update.UpdateMaterialsOperationResult';
		prototype.getMessage = function() {
			return "UpdateMaterialsOperationResult";
		};
	}, {});
	return UpdateMaterialsOperationResult;
})