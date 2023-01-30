/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var EntityKindSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "entityKind", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EntityKindSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.search.EntityKindSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EntityKindSearchCriteria;
})