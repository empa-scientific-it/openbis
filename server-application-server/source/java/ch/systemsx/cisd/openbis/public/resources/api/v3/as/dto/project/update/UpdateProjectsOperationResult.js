/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateProjectsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateProjectsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.update.UpdateProjectsOperationResult';
		prototype.getMessage = function() {
			return "UpdateProjectsOperationResult";
		};
	}, {});
	return UpdateProjectsOperationResult;
})