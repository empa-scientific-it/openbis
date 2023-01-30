/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EventEntitySpaceSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "event_entity_space", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntitySpaceSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntitySpaceSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntitySpaceSearchCriteria;
})