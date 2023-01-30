/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/person/search/UserIdSearchCriteria", "as/dto/person/search/UserIdsSearchCriteria",
		"as/dto/person/search/FirstNameSearchCriteria", "as/dto/person/search/LastNameSearchCriteria", "as/dto/person/search/EmailSearchCriteria" ], function(require, stjs,
		AbstractObjectSearchCriteria) {
	var PersonSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(PersonSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.search.PersonSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withUserId = function() {
			var UserIdSearchCriteria = require("as/dto/person/search/UserIdSearchCriteria");
			return this.addCriteria(new UserIdSearchCriteria());
		};
		prototype.withUserIds = function() {
			var UserIdsSearchCriteria = require("as/dto/person/search/UserIdsSearchCriteria");
			return this.addCriteria(new UserIdsSearchCriteria());
		};
		prototype.withFirstName = function() {
			var FirstNameSearchCriteria = require("as/dto/person/search/FirstNameSearchCriteria");
			return this.addCriteria(new FirstNameSearchCriteria());
		};
		prototype.withLastName = function() {
			var LastNameSearchCriteria = require("as/dto/person/search/LastNameSearchCriteria");
			return this.addCriteria(new LastNameSearchCriteria());
		};
		prototype.withEmail = function() {
			var EmailSearchCriteria = require("as/dto/person/search/EmailSearchCriteria");
			return this.addCriteria(new EmailSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return PersonSearchCriteria;
})