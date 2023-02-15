define([ "stjs" ], function(stjs) {
	var AuthorizationGroupCreation = function() {
	};
	stjs.extend(AuthorizationGroupCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.create.AuthorizationGroupCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
		prototype.userIds = null;

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
		prototype.getUserIds = function() {
			return this.userIds;
		};
		prototype.setUserIds = function(userIds) {
			this.userIds = userIds;
		};
	}, {
		userIds : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return AuthorizationGroupCreation;
})