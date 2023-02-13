/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var ExternalDmsTypeSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(ExternalDmsTypeSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.search.ExternalDmsTypeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return ExternalDmsTypeSearchCriteria;
})