/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoExperimentSearchCriteria = function() {
	};
	stjs.extend(NoExperimentSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.NoExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.isNegated = function() {
			return false;
		};
	}, {});
	return NoExperimentSearchCriteria;
})
