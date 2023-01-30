define([ "require", "stjs" ], function(require, stjs) {
	var SortOrder = function() {
		this._asc = true;
	};

	stjs.extend(SortOrder, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.SortOrder';
		constructor.serialVersionUID = 1;
		prototype.asc = function() {
			this._asc = true;
		};
		prototype.desc = function() {
			this._asc = false;
		};
		prototype.isAsc = function() {
			return this._asc;
		};
	}, {});
	return SortOrder;
})