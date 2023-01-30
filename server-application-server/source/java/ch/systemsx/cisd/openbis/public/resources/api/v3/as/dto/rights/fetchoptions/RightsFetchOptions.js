define([ "stjs", "as/dto/common/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var RightsFetchOptions = function() {
	};
	stjs.extend(RightsFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.rights.fetchoptions.RightsFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return RightsFetchOptions;
})
