/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var AbstractSearchCriteria = function() {
	};
	stjs.extend(AbstractSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.hashCode = function() {
		};
		prototype.isNegated = function() {
			return false;
		};
	}, {});
	return AbstractSearchCriteria;
})