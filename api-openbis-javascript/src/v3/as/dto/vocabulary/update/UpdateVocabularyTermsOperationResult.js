/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateVocabularyTermsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateVocabularyTermsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.UpdateVocabularyTermsOperationResult';
		prototype.getMessage = function() {
			return "UpdateVocabularyTermsOperationResult";
		};
	}, {});
	return UpdateVocabularyTermsOperationResult;
})