define([ "require", "stjs", "as/dto/common/search/AbstractFieldSearchCriteria" ], function(require, stjs, AbstractFieldSearchCriteria) {
	var BooleanFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(BooleanFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.BooleanFieldSearchCriteria';
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
	return BooleanFieldSearchCriteria;
})