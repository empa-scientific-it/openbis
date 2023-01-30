/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectTechId", "as/dto/deletion/id/IDeletionId" ], function(stjs, ObjectTechId, IDeletionId) {
	var DeletionTechId = function(techId) {
		ObjectTechId.call(this, techId);
	};
	stjs.extend(DeletionTechId, ObjectTechId, [ ObjectTechId, IDeletionId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.id.DeletionTechId';
		constructor.serialVersionUID = 1;
	}, {});
	return DeletionTechId;
})