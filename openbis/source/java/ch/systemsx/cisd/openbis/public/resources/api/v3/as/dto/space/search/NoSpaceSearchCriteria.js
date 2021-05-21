define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoSpaceSearchCriteria = function() {
	};
	stjs.extend(NoSpaceSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.search.NoSpaceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.isNegated = function() {
			return false;
		};
	}, {});
	return NoSpaceSearchCriteria;
})
