define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractSearchCriteria" ], function(require, stjs, exceptions, AbstractSearchCriteria) {
	var GlobalSearchWildCardsCriteria = function() {
	};

	stjs.extend(GlobalSearchWildCardsCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.search.GlobalSearchWildCardsCriteria';
		constructor.serialVersionUID = 1;

		prototype.toString = function() {
			return "with wildcards";
		};
	}, {});

	return GlobalSearchWildCardsCriteria;
})
