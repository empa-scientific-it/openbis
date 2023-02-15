define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var AuthorizationGroupUpdate = function() {
		this.description = new FieldUpdateValue();
		this.userIds = new IdListUpdateValue();
	};
	stjs.extend(AuthorizationGroupUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.update.AuthorizationGroupUpdate';
		constructor.serialVersionUID = 1;
		prototype.groupId = null;
		prototype.description = null;
		prototype.userIds = null;

		prototype.getObjectId = function() {
			return this.getauthorizationGroupId();
		};
		prototype.getAuthorizationGroupId = function() {
			return this.groupId;
		};
		prototype.setAuthorizationGroupId = function(groupId) {
			this.groupId = groupId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getUserIds = function() {
			return this.userIds;
		};
		prototype.setUserIdActions = function(actions) {
			this.userIds.setActions(actions);
		};
	}, {
		AuthorizationGroupId : "IAuthorizationGroupId",
		groupId: {
			name: "IAuthorizationGroupId"
		},
		description : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		userIds : {
			name : "IdListUpdateValue",
			arguments : [ "IPersonId" ]
		}
	});
	return AuthorizationGroupUpdate;
})