define([ "stjs", "as/dto/common/update/ListUpdateValue" ], function(stjs, ListUpdateValue) {
	var ListUpdateMapValues = function() {
		ListUpdateValue.call(this);
	};
	stjs.extend(ListUpdateMapValues, ListUpdateValue, [ ListUpdateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.ListUpdateMapValues';
		constructor.serialVersionUID = 1;
		prototype.put = function(key, value) {
			var pair = {};
			pair[key] = value;
			this.add(pair);
			return this;
		}
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return ListUpdateMapValues;
})