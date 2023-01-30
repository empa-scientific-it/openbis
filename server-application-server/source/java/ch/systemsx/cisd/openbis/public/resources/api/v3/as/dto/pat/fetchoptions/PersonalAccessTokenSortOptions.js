define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PersonalAccessTokenSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PersonalAccessTokenSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.fetchoptions.PersonalAccessTokenSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonalAccessTokenSortOptions;
})