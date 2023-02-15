/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/NumberFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var SizeSearchCriteria = function() {
		NumberFieldSearchCriteria.call(this, "size", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(SizeSearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.SizeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return SizeSearchCriteria;
})