/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var CompleteSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "complete", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(CompleteSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.CompleteSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CompleteSearchCriteria;
})