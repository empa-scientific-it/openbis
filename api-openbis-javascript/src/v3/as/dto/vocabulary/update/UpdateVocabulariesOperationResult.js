define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateVocabulariesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateVocabulariesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.UpdateVocabulariesOperationResult';
		prototype.getMessage = function() {
			return "UpdateVocabulariesOperationResult";
		};
	}, {});
	return UpdateVocabulariesOperationResult;
})
