/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var DescriptorOntologyVersionSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "descriptorOntologyVersion", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(DescriptorOntologyVersionSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.DescriptorOntologyVersionSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return DescriptorOntologyVersionSearchCriteria;
})