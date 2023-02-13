define([ "require", "stjs", "as/dto/common/search/CollectionFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(require, stjs, CollectionFieldSearchCriteria, SearchFieldType) {
	var CodesSearchCriteria = function() {
		CollectionFieldSearchCriteria.call(this, "codes", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(CodesSearchCriteria, CollectionFieldSearchCriteria, [ CollectionFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.CodesSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CodesSearchCriteria;
})
