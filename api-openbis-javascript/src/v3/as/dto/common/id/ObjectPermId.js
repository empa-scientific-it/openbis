/**
 * Base class for ids that identify objects by a perm id. A perm id is an
 * immutable system-generated string. A perm id is assigned to an object during
 * the object creation and cannot be changed afterwards. An object's perm id is
 * guaranteed to be always the same, e.g. a sample perm id remains the same even
 * if the sample is moved to a different space.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/common/id/IObjectId" ], function(stjs, exceptions, IObjectId) {
	var ObjectPermId = function(permId) {
		this.setPermId(permId);
	};
	stjs.extend(ObjectPermId, null, [ IObjectId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.id.ObjectPermId';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			if (permId == null) {
				// TODO throw new exceptions.IllegalArgumentException("PermId
				// cannot be null");
			}
			this.permId = permId;
		};
		prototype.toString = function() {
			return this.getPermId();
		};
		prototype.hashCode = function() {
			return ((this.getPermId() == null) ? 0 : this.getPermId().hashCode());
		};
		prototype.equals = function(obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			var other = obj;
			if (this.getPermId() == null) {
				if (other.getPermId() != null) {
					return false;
				}
			} else if (!this.getPermId().equals(other.getPermId())) {
				return false;
			}
			return true;
		};
	}, {});
	return ObjectPermId;
})