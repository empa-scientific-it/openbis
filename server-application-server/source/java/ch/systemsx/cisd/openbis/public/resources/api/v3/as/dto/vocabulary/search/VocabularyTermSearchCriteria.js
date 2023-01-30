/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", 
		"as/dto/common/search/CodeSearchCriteria", "as/dto/common/search/PermIdSearchCriteria",
		"as/dto/vocabulary/search/VocabularySearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var VocabularyTermSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(VocabularyTermSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.VocabularyTermSearchCriteria';
		constructor.serialVersionUID = 1;

		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withVocabulary = function() {
			var VocabularySearchCriteria = require("as/dto/vocabulary/search/VocabularySearchCriteria");
			return this.addCriteria(new VocabularySearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return VocabularyTermSearchCriteria;
})