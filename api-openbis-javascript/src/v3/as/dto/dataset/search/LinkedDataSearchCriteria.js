/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/dataset/search/ExternalCodeSearchCriteria", "as/dto/dataset/search/ExternalDmsSearchCriteria" ], function(
		require, stjs, AbstractCompositeSearchCriteria) {
	var LinkedDataSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(LinkedDataSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.LinkedDataSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withExternalCode = function() {
			var ExternalCodeSearchCriteria = require("as/dto/dataset/search/ExternalCodeSearchCriteria");
			return this.addCriteria(new ExternalCodeSearchCriteria());
		};
		prototype.withExternalDms = function() {
			var ExternalDmsSearchCriteria = require("as/dto/dataset/search/ExternalDmsSearchCriteria");
			return this.addCriteria(new ExternalDmsSearchCriteria());
		};
		prototype.withCopy = function() {
			var ContentCopySearchCriteria = require("as/dto/dataset/search/ContentCopySearchCriteria");
			return this.addCriteria(new ContentCopySearchCriteria());
		};		
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return LinkedDataSearchCriteria;
})