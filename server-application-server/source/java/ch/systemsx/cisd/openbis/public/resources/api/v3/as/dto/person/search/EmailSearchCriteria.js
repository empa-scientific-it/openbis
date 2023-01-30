/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var EmailSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "email", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(EmailSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.search.EmailSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EmailSearchCriteria;
})