/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var LocationSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "location", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(LocationSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.LocationSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return LocationSearchCriteria;
})