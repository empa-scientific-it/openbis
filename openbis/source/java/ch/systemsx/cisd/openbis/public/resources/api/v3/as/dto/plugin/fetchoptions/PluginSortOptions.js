define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PluginSortOptions = function() {
		SortOptions.call(this);
	};
	
	var fields = {
		NAME: "NAME",
	};
	
	stjs.extend(PluginSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.fetchoptions.PluginSortOptions';
		constructor.serialVersionUID = 1;
		
		prototype.code = function() {
			return this.getOrCreateSorting(fields.NAME);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.NAME);
		};
	}, {});
	return PluginSortOptions;
})
