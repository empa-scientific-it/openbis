/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var StatusSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "status", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(StatusSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.StatusSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StatusSearchCriteria;
})