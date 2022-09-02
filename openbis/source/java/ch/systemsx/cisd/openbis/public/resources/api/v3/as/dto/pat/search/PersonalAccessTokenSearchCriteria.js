/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/pat/search/PersonalAccessTokenOwnerSearchCriteria", "as/dto/pat/search/PersonalAccessTokenSessionNameSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var PersonalAccessTokenSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(PersonalAccessTokenSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.PersonalAccessTokenSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withOwner = function() {
			var PersonalAccessTokenOwnerSearchCriteria = require("as/dto/pat/search/PersonalAccessTokenOwnerSearchCriteria");
			return this.addCriteria(new PersonalAccessTokenOwnerSearchCriteria());
		};
		prototype.withSessionName = function() {
			var PersonalAccessTokenSessionNameSearchCriteria = require("as/dto/pat/search/PersonalAccessTokenSessionNameSearchCriteria");
			return this.addCriteria(new PersonalAccessTokenSessionNameSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return PersonalAccessTokenSearchCriteria;
})