define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var SessionInformationSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(SessionInformationSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.fetchoptions.SessionInformationSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SessionInformationSortOptions;
})