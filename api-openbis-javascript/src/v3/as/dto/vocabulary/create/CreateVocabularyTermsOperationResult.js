/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateVocabularyTermsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateVocabularyTermsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.CreateVocabularyTermsOperationResult';
		prototype.getMessage = function() {
			return "CreateVocabularyTermsOperationResult";
		};
	}, {});
	return CreateVocabularyTermsOperationResult;
})