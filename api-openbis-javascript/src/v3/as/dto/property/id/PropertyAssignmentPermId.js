/**
 * Property assignment perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/property/id/IPropertyAssignmentId" ], function(stjs, IPropertyAssignmentId) {

	var PropertyAssignmentPermId = function(entityTypeId, propertyTypeId) {
		this.setEntityTypeId(entityTypeId);
		this.setPropertyTypeId(propertyTypeId);
	};
	stjs.extend(PropertyAssignmentPermId, null, [ IPropertyAssignmentId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.id.PropertyAssignmentPermId';
		constructor.serialVersionUID = 1;
		prototype.entityTypeId = null;
		prototype.propertyTypeId = null;

		prototype.getEntityTypeId = function() {
			return this.entityTypeId;
		};
		prototype.setEntityTypeId = function(entityTypeId) {
			if (entityTypeId) {
				this.entityTypeId = entityTypeId;
			} else {
				this.entityTypeId = null
			}
		};
		prototype.getPropertyTypeId = function() {
			return this.propertyTypeId;
		};
		prototype.setPropertyTypeId = function(propertyTypeId) {
			if (propertyTypeId) {
				this.propertyTypeId = propertyTypeId;
			} else {
				this.propertyTypeId = null;
			}
		};
		prototype.toString = function() {
			return (this.getEntityTypeId() ? this.getEntityTypeId().toString() : "") + ", " + (this.getPropertyTypeId() ? this.getPropertyTypeId().toString() : "");
		};
		prototype.hashCode = function() {
			return (this.getEntityTypeId() ? this.getEntityTypeId().hashCode() : 0) + (this.getPropertyTypeId() ? this.getPropertyTypeId().hashCode() : 0);
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
			if (this.getEntityTypeId() == null) {
				if (other.getEntityTypeId() != null) {
					return false;
				}
			} else if (!this.getEntityTypeId().equals(other.getEntityTypeId())) {
				return false;
			}
			if (this.getPropertyTypeId() == null) {
				if (other.getPropertyTypeId() != null) {
					return false;
				}
			} else if (!this.getPropertyTypeId().equals(other.getPropertyTypeId())) {
				return false;
			}
			return true;
		};
	}, {
		entityTypeId : "IEntityTypeId",
		propertyTypeId : "IPropertyTypeId"
	});
	return PropertyAssignmentPermId;
})