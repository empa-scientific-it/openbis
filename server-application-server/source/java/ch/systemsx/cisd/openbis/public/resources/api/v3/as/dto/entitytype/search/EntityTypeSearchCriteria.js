define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", 
	"as/dto/common/search/CodesSearchCriteria", "as/dto/entitytype/search/EntityKindSearchCriteria" ], function(require,
		stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, CodesSearchCriteria, EntityKindSearchCriteria) {
	var EntityTypeSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(EntityTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.search.EntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withKind = function() {
			var EntityKindSearchCriteria = require("as/dto/entitytype/search/EntityKindSearchCriteria");
			return this.addCriteria(new EntityKindSearchCriteria());
		};
	}, {});

	return EntityTypeSearchCriteria;
})