/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var PredicateOntologyIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "predicateOntologyId", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PredicateOntologyIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.PredicateOntologyIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PredicateOntologyIdSearchCriteria;
})