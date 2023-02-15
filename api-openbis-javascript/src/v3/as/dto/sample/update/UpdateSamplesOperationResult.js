/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateSamplesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateSamplesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.UpdateSamplesOperationResult';
		prototype.getMessage = function() {
			return "UpdateSamplesOperationResult";
		};
	}, {});
	return UpdateSamplesOperationResult;
})