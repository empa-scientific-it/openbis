define([ "require", "stjs", "as/dto/common/search/AbstractFieldSearchCriteria" ], function(require, stjs, AbstractFieldSearchCriteria) {
	var CollectionFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(CollectionFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.CollectionFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatIn = function(values) {
			this.setFieldValue(values);
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CollectionFieldSearchCriteria;
})