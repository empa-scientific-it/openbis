define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateVocabulariesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateVocabulariesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.CreateVocabulariesOperationResult';
		prototype.getMessage = function() {
			return "CreateVocabulariesOperationResult";
		};
	}, {});
	return CreateVocabulariesOperationResult;
})
