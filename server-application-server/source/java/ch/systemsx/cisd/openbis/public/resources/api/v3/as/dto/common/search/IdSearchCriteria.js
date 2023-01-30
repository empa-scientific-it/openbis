/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractSearchCriteria" ], function(stjs, AbstractSearchCriteria) {
	var IdSearchCriteria = function() {
		AbstractSearchCriteria.call(this);
	};
	stjs.extend(IdSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.IdSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.id = null;
		prototype.thatEquals = function(id) {
			this.id = id;
		};
		prototype.getId = function() {
			return this.id;
		};
		prototype.hashCode = function() {
			return ((this.id == null) ? 0 : this.id.hashCode());
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
			return this.id == null ? this.id == other.id : this.id.equals(other.id);
		};
	}, {});
	return IdSearchCriteria;
})