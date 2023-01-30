/**
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/common/id/IObjectId" ], function(stjs, exceptions, IObjectId) {
	var ObjectTechId = function(techId) {
		this.setTechId(techId);
	};
	stjs.extend(ObjectTechId, null, [ IObjectId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.id.ObjectTechId';
		constructor.serialVersionUID = 1;
		prototype.techId = null;
		prototype.getTechId = function() {
			return this.techId;
		};
		prototype.setTechId = function(techId) {
			if (techId == null) {
				// throw new exceptions.IllegalArgumentException("TechId cannot
				// be null");
			}
			this.techId = techId;
		};
		prototype.toString = function() {
			return this.getTechId().toString();
		};
		prototype.hashCode = function() {
			return ((this.getTechId() == null) ? 0 : this.getTechId().hashCode());
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
			if (this.getTechId() == null) {
				if (other.getTechId() != null) {
					return false;
				}
			} else if (!this.getTechId().equals(other.getTechId())) {
				return false;
			}
			return true;
		};
	}, {});
	return ObjectTechId;
})