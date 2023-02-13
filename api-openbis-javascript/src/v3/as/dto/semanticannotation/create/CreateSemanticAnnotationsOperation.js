/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateSemanticAnnotationsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateSemanticAnnotationsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.create.CreateSemanticAnnotationsOperation';
		prototype.getMessage = function() {
			return "CreateSemanticAnnotationsOperation";
		};
	}, {});
	return CreateSemanticAnnotationsOperation;
})