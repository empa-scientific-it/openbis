define([ "stjs", "as/dto/common/update/ListUpdateActionRemove", "as/dto/common/update/ListUpdateActionAdd", "as/dto/common/update/ListUpdateActionSet" ], function(stjs, ListUpdateActionRemove,
		ListUpdateActionAdd, ListUpdateActionSet) {
	var ListUpdateValue = function() {
		this.actions = [];
	};
	stjs.extend(ListUpdateValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.ListUpdateValue';
		constructor.serialVersionUID = 1;
		prototype.setActions = function(actions) {
			this.actions = actions;
		};
		prototype.getActions = function() {
			return this.actions;
		};
		prototype.hasActions = function() {
			return this.getActions() && this.getActions().length > 0;
		};
		var asArray = function(items) {
			if (Array.isArray(items)) {
				return items;
			}
			if (items === null) {
				return [];
			}
			return [ items ];
		}
		var createAction = function(actions, actionClass, items) {
			var action = new actionClass();
			action.setItems(asArray(items));
			actions.push(action);
		}
		prototype.remove = function(items) {
			createAction(this.actions, ListUpdateActionRemove, items);
		};
		prototype.add = function(items) {
			createAction(this.actions, ListUpdateActionAdd, items);
		};
		prototype.set = function(items) {
			createAction(this.actions, ListUpdateActionSet, items);
		};
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return ListUpdateValue;
})