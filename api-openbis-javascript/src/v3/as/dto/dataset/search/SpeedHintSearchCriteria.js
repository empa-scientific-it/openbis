/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/NumberFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var SpeedHintSearchCriteria = function() {
		NumberFieldSearchCriteria.call(this, "speedHint", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(SpeedHintSearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.SpeedHintSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return SpeedHintSearchCriteria;
})