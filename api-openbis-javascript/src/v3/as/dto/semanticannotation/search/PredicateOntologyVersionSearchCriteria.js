/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var PredicateOntologyVersionSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "predicateOntologyVersion", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PredicateOntologyVersionSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.PredicateOntologyVersionSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PredicateOntologyVersionSearchCriteria;
})