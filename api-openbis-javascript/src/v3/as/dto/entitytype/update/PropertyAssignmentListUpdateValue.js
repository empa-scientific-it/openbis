define([ "stjs", "as/dto/common/update/ListUpdateValue" ], function(stjs, ListUpdateValue) {
	var PropertyAssignmentListUpdateValue = function() {
		ListUpdateValue.call(this);
	};
	stjs.extend(PropertyAssignmentListUpdateValue, ListUpdateValue, [ ListUpdateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.update.PropertyAssignmentListUpdateValue';
		constructor.serialVersionUID = 1;
		prototype.forceRemovingAssignments = false;
		prototype.isForceRemovingAssignments = function() {
			return this.forceRemovingAssignments;
		};
		prototype.setForceRemovingAssignments = function(forceRemovingAssignments) {
			this.forceRemovingAssignments = forceRemovingAssignments;
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
	return PropertyAssignmentListUpdateValue;
})
