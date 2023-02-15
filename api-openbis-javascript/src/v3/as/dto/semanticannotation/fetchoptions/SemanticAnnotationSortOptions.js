define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var SemanticAnnotationSortOptions = function() {
		SortOptions.call(this);
	};

	stjs.extend(SemanticAnnotationSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.fetchoptions.SemanticAnnotationSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SemanticAnnotationSortOptions;
})