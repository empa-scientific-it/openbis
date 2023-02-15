define([ "require", "stjs", "as/dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var SampleSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};

	var fields = {
		IDENTIFIER : "IDENTIFIER"
	};

	stjs.extend(SampleSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.fetchoptions.SampleSortOptions';
		constructor.serialVersionUID = 1;

		prototype.identifier = function() {
			return this.getOrCreateSorting(fields.IDENTIFIER);
		};
		prototype.getIdentifier = function() {
			return this.getSorting(fields.IDENTIFIER);
		};
	}, {});
	return SampleSortOptions;
})