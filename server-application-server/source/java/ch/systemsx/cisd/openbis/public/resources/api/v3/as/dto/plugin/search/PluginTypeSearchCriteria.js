define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var PluginTypeSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "script type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PluginTypeSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.search.PluginTypeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PluginTypeSearchCriteria;
})
