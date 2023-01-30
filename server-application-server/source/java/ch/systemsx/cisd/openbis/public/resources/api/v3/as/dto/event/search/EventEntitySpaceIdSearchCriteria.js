/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EventEntitySpaceIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "event_entity_space_id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntitySpaceIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntitySpaceIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntitySpaceIdSearchCriteria;
})