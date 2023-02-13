define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetVocabulariesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetVocabulariesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.get.GetVocabulariesOperation';
		prototype.getMessage = function() {
			return "GetVocabulariesOperation";
		};
	}, {});
	return GetVocabulariesOperation;
})
