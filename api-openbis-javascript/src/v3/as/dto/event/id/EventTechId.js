/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectTechId", "as/dto/event/id/IEventId" ], function(stjs, ObjectTechId, IEventId) {
	var EventTechId = function(techId) {
		ObjectTechId.call(this, techId);
	};
	stjs.extend(EventTechId, ObjectTechId, [ ObjectTechId, IEventId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.id.EventTechId';
		constructor.serialVersionUID = 1;
	}, {});
	return EventTechId;
})