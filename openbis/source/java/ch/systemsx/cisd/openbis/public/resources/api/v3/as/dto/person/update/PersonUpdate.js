define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue", "as/dto/webapp/update/WebAppSettingsUpdateValue" ], function(stjs, FieldUpdateValue,
		IdListUpdateValue, WebAppSettingsUpdateValue) {
	var PersonUpdate = function() {
		this.spaceId = new FieldUpdateValue();
		this.active = new FieldUpdateValue();
	};
	stjs.extend(PersonUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.update.PersonUpdate';
		constructor.serialVersionUID = 1;
		prototype.userId = null;
		prototype.spaceId = null;
		prototype.webAppSettings = null;
		prototype.active = null;

		prototype.getObjectId = function() {
			return this.getUserId();
		};
		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId.setValue(spaceId);
		};
		prototype.getWebAppSettings = function(webAppId) {
			if (webAppId === undefined) {
				return this.webAppSettings;
			} else {
				if (this.webAppSettings == null) {
					this.webAppSettings = {};
				}

				var updateValue = this.webAppSettings[webAppId];

				if (updateValue == null) {
					updateValue = new WebAppSettingsUpdateValue();
					this.webAppSettings[webAppId] = updateValue;
				}

				return updateValue;
			}
		};
		prototype.isActive = function() {
			return this.active;
		};
		prototype.activate = function() {
			this.active.setValue(true);
		};
		prototype.deactivate = function() {
			this.active.setValue(false);
		};
	}, {
		userId : "IPersonId",
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		},
		webAppSettings : {
			name : "Map",
			arguments : [ "String", "WebAppSettingsUpdateValue" ]
		},
		active : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
	});
	return PersonUpdate;
})