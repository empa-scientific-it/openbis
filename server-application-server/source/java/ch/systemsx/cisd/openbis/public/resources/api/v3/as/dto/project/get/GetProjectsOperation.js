/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetProjectsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetProjectsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.get.GetProjectsOperation';
		prototype.getMessage = function() {
			return "GetProjectsOperation";
		};
	}, {});
	return GetProjectsOperation;
})