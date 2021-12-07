define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions",
         "as/dto/roleassignment/fetchoptions/RoleAssignmentFetchOptions",
         "as/dto/authorizationgroup/fetchoptions/AuthorizationGroupSortOptions"], function(require, stjs, FetchOptions) {
  var AuthorizationGroupFetchOptions = function() {
  };
  stjs.extend(AuthorizationGroupFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.registrator = null;
    prototype.users = null;
    prototype.roleAssignments = null;
    prototype.sort = null;

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
    prototype.withUsers = function() {
      if (this.users == null) {
        var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
        this.users = new PersonFetchOptions();
      }
      return this.users;
    };
    prototype.withUsersUsing = function(fetchOptions) {
      return this.users = fetchOptions;
    };
    prototype.hasUsers = function() {
      return this.users != null;
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
    prototype.sortBy = function() {
      if (this.sort == null) {
        var AuthorizationSortOptions = require("as/dto/authorizationgroup/fetchoptions/AuthorizationGroupSortOptions");
        this.sort = new AuthorizationSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    registrator : "PersonFetchOptions",
    users : "PersonFetchOptions",
    roleAssignments : "RoleAssignmentFetchOptions",
    sort : "AuthorizationGroupSortOptions"
  });
  return AuthorizationGroupFetchOptions;
})