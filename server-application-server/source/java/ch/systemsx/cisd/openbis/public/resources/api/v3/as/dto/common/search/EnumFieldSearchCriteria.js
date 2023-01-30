define([ "require", "stjs", "as/dto/common/search/AbstractFieldSearchCriteria" ], function(require, stjs, AbstractFieldSearchCriteria) {
	var EnumFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(EnumFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.EnumFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatEquals = function(value) {
			this.setFieldValue(value);
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return EnumFieldSearchCriteria;
})