/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/space/fetchoptions/SpaceFetchOptions", "as/dto/person/fetchoptions/PersonSortOptions",
		"as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions", "as/dto/webapp/fetchoptions/WebAppSettingsFetchOptions" ], function(require, stjs, FetchOptions) {
	var PersonFetchOptions = function() {
	};
	stjs.extend(PersonFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.fetchoptions.PersonFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.space = null;
		prototype.registrator = null;
		prototype.roleAssignments = null;
		prototype.webAppSettings = null;
		prototype.allWebAppSettings = false;
		prototype.sort = null;
		prototype.withSpace = function() {
			if (this.space == null) {
				var SpaceFetchOptions = require("as/dto/space/fetchoptions/SpaceFetchOptions");
				this.space = new SpaceFetchOptions();
			}
			return this.space;
		};
		prototype.withSpaceUsing = function(fetchOptions) {
			return this.space = fetchOptions;
		};
		prototype.hasSpace = function() {
			return this.space != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withRoleAssignments = function() {
			if (this.roleAssignments == null) {
				var RoleAssignmentsFetchOptions = require("as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions");
				this.roleAssignments = new RoleAssignmentsFetchOptions();
			}
			return this.roleAssignments;
		};
		prototype.withRoleAssignmentsUsing = function(fetchOptions) {
			return this.roleAssignments = fetchOptions;
		};
		prototype.hasRoleAssignments = function() {
			return this.roleAssignments != null;
		};

		prototype.withWebAppSettings = function(webAppId) {
			if (this.webAppSettings == null) {
				this.webAppSettings = {};
			}

			var webAppFo = this.webAppSettings[webAppId];

			if (webAppFo == null) {
				var WebAppSettingsFetchOptions = require("as/dto/webapp/fetchoptions/WebAppSettingsFetchOptions");
				webAppFo = new WebAppSettingsFetchOptions();
				this.webAppSettings[webAppId] = webAppFo;
			}

			return webAppFo;
		};

		prototype.hasWebAppSettings = function(webAppId) {
			return this.webAppSettings != null && this.webAppSettings[webAppId] != null;
		};

		prototype.withWebAppSettingsUsing = function(webAppSettings) {
			return this.webAppSettings = webAppSettings;
		};

		prototype.getWebAppSettings = function() {
			return this.webAppSettings;
		};

		prototype.setWebAppSettings = function(webAppSettings) {
			this.webAppSettings = webAppSettings;
		};

		prototype.withAllWebAppSettings = function() {
			this.allWebAppSettings = true;
		};

		prototype.hasAllWebAppSettings = function() {
			return this.allWebAppSettings;
		};

		prototype.withAllWebAppSettingsUsing = function(allWebAppSettings) {
			this.allWebAppSettings = allWebAppSettings;
		};

		prototype.getAllWebAppSettings = function() {
			return this.allWebAppSettings;
		};

		prototype.setAllWebAppSettings = function(allWebAppSettings) {
			this.allWebAppSettings = allWebAppSettings;
		};

		prototype.sortBy = function() {
			if (this.sort == null) {
				var PersonSortOptions = require("as/dto/person/fetchoptions/PersonSortOptions");
				this.sort = new PersonSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		space : "SpaceFetchOptions",
		registrator : "PersonFetchOptions",
		roleAssignments : "RoleAssignmentFetchOptions",
		webAppSettings : {
			name : "Map",
			arguments : [ "String", "WebAppSettingsFetchOptions" ]
		},
		sort : "PersonSortOptions"
	});
	return PersonFetchOptions;
})