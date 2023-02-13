/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchExperimentsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchExperimentsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.SearchExperimentsOperationResult';
		prototype.getMessage = function() {
			return "SearchExperimentsOperationResult";
		};
	}, {});
	return SearchExperimentsOperationResult;
})