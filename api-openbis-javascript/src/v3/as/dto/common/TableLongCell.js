define([ "stjs", "util/Exceptions", "as/dto/common/ITableCell" ], function(stjs, exceptions, ITableCell) {
	var TableLongCell = function(value) {
		this.setValue(value);
	};
	stjs.extend(TableLongCell, null, [ ITableCell ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.TableLongCell';
		constructor.serialVersionUID = 1;
		prototype.value = null;
		prototype.getValue = function() {
			return this.value;
		};
		prototype.setValue = function(value) {
			this.value = value;
		};
		prototype.toString = function() {
			return this.value;
		};
		prototype.hashCode = function() {
			return this.value == null ? 0 : this.value;
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
			var that = obj;
			if (this.value == null) {
				return that.value === null;
			}
			return this.value === that.value;
		};
	}, {});
	return TableLongCell;
})
