define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var TableColumn = function(title) {
		this.setTitle(title);
	};
	stjs.extend(TableColumn, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.TableColumn';
		constructor.serialVersionUID = 1;
		prototype.title = null;
		prototype.getTitle = function() {
			return this.title;
		};
		prototype.setTitle = function(title) {
			this.title = title;
		};
		prototype.toString = function() {
			return this.title;
		};
		prototype.hashCode = function() {
			return this.title == null ? 0 : this.title;
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
			if (this.title == null) {
				return that.title === null;
			}
			return this.title === that.title;
		};
	}, {});
	return TableColumn;
})
