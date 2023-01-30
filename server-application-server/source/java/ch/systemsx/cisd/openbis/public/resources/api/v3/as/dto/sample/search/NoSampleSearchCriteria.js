/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoSampleSearchCriteria = function() {
	};
	stjs.extend(NoSampleSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.NoSampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.isNegated = function() {
			return false;
		};
	}, {});
	return NoSampleSearchCriteria;
})
