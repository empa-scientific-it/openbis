define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var RoleAssignmentCreation = function() {
	};
	stjs.extend(RoleAssignmentCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.create.RoleAssignmentCreation';
		constructor.serialVersionUID = 1;
		prototype.userId = null;
		prototype.authorizationGroupId = null;
		prototype.role = null;
		prototype.spaceId = null;
		prototype.projectId = null;
		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getAuthorizationGroupId = function() {
			return this.authorizationGroupId;
		};
		prototype.setAuthorizationGroupId = function(authorizationGroupId) {
			this.authorizationGroupId = authorizationGroupId;
		};
		prototype.getRole = function() {
			return this.role;
		};
		prototype.setRole = function(role) {
			this.role = role;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId = spaceId;
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId = projectId;
		};
	}, {
		userId : "IPersonId",
		authorizationGroupId : "IAuthorizationGroupId",
		role : "Role",
		spaceId : "ISpaceId",
		projectId : "IProjectId"
	});
	return RoleAssignmentCreation;
})