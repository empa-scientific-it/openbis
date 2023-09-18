define([ "stjs", "as/dto/common/update/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	var ListUpdateActionAdd = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionAdd, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.ListUpdateActionAdd';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionAdd;
})