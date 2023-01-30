define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularyTermSortOptions = function() {
		SortOptions.call(this);
	};
	
	var fields = {
		CODE: "CODE",
		ORDINAL: "ORDINAL"
	};

	stjs.extend(VocabularyTermSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {

		prototype['@type'] = 'as.dto.vocabulary.fetchoptions.VocabularyTermSortOptions';
		constructor.serialVersionUID = 1;
		
	    prototype.code = function() {
	        return this.getOrCreateSorting(fields.CODE);
	    };

	    prototype.getCode = function() {
	        return this.getSorting(fields.CODE);
	    };

	    prototype.ordinal = function() {
	        return this.getOrCreateSorting(fields.ORDINAL);
	    };

	    prototype.getOrdinal = function() {
	        return this.getSorting(fields.ORDINAL);
	    };
	}, {});
	return VocabularyTermSortOptions;
})