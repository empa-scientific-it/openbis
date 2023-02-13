/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var PropertyFetchOptions = function() {
	};
	stjs.extend(PropertyFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyFetchOptions;
})