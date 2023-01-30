/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateProjectsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateProjectsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.update.UpdateProjectsOperation';
		prototype.getMessage = function() {
			return "UpdateProjectsOperation";
		};
	}, {});
	return UpdateProjectsOperation;
})