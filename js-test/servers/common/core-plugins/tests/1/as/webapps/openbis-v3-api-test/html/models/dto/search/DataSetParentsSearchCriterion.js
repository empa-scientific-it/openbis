/**
 * @author Jakub Straszewski
 */
define([ "support/stjs", "dto/search/DataSetSearchCriterion", "dto/search/DataSetSearchRelation" ], function(stjs, DataSetSearchCriterion, DataSetSearchRelation) {
	var DataSetParentsSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.PARENTS);
	};
	stjs.extend(DataSetParentsSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetParentsSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return DataSetParentsSearchCriterion;
})