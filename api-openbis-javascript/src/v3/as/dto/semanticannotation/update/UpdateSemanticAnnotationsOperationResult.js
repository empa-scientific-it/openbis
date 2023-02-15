/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateSemanticAnnotationsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateSemanticAnnotationsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.update.UpdateSemanticAnnotationsOperationResult';
		prototype.getMessage = function() {
			return "UpdateSemanticAnnotationsOperationResult";
		};
	}, {});
	return UpdateSemanticAnnotationsOperationResult;
})