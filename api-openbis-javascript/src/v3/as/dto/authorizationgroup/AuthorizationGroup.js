define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var AuthorizationGroup = function() {
	};
	stjs.extend(AuthorizationGroup, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.AuthorizationGroup';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.users = null;
		prototype.roleAssignments = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.registrator = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getUsers = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasUsers()) {
				return this.users;
			} else {
				throw new exceptions.NotFetchedException("Users have not been fetched.");
			}
		};
		prototype.setUsers = function(users) {
			this.users = users;
		};
		prototype.getRoleAssignments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRoleAssignments()) {
				return this.roleAssignments;
			} else {
				throw new exceptions.NotFetchedException("RoleAssignments have not been fetched.");
			}
		};
		prototype.setRoleAssignments = function(roleAssignments) {
			this.roleAssignments = roleAssignments;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
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
		fetchOptions : "AuthorizationGroupFetchOptions",
		permId : "AuthorizationGroupPermId",
		users : {
			name : "List",
			arguments : [ "Person" ]
		},
		roleAssignments : {
			name : "List",
			arguments : [ "RoleAssignment" ]
		},
		registrationDate : "Date",
		modificationDate : "Date",
		registrator : "Person"
	});
	return AuthorizationGroup;
})