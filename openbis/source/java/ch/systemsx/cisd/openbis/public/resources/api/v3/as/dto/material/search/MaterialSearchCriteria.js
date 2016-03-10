/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriteria, SearchOperator) {
	var MaterialSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(MaterialSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.search.MaterialSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return MaterialSearchCriteria;
})