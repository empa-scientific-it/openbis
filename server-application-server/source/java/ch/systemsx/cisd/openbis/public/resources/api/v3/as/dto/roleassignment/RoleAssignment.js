define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var RoleAssignment = function() {
	};
	stjs.extend(RoleAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.RoleAssignment';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.user = null;
		prototype.authorizationGroup = null;
		prototype.id = null;
		prototype.role = null;
		prototype.roleLevel = null;
		prototype.space = null;
		prototype.project = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getUser = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasUser()) {
				return this.user;
			}
			throw new exceptions.NotFetchedException("User has not been fetched.");
		}
		prototype.setUser = function(user) {
			this.user = user;
		}
		prototype.getAuthorizationGroup = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasAuthorizationGroup()) {
				return this.authorizationGroup;
			}
			throw new exceptions.NotFetchedException("Authorization group has not been fetched.");
		}
		prototype.setAuthorizationGroup = function(authorizationGroup) {
			this.authorizationGroup = authorizationGroup;
		}
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getRole = function() {
			return this.role;
		};
		prototype.setRole = function(role) {
			this.role = role;
		};
		prototype.getRoleLevel = function() {
			return this.roleLevel;
		};
		prototype.setRoleLevel = function(roleLevel) {
			this.roleLevel = roleLevel;
		};
		prototype.getSpace = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSpace()) {
				return this.space;
			} else {
				throw new exceptions.NotFetchedException("Space has not been fetched.");
			}
		};
		prototype.setSpace = function(space) {
			this.space = space;
		};
		prototype.getProject = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProject()) {
				return this.project;
			} else {
				throw new exceptions.NotFetchedException("Project has not been fetched.");
			}
		};
		prototype.setProject = function(project) {
			this.project = project;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
	}, {
		fetchOptions : "RoleAssignmentFetchOptions",
		id : "IRoleAssignmentId",
		user : "Person",
		authorizationGroup : "AuthorizationGroup",
		role : "Role",
		roleLevel : "RoleLevel",
		space : "Space",
		project : "Project",
		registrationDate : "Date",
		registrator : "Person"
	});
	return RoleAssignment;
})