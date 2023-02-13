define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var AuthorizationGroupDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(AuthorizationGroupDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return AuthorizationGroupDeletionOptions;
})
