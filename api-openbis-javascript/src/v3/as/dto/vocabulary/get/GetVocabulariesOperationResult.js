define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetVocabulariesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetVocabulariesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.get.GetVocabulariesOperationResult';
		prototype.getMessage = function() {
			return "GetVocabulariesOperationResult";
		};
	}, {});
	return GetVocabulariesOperationResult;
})
