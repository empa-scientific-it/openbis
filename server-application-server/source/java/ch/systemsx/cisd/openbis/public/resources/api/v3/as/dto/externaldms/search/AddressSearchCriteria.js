/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AddressSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "address", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(AddressSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.search.AddressSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AddressSearchCriteria;
})