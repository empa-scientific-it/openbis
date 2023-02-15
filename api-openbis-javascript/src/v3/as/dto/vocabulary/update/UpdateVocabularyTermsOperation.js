/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateVocabularyTermsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateVocabularyTermsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.UpdateVocabularyTermsOperation';
		prototype.getMessage = function() {
			return "UpdateVocabularyTermsOperation";
		};
	}, {});
	return UpdateVocabularyTermsOperation;
})