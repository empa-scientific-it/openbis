/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var SearchObjectsOperation = function(criteria, fetchOptions) {
		this.criteria = criteria;
		this.fetchOptions = fetchOptions;
	};
	stjs.extend(SearchObjectsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.SearchObjectsOperation';
		prototype.criteria = null;
		prototype.fetchOptions = null;
		prototype.getCriteria = function() {
			return this.criteria;
		};
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.getMessage = function() {
			return "SearchObjectsOperation";
		};
	}, {
		criteria : "ISearchCriteria",
		fetchOptions : "FetchOptions"
	});
	return SearchObjectsOperation;
})