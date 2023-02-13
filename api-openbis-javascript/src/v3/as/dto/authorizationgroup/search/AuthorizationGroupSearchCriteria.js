define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", 
		"as/dto/common/search/CodesSearchCriteria", "as/dto/common/search/PermIdSearchCriteria", "as/dto/person/search/PersonSearchCriteria" ], function(require, stjs,
		AbstractObjectSearchCriteria) {
	var AuthorizationGroupSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(AuthorizationGroupSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withUser = function() {
			var PersonSearchCriteria = require("as/dto/person/search/PersonSearchCriteria");
			return this.addCriteria(new PersonSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AuthorizationGroupSearchCriteria;
})