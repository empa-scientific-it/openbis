define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var HistoryEntrySortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(HistoryEntrySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.fetchoptions.HistoryEntrySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return HistoryEntrySortOptions;
})