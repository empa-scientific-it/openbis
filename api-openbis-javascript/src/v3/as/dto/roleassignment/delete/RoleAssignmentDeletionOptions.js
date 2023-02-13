define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var RoleAssignmentDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(RoleAssignmentDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.delete.RoleAssignmentDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return RoleAssignmentDeletionOptions;
})
