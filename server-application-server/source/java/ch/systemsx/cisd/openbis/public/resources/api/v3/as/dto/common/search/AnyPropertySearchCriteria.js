/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyPropertySearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_PROPERTY);
	};
	stjs.extend(AnyPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AnyPropertySearchCriteria';
		constructor.serialVersionUID = 1;

		prototype.thatMatches = function (text) {
			var StringMatchesValue = require("as/dto/common/search/StringMatchesValue");
			this.setFieldValue(new StringMatchesValue(text));
		}
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyPropertySearchCriteria;
})