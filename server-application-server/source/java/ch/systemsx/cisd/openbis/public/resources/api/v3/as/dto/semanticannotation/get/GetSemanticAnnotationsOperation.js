/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetSemanticAnnotationsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetSemanticAnnotationsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.get.GetSemanticAnnotationsOperation';
		prototype.getMessage = function() {
			return "GetSemanticAnnotationsOperation";
		};
	}, {});
	return GetSemanticAnnotationsOperation;
})