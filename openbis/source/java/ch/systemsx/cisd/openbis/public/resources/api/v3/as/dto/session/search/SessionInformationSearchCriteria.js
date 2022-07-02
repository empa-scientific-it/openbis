/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/session/search/UserNameSearchCriteria", "as/dto/session/search/PersonalAccessTokenSessionSearchCriteria", "as/dto/session/search/PersonalAccessTokenSessionNameSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var SessionInformationSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SessionInformationSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.SessionInformationSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withUserName = function() {
			var UserNameSearchCriteria = require("as/dto/session/search/UserNameSearchCriteria");
			return this.addCriteria(new UserNameSearchCriteria());
		};
		prototype.withPersonalAccessTokenSession = function() {
			var PersonalAccessTokenSessionSearchCriteria = require("as/dto/session/search/PersonalAccessTokenSessionSearchCriteria");
			return this.addCriteria(new PersonalAccessTokenSessionSearchCriteria());
		};
		prototype.withPersonalAccessTokenSessionName = function() {
			var PersonalAccessTokenSessionNameSearchCriteria = require("as/dto/session/search/PersonalAccessTokenSessionNameSearchCriteria");
			return this.addCriteria(new PersonalAccessTokenSessionNameSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SessionInformationSearchCriteria;
})