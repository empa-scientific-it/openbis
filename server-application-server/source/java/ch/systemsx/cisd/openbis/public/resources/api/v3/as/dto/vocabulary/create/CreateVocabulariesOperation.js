define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateVocabulariesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateVocabulariesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.CreateVocabulariesOperation';
		prototype.getMessage = function() {
			return "CreateVocabulariesOperation";
		};
	}, {});
	return CreateVocabulariesOperation;
})

