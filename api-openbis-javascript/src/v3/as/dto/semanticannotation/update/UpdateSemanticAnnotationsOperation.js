/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateSemanticAnnotationsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateSemanticAnnotationsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.update.UpdateSemanticAnnotationsOperation';
		prototype.getMessage = function() {
			return "UpdateSemanticAnnotationsOperation";
		};
	}, {});
	return UpdateSemanticAnnotationsOperation;
})