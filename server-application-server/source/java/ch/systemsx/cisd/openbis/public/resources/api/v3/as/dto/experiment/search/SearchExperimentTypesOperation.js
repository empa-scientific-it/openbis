/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchExperimentTypesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchExperimentTypesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.SearchExperimentTypesOperation';
		prototype.getMessage = function() {
			return "SearchExperimentTypesOperation";
		};
	}, {});
	return SearchExperimentTypesOperation;
})