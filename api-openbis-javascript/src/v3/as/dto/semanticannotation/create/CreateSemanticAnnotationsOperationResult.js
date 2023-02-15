/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateSemanticAnnotationsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateSemanticAnnotationsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.create.CreateSemanticAnnotationsOperationResult';
		prototype.getMessage = function() {
			return "CreateSemanticAnnotationsOperationResult";
		};
	}, {});
	return CreateSemanticAnnotationsOperationResult;
})