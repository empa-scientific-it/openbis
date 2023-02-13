define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var TableModel = function() {
	};
	stjs.extend(TableModel, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.TableModel';
		constructor.serialVersionUID = 1;
		prototype.columns = null;
		prototype.rows = null;
		prototype.getColumns = function() {
			return this.columns;
		};
		prototype.setColumns = function(columns) {
			this.columns = columns;
		};
		prototype.getRows = function() {
			return this.rows;
		};
		prototype.setRows = function(rows) {
			this.rows = rows;
		};
	}, {});
	return TableModel;
})
