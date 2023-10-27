define([ "stjs", "as/dto/common/update/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	var ListUpdateActionSet = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionSet, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.ListUpdateActionSet';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionSet;
})