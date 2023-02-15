/**
 * Space perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/space/id/ISpaceId" ], function(stjs, ObjectPermId, ISpaceId) {
	/**
	 * @param permId
	 *            Space perm id, e.g. "/MY_SPACE" or "MY_SPACE".
	 */
	var SpacePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(SpacePermId, ObjectPermId, [ ObjectPermId, ISpaceId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.id.SpacePermId';
		constructor.serialVersionUID = 1;
		prototype.getPermId = function() {
			var permId = ObjectPermId.prototype.getPermId.call(this);
			if (permId.startsWith("/")) {
				return permId.substring(1);
			} else {
				return permId;
			}
		};
	}, {});
	return SpacePermId;
})