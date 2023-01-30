/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var PersonalAccessTokenSessionNameSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "sessionName", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PersonalAccessTokenSessionNameSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.PersonalAccessTokenSessionNameSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PersonalAccessTokenSessionNameSearchCriteria;
})