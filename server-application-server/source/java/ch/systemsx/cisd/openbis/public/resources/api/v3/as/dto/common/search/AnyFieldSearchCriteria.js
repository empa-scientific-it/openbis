/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyFieldSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_FIELD);
	};
	stjs.extend(AnyFieldSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AnyFieldSearchCriteria';
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
	return AnyFieldSearchCriteria;
})