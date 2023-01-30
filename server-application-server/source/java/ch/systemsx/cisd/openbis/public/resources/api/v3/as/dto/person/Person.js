/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Person = function() {
	};
	stjs.extend(Person, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.Person';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.userId = null;
		prototype.firstName = null;
		prototype.lastName = null;
		prototype.email = null;
		prototype.registrationDate = null;
		prototype.active = null;
		prototype.space = null;
		prototype.registrator = null;
		prototype.roleAssignments = null;
		prototype.webAppSettings = null;

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
		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getFirstName = function() {
			return this.firstName;
		};
		prototype.setFirstName = function(firstName) {
			this.firstName = firstName;
		};
		prototype.getLastName = function() {
			return this.lastName;
		};
		prototype.setLastName = function(lastName) {
			this.lastName = lastName;
		};
		prototype.getEmail = function() {
			return this.email;
		};
		prototype.setEmail = function(email) {
			this.email = email;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.isActive = function() {
			return this.active;
		};
		prototype.setActive = function(active) {
			this.active = active;
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

		prototype.getWebAppSettings = function(webAppId) {
			if (webAppId === undefined) {
				if (this.getFetchOptions()
						&& (this.getFetchOptions().hasAllWebAppSettings() || (this.getFetchOptions().getWebAppSettings() && Object.keys(this.getFetchOptions().getWebAppSettings()).length > 0))) {
					return this.webAppSettings;
				} else {
					throw new exceptions.NotFetchedException("Settings have not been fetched.");
				}
			} else {
				if (this.getFetchOptions() && (this.getFetchOptions().hasAllWebAppSettings() || this.getFetchOptions().hasWebAppSettings(webAppId))) {
					return this.webAppSettings ? this.webAppSettings[webAppId] : null;
				} else {
					throw new exceptions.NotFetchedException("Settings for web app '" + webAppId + "' have not been fetched.");
				}
			}
		};

		prototype.setWebAppSettings = function(webAppSettings) {
			this.webAppSettings = webAppSettings;
		};

	}, {
		fetchOptions : "PersonFetchOptions",
		permId : "PersonPermId",
		registrationDate : "Date",
		space : "Space",
		registrator : "Person",
		roleAssignments : {
			name : "List",
			arguments : [ "RoleAssignment" ]
		},
		webAppSettings : {
			name : "Map",
			arguments : [ "String", "WebAppSettings" ]
		}
	});
	return Person;
})