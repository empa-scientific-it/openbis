/**
 * Entity type perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/entitytype/id/IEntityTypeId" ], function(stjs, ObjectPermId, IEntityTypeId) {
	/**
	 * @param permId
	 *            Entity type perm id, e.g. "MY_ENTITY_TYPE".
	 */
	var EntityTypePermId = function(permId, entityKind) {
		ObjectPermId.call(this, permId);
		this.setEntityKind(entityKind);
	};
	stjs.extend(EntityTypePermId, ObjectPermId, [ ObjectPermId, IEntityTypeId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.id.EntityTypePermId';
		constructor.serialVersionUID = 1;
		prototype.entityKind = null;
		prototype.getEntityKind = function() {
			return this.entityKind;
		};
		prototype.setEntityKind = function(entityKind) {
			this.entityKind = entityKind;
		};
		prototype.toString = function() {
			return this.getPermId() + " (" + this.getEntityKind() + ")";
		};
		prototype.equals = function(obj) {
			if (ObjectPermId.equals.call(this, obj)) {
				return this.getEntityKind() == obj.getEntityKind();
			} else {
				return false;
			}
		};
	}, {});
	return EntityTypePermId;
})