define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/common/search/NameSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var ReportingServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ReportingServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.ReportingServiceSearchCriteria';
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
	return ReportingServiceSearchCriteria;
})
