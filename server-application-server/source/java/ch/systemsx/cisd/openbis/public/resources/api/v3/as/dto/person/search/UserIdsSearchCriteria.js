define([ "require", "stjs", "as/dto/common/search/CollectionFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(require, stjs, CollectionFieldSearchCriteria, SearchFieldType) {
	var UserIdsSearchCriteria = function() {
		CollectionFieldSearchCriteria.call(this, "userId", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(UserIdsSearchCriteria, CollectionFieldSearchCriteria, [ CollectionFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.search.UserIdsSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return UserIdsSearchCriteria;
})