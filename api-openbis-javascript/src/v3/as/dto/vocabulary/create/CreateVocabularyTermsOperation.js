/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateVocabularyTermsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateVocabularyTermsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.CreateVocabularyTermsOperation';
		prototype.getMessage = function() {
			return "CreateVocabularyTermsOperation";
		};
	}, {});
	return CreateVocabularyTermsOperation;
})