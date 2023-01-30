define([ "require", "stjs" ], function(require, stjs) {
	var Sorting = function(field, order, parameters) {
		this.field = field;
		this.order = order;
		this.parameters = parameters;
	};

	stjs.extend(Sorting, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.Sorting';
		constructor.serialVersionUID = 1;
		prototype.getField = function() {
			return this.field;
		};
		prototype.getOrder = function() {
			return this.order;
		};
		prototype.getParameters = function() {
			return this.parameters;
		};
	}, {});
	return Sorting;
})