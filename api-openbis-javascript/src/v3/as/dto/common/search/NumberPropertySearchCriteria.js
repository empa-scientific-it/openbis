/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/NumberFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var NumberPropertySearchCriteria = function(fieldName) {
		NumberFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(NumberPropertySearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NumberPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberPropertySearchCriteria;
})