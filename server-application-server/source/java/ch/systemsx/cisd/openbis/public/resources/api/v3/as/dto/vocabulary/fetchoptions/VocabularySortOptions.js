define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularySortOptions = function() {
		SortOptions.call(this);
	};
	
	var fields = {
		CODE: "CODE",
	};

	stjs.extend(VocabularySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.fetchoptions.VocabularySortOptions';
		constructor.serialVersionUID = 1;
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};

		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
	}, {});
	return VocabularySortOptions;
})