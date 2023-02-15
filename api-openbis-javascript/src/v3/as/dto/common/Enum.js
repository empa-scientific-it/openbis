define([ "stjs" ], function(stjs) {
	var Enum = function(values) {
		var thisEnum = this;
		this.values = values;
		values.forEach(function(value) {
			thisEnum[value] = value;
		});
	};
	stjs.extend(Enum, null, [], function(constructor, prototype) {
		prototype.values = function() {
			return this.values;
		};
	}, {});
	return Enum;
})