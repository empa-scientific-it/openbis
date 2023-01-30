/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EventEntityProjectSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "event_entity_project", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntityProjectSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntityProjectSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntityProjectSearchCriteria;
})