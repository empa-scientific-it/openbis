/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", 
	"as/dto/common/search/CodesSearchCriteria", "as/dto/common/search/PermIdSearchCriteria",
		"as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var SpaceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SpaceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.search.SpaceSearchCriteria';
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
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SpaceSearchCriteria;
})