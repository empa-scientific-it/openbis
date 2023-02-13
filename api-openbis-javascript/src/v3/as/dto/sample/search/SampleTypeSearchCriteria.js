define([ "require", "stjs", "as/dto/entitytype/search/AbstractEntityTypeSearchCriteria", "as/dto/sample/search/ListableSampleTypeSearchCriteria",
		"as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria" ], function(require, stjs, AbstractEntityTypeSearchCriteria) {
	var SampleTypeSearchCriteria = function() {
		AbstractEntityTypeSearchCriteria.call(this);
	};
	stjs.extend(SampleTypeSearchCriteria, AbstractEntityTypeSearchCriteria, [ AbstractEntityTypeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withListable = function() {
			var ListableSampleTypeSearchCriteria = require("as/dto/sample/search/ListableSampleTypeSearchCriteria");
			return this.addCriteria(new ListableSampleTypeSearchCriteria());
		};
		prototype.withSemanticAnnotations = function() {
			var SemanticAnnotationSearchCriteria = require("as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria");
			return this.addCriteria(new SemanticAnnotationSearchCriteria());
		};
	}, {});

	return SampleTypeSearchCriteria;
})