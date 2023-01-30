define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var EventSortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
	    ID : "event_id",
	    IDENTIFIER : "event_identifier",
        REGISTRATION_DATE : "event_registration_date"
    };

	stjs.extend(EventSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.event.fetchoptions.EventSortOptions';
		constructor.serialVersionUID = 1;

        prototype.id = function() {
            return this.getOrCreateSorting(fields.ID);
        };
        prototype.getId = function() {
            return this.getSorting(fields.ID);
        };
        prototype.identifier = function() {
            return this.getOrCreateSorting(fields.IDENTIFIER);
        };
        prototype.getIdentifier = function() {
            return this.getSorting(fields.IDENTIFIER);
        };
        prototype.registrationDate = function() {
            return this.getOrCreateSorting(fields.REGISTRATION_DATE);
        };
        prototype.getRegistrationDate = function() {
            return this.getSorting(fields.REGISTRATION_DATE);
        };
	}, {});
	return EventSortOptions;
})