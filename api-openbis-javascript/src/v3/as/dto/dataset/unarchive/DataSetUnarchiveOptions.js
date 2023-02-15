/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var DataSetUnarchiveOptions = function() {
	};
	stjs.extend(DataSetUnarchiveOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.unarchive.DataSetUnarchiveOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetUnarchiveOptions;
})