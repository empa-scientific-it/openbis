/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var PersonalAccessTokenDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(PersonalAccessTokenDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.delete.PersonalAccessTokenDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonalAccessTokenDeletionOptions;
})