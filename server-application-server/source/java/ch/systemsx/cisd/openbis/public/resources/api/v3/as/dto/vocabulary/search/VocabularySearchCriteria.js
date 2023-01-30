/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", 
	"as/dto/common/search/CodeSearchCriteria", "as/dto/common/search/CodesSearchCriteria", "as/dto/common/search/PermIdSearchCriteria",
		"as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var VocabularySearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(VocabularySearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.VocabularySearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
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
	return VocabularySearchCriteria;
})