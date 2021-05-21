/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/DateFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, DateFieldSearchCriteria, SearchFieldType) {
	var EventEntityRegistrationDateSearchCriteria = function() {
		DateFieldSearchCriteria.call(this, "event_entity_registration_date", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EventEntityRegistrationDateSearchCriteria, DateFieldSearchCriteria, [ DateFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventEntityRegistrationDateSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EventEntityRegistrationDateSearchCriteria;
})