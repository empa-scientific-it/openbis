define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var PluginKindSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "plugin type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PluginKindSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.search.PluginKindSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PluginKindSearchCriteria;
})
