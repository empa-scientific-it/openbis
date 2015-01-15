/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var ObjectTechId = function(techId) {
        this.setTechId(techId);
    };
    stjs.extend(ObjectTechId, null, [IObjectId], function(constructor, prototype) {
        prototype['@type'] = 'ObjectTechId';
        constructor.serialVersionUID = 1;
        prototype.techId = null;
        prototype.getTechId = function() {
            return this.techId;
        };
        prototype.setTechId = function(techId) {
            if (techId == null) {
                 throw new IllegalArgumentException("TechId cannot be null");
            }
            this.techId = techId;
        };
        prototype.getIdAsString = function() {
            return JsonPropertyUtil.toStringOrNull(this.techId);
        };
        prototype.setIdAsString = function(id) {
            this.techId = JsonPropertyUtil.toLongOrNull(id);
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