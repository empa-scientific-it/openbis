/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/IdsSearchCriteria", "as/dto/common/search/CodeSearchCriteria", "as/dto/common/search/CodesSearchCriteria",
		"as/dto/property/search/PropertyAssignmentSearchCriteria", "as/dto/common/search/PermIdSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs,
		AbstractObjectSearchCriteria) {
	var AbstractEntityTypeSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(AbstractEntityTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.search.AbstractEntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withIds = function() {
			var IdsSearchCriteria = require("as/dto/common/search/IdsSearchCriteria");
			return this.addCriteria(new IdsSearchCriteria());
		};
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
		prototype.withPropertyAssignments = function() {
			var PropertyAssignmentSearchCriteria = require("as/dto/property/search/PropertyAssignmentSearchCriteria");
			return this.addCriteria(new PropertyAssignmentSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractEntityTypeSearchCriteria;
})