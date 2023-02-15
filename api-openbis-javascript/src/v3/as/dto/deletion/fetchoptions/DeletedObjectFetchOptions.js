/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var DeletedObjectFetchOptions = function() {
	};
	stjs.extend(DeletedObjectFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.fetchoptions.DeletedObjectFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DeletedObjectFetchOptions;
})