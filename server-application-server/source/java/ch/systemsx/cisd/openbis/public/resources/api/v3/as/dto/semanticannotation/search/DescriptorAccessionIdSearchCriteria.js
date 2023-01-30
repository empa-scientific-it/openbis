/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var DescriptorAccessionIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "descriptorAccessionId", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(DescriptorAccessionIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.DescriptorAccessionIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return DescriptorAccessionIdSearchCriteria;
})