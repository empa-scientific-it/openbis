/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/event/search/EventTypeSearchCriteria", "as/dto/event/search/EventEntityTypeSearchCriteria",
 "as/dto/event/search/EventEntitySpaceSearchCriteria", "as/dto/event/search/EventEntitySpaceIdSearchCriteria", "as/dto/event/search/EventEntityProjectSearchCriteria",
 "as/dto/event/search/EventEntityProjectIdSearchCriteria", "as/dto/event/search/EventEntityRegistratorSearchCriteria", "as/dto/event/search/EventEntityRegistrationDateSearchCriteria",
 "as/dto/person/search/RegistratorSearchCriteria", "as/dto/common/search/RegistrationDateSearchCriteria"],
	function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {

	var EventSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};

	stjs.extend(EventSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.search.EventSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withEventType = function() {
			var EventEventTypeSearchCriteria = require("as/dto/event/search/EventTypeSearchCriteria");
			return this.addCriteria(new EventEventTypeSearchCriteria());
		};
		prototype.withEntityType = function() {
			var EventEntityTypeSearchCriteria = require("as/dto/event/search/EventEntityTypeSearchCriteria");
			return this.addCriteria(new EventEntityTypeSearchCriteria());
		};
		prototype.withEntitySpace = function() {
			var EventEntitySpaceSearchCriteria = require("as/dto/event/search/EventEntitySpaceSearchCriteria");
			return this.addCriteria(new EventEntitySpaceSearchCriteria());
		};
		prototype.withEntitySpaceId = function() {
			var EventEntitySpaceIdSearchCriteria = require("as/dto/event/search/EventEntitySpaceIdSearchCriteria");
			return this.addCriteria(new EventEntitySpaceIdSearchCriteria());
		};
		prototype.withEntityProject = function() {
			var EventEntityProjectSearchCriteria = require("as/dto/event/search/EventEntityProjectSearchCriteria");
			return this.addCriteria(new EventEntityProjectSearchCriteria());
		};
		prototype.withEntityProjectId = function() {
			var EventEntityProjectIdSearchCriteria = require("as/dto/event/search/EventEntityProjectIdSearchCriteria");
			return this.addCriteria(new EventEntityProjectIdSearchCriteria());
		};
		prototype.withEntityRegistrator = function() {
			var EventEntityRegistratorSearchCriteria = require("as/dto/event/search/EventEntityRegistratorSearchCriteria");
			return this.addCriteria(new EventEntityRegistratorSearchCriteria());
		};
		prototype.withEntityRegistrationDate = function() {
			var EventEntityRegistrationDateSearchCriteria = require("as/dto/event/search/EventEntityRegistrationDateSearchCriteria");
			return this.addCriteria(new EventEntityRegistrationDateSearchCriteria());
		};
		prototype.withRegistrator = function() {
			var RegistratorSearchCriteria = require("as/dto/person/search/RegistratorSearchCriteria");
			return this.addCriteria(new RegistratorSearchCriteria());
		};
		prototype.withRegistrationDate = function() {
			var RegistrationDateSearchCriteria = require("as/dto/common/search/RegistrationDateSearchCriteria");
			return this.addCriteria(new RegistrationDateSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});

	return EventSearchCriteria;
});
