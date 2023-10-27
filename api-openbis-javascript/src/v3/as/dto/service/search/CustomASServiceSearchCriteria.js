define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/common/search/CodeSearchCriteria", 
		"as/dto/common/search/CodesSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var CustomASServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(CustomASServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.CustomASServiceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
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
	return CustomASServiceSearchCriteria;
})