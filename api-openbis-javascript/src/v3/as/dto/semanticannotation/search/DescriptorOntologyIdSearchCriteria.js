/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var DescriptorOntologyIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "descriptorOntologyId", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(DescriptorOntologyIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.DescriptorOntologyIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return DescriptorOntologyIdSearchCriteria;
})