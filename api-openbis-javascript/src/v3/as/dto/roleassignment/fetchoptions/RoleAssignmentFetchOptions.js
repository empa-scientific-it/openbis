define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", 
         "as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions", "as/dto/space/fetchoptions/SpaceFetchOptions", 
         "as/dto/project/fetchoptions/ProjectFetchOptions", "as/dto/roleassignment/fetchoptions/RoleAssignmentSortOptions" ], function(require, stjs,
		FetchOptions) {
	var RoleAssignmentFetchOptions = function() {
	};
	stjs.extend(RoleAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
	    prototype.registrator = null;
		prototype.user = null;
		prototype.authorizationGroup = null;
		prototype.space = null;
		prototype.sort = null;
		prototype.project = null;
		
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
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
		prototype.withUser = function() {
			if (this.user == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.user = new PersonFetchOptions();
			}
			return this.user;
		}
		prototype.withUserUsing = function(fetchOptions) {
			return this.user = fetchOptions;
		}
		prototype.hasUser = function() {
			return this.user != null;
		}
		prototype.withAuthorizationGroup = function() {
			if (this.authorizationGroup == null) {
				var AuthorizationGroupFetchOptions = require("as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions");
				this.authorizationGroup = new AuthorizationGroupFetchOptions();
			}
			return this.authorizationGroup;
		}
		prototype.withAuthorizationGroupUsing = function(fetchOptions) {
			return this.authorizationGroup = fetchOptions;
		}
		prototype.hasAuthorizationGroup = function() {
			return this.authorizationGroup != null;
		}
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
		prototype.withProject = function() {
			if (this.project == null) {
				var ProjectFetchOptions = require("as/dto/project/fetchoptions/ProjectFetchOptions");
				this.project = new ProjectFetchOptions();
			}
			return this.project;
		};
		prototype.withProjectUsing = function(fetchOptions) {
			return this.project = fetchOptions;
		};
		prototype.hasProject = function() {
			return this.project != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var RoleAssignmentSortOptions = require("as/dto/roleassignment/fetchoptions/RoleAssignmentSortOptions");
				this.sort = new RoleAssignmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		user : "PersonFetchOptions",
		authorizationGroup : "AuthorizationGroupFetchOptions",
		space : "SpaceFetchOptions",
		project : "ProjectFetchOptions",
		sort : "RoleAssignmentSortOptions"
	});
	return RoleAssignmentFetchOptions;
})