define([ "stjs", "as/dto/common/search/BooleanFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, BooleanFieldSearchCriteria, SearchFieldType) {
	var PersonalAccessTokenSessionSearchCriteria = function() {
		BooleanFieldSearchCriteria.call(this, "personalAccessTokenSession", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PersonalAccessTokenSessionSearchCriteria, BooleanFieldSearchCriteria, [ BooleanFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.PersonalAccessTokenSessionSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PersonalAccessTokenSessionSearchCriteria;
})