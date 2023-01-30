/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractSearchCriteria", "as/dto/common/search/SearchOperator" ], function(stjs, AbstractSearchCriteria, SearchOperator) {
	var AbstractCompositeSearchCriteria = function() {
		AbstractSearchCriteria.call(this);
		this.criteria = [];
	};
	stjs.extend(AbstractCompositeSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractCompositeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.operator = SearchOperator.AND;
		prototype.getCriteria = function() {
			return this.criteria;
		};
		prototype.setCriteria = function(criteria) {
			this.criteria = criteria;
		};
		prototype.addCriteria = function(criteria) {
			this.criteria.push(criteria);
			return criteria;
		};
		prototype.getOperator = function() {
			return this.operator;
		}
		prototype.withOperator = function(anOperator) {
			this.operator = anOperator;
			return this;
		};
		prototype.withOrOperator = function() {
			this.operator = SearchOperator.OR;
			return this;
		}
		prototype.withAndOperator = function() {
			this.operator = SearchOperator.AND;
			return this;
		}
	}, {
		criteria : {
			name : "Collection",
			operator : "SearchOperator",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractCompositeSearchCriteria;
})