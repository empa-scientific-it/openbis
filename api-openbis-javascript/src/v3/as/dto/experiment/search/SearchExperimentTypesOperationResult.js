/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchExperimentTypesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchExperimentTypesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.SearchExperimentTypesOperationResult';
		prototype.getMessage = function() {
			return "SearchExperimentTypesOperationResult";
		};
	}, {});
	return SearchExperimentTypesOperationResult;
})