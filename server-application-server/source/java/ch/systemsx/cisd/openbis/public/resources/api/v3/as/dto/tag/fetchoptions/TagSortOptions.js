define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var TagSortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
		CODE : "CODE",
		REGISTRATION_DATE : "REGISTRATION_DATE"
	};

	stjs.extend(TagSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.fetchoptions.TagSortOptions';
		constructor.serialVersionUID = 1;
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
		prototype.registrationDate = function() {
			return this.getOrCreateSorting(fields.REGISTRATION_DATE);
		};
		prototype.getRegistrationDate = function() {
			return this.getSorting(fields.REGISTRATION_DATE);
		};
	}, {});
	return TagSortOptions;
})