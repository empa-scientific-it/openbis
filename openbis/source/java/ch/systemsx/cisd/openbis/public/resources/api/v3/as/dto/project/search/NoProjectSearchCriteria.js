/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoProjectSearchCriteria = function() {
	};
	stjs.extend(NoProjectSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.search.NoProjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.isNegated = function() {
			return false;
		};
	}, {});
	return NoProjectSearchCriteria;
})
