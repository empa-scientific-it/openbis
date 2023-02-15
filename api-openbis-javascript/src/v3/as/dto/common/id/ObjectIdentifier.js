/**
 * Base class for ids that identify objects by identifiers. An identifier is a
 * mutable user-defined string. An identifier is assigned to an object during
 * the object creation but can change afterwards. An object's identifier is not
 * guaranteed to be always the same, e.g. a sample identifier changes when the
 * sample is moved to a different space.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/common/id/IObjectId" ], function(stjs, exceptions, IObjectId) {
	var ObjectIdentifier = function(identifier) {
		this.setIdentifier(identifier);
	};
	stjs.extend(ObjectIdentifier, null, [ IObjectId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.id.ObjectIdentifier';
		constructor.serialVersionUID = 1;
		prototype.identifier = null;
		prototype.getIdentifier = function() {
			return this.identifier;
		};
		prototype.setIdentifier = function(identifier) {
			if (identifier == null) {
				// throw new exceptions.IllegalArgumentException("Identifier id
				// cannot be null");
			}
			this.identifier = identifier;
		};
		prototype.toString = function() {
			return this.getIdentifier();
		};
		prototype.hashCode = function() {
			return ((this.getIdentifier() == null) ? 0 : this.getIdentifier().hashCode());
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
			return this.getIdentifier() == null ? this.getIdentifier() == other.getIdentifier() : this.getIdentifier().equals(other.getIdentifier());
		};
	}, {});
	return ObjectIdentifier;
})