define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/PermIdSearchCriteria", "as/dto/entitytype/search/EntityTypeSearchCriteria",
		"as/dto/property/search/PropertyTypeSearchCriteria", "as/dto/property/search/PropertyAssignmentSearchCriteria", "as/dto/semanticannotation/search/PredicateOntologyIdSearchCriteria",
		"as/dto/semanticannotation/search/PredicateOntologyVersionSearchCriteria", "as/dto/semanticannotation/search/PredicateAccessionIdSearchCriteria",
		"as/dto/semanticannotation/search/DescriptorOntologyIdSearchCriteria", "as/dto/semanticannotation/search/DescriptorOntologyVersionSearchCriteria",
		"as/dto/semanticannotation/search/DescriptorAccessionIdSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var SemanticAnnotationSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SemanticAnnotationSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.SemanticAnnotationSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withEntityType = function() {
			var EntityTypeSearchCriteria = require("as/dto/entitytype/search/EntityTypeSearchCriteria");
			return this.addCriteria(new EntityTypeSearchCriteria());
		};
		prototype.withPropertyType = function() {
			var PropertyTypeSearchCriteria = require("as/dto/property/search/PropertyTypeSearchCriteria");
			return this.addCriteria(new PropertyTypeSearchCriteria());
		};
		prototype.withPropertyAssignment = function() {
			var PropertyAssignmentSearchCriteria = require("as/dto/property/search/PropertyAssignmentSearchCriteria");
			return this.addCriteria(new PropertyAssignmentSearchCriteria());
		};
		prototype.withPredicateOntologyId = function() {
			var PredicateOntologyIdSearchCriteria = require("as/dto/semanticannotation/search/PredicateOntologyIdSearchCriteria");
			return this.addCriteria(new PredicateOntologyIdSearchCriteria());
		};
		prototype.withPredicateOntologyVersion = function() {
			var PredicateOntologyVersionSearchCriteria = require("as/dto/semanticannotation/search/PredicateOntologyVersionSearchCriteria");
			return this.addCriteria(new PredicateOntologyVersionSearchCriteria());
		};
		prototype.withPredicateAccessionId = function() {
			var PredicateAccessionIdSearchCriteria = require("as/dto/semanticannotation/search/PredicateAccessionIdSearchCriteria");
			return this.addCriteria(new PredicateAccessionIdSearchCriteria());
		};
		prototype.withDescriptorOntologyId = function() {
			var DescriptorOntologyIdSearchCriteria = require("as/dto/semanticannotation/search/DescriptorOntologyIdSearchCriteria");
			return this.addCriteria(new DescriptorOntologyIdSearchCriteria());
		};
		prototype.withDescriptorOntologyVersion = function() {
			var DescriptorOntologyVersionSearchCriteria = require("as/dto/semanticannotation/search/DescriptorOntologyVersionSearchCriteria");
			return this.addCriteria(new DescriptorOntologyVersionSearchCriteria());
		};
		prototype.withDescriptorAccessionId = function() {
			var DescriptorAccessionIdSearchCriteria = require("as/dto/semanticannotation/search/DescriptorAccessionIdSearchCriteria");
			return this.addCriteria(new DescriptorAccessionIdSearchCriteria());
		};
	}, {});

	return SemanticAnnotationSearchCriteria;
})