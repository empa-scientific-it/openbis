define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PersonSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PersonSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.fetchoptions.PersonSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonSortOptions;
})