define([ "stjs", "as/dto/common/id/ObjectTechId", "as/dto/roleassignment/id/IRoleAssignmentId" ], function(stjs, ObjectTechId, IRoleAssignmentId) {
	var RoleAssignmentTechId = function(techId) {
		ObjectTechId.call(this, techId);
	};
	stjs.extend(RoleAssignmentTechId, ObjectTechId, [ ObjectTechId, IRoleAssignmentId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.id.RoleAssignmentTechId';
		constructor.serialVersionUID = 1;
	}, {});
	return RoleAssignmentTechId;
})
