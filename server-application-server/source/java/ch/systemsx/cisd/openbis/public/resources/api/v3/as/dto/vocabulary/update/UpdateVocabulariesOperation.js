define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateVocabulariesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateVocabulariesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.UpdateVocabulariesOperation';
		prototype.getMessage = function() {
			return "UpdateVocabulariesOperation";
		};
	}, {});
	return UpdateVocabulariesOperation;
})
