/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateDataSetsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateDataSetsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.UpdateDataSetsOperationResult';
		prototype.getMessage = function() {
			return "UpdateDataSetsOperationResult";
		};
	}, {});
	return UpdateDataSetsOperationResult;
})