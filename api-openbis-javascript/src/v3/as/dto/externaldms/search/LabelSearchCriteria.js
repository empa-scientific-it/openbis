/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var LabelSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "label", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(LabelSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.search.LabelSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return LabelSearchCriteria;
})