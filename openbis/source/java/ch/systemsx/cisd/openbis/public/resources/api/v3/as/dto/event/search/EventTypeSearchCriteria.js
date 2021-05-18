/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var EventTypeSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "event_type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventTypeSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventTypeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventTypeSearchCriteria;
})