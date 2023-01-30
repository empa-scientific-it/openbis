/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/sample/search/NoSampleSearchCriteria" ], function(stjs, NoSampleSearchCriteria) {
	var NoSampleContainerSearchCriteria = function() {
	};
	stjs.extend(NoSampleContainerSearchCriteria, NoSampleSearchCriteria, [ NoSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.NoSampleContainerSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoSampleContainerSearchCriteria;
})
