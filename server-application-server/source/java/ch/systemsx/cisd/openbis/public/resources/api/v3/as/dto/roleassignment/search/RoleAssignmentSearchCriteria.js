define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/person/search/PersonSearchCriteria", 
         "as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria", "as/dto/space/search/SpaceSearchCriteria",
         "as/dto/project/search/ProjectSearchCriteria"], function(require, stjs,
		AbstractObjectSearchCriteria) {
	var RoleAssignmentSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(RoleAssignmentSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.search.RoleAssignmentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withUser = function() {
			var PersonSearchCriteria = require("as/dto/person/search/PersonSearchCriteria");
			return this.addCriteria(new PersonSearchCriteria());
		};
		prototype.withAuthorizationGroup = function() {
			var AuthorizationGroupSearchCriteria = require("as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria");
			return this.addCriteria(new AuthorizationGroupSearchCriteria());
		};
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("as/dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("as/dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return RoleAssignmentSearchCriteria;
})