define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/common/search/NameSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var SearchDomainServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SearchDomainServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchDomainServiceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withName = function() {
			var NameSearchCriteria = require("as/dto/common/search/NameSearchCriteria");
			return this.addCriteria(new NameSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SearchDomainServiceSearchCriteria;
})
