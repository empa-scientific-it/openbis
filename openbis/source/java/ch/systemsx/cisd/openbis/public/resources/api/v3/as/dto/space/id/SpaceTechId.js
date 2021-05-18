/**
 * Space tech id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectTechId", "as/dto/space/id/ISpaceId" ], function(stjs, ObjectTechId, ISpaceId) {
	/**
	 * @param techId Space tech id, e.g. 123.
	 */
	var SpaceTechId = function(techId) {
		ObjectTechId.call(this, techId);
	};
	stjs.extend(SpaceTechId, ObjectTechId, [ ObjectTechId, ISpaceId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.id.SpaceTechId';
		constructor.serialVersionUID = 1;
	}, {});
	return SpaceTechId;
})