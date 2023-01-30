/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/event/fetchoptions/EventSortOptions" ], function(require, stjs,
		FetchOptions) {
	var EventFetchOptions = function() {
	};
	stjs.extend(EventFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.fetchoptions.EventFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(registrator) {
			this.registrator = registrator;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var EventSortOptions = require("as/dto/event/fetchoptions/EventSortOptions");
				this.sort = new EventSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		sort : "EventSortOptions"
	});
	return EventFetchOptions;
})