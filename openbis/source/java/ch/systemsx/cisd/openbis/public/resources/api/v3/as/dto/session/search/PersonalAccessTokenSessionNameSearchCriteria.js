define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var PersonalAccessTokenSessionNameSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "personalAccessTokenSessionName", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PersonalAccessTokenSessionNameSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.PersonalAccessTokenSessionNameSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PersonalAccessTokenSessionNameSearchCriteria;
})
