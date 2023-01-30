/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/NumberFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var TechIdSearchCriteria = function() {
		NumberFieldSearchCriteria.call(this, "id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(TechIdSearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.TechIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return TechIdSearchCriteria;
})