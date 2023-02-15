define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PropertyTypeSortOptions = function() {
		SortOptions.call(this);
	};
	
	var fields = {
		CODE: "CODE",
	};
	
	stjs.extend(PropertyTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyTypeSortOptions';
		constructor.serialVersionUID = 1;
		
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
	}, {});
	return PropertyTypeSortOptions;
})