/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/common/search/CodeSearchCriteria", 
		"as/dto/common/search/CodesSearchCriteria", "as/dto/externaldms/search/LabelSearchCriteria",  "as/dto/externaldms/search/AddressSearchCriteria", "as/dto/externaldms/search/ExternalDmsTypeSearchCriteria"], function(require, stjs, AbstractCompositeSearchCriteria) {
	var ExternalDmsSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(ExternalDmsSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.ExternalDmsSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withLabel = function() {
			var LabelSearchCriteria = require("as/dto/externaldms/search/LabelSearchCriteria");
			return this.addCriteria(new LabelSearchCriteria());
		};
		prototype.withAddress = function() {
			var AddressSearchCriteria = require("as/dto/externaldms/search/AddressSearchCriteria");
			return this.addCriteria(new AddressSearchCriteria());
		};
		prototype.withType = function() {
			var ExternalDmsTypeSearchCriteria = require("as/dto/externaldms/search/ExternalDmsTypeSearchCriteria");
			return this.addCriteria(new ExternalDmsTypeSearchCriteria());
		};
		
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ExternalDmsSearchCriteria;
})