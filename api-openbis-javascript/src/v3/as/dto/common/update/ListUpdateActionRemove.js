define([ "stjs", "as/dto/common/update/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	var ListUpdateActionRemove = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionRemove, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.ListUpdateActionRemove';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionRemove;
})