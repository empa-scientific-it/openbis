define([ "require", "stjs", "as/dto/common/search/CollectionFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(require, stjs, CollectionFieldSearchCriteria, SearchFieldType) {
	var IdsSearchCriteria = function() {
		CollectionFieldSearchCriteria.call(this, "id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(IdsSearchCriteria, CollectionFieldSearchCriteria, [ CollectionFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.IdsSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return IdsSearchCriteria;
})