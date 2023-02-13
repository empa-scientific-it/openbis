/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetSemanticAnnotationsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetSemanticAnnotationsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.get.GetSemanticAnnotationsOperationResult';
		prototype.getMessage = function() {
			return "GetSemanticAnnotationsOperationResult";
		};
	}, {});
	return GetSemanticAnnotationsOperationResult;
})