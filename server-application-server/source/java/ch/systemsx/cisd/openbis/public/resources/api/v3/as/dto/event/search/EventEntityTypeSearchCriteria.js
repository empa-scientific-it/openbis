/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var EventEntityTypeSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "event_entity_type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntityTypeSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntityTypeSearchCriteria;
})