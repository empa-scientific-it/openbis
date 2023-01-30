/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateSpacesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateSpacesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.update.UpdateSpacesOperationResult';
		prototype.getMessage = function() {
			return "UpdateSpacesOperationResult";
		};
	}, {});
	return UpdateSpacesOperationResult;
})