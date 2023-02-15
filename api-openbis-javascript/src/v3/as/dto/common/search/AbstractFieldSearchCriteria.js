define([ "stjs", "as/dto/common/search/AbstractSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, AbstractSearchCriteria, SearchFieldType) {
	var AbstractFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractSearchCriteria.call(this);
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	};
	stjs.extend(AbstractFieldSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.fieldName = null;
		prototype.fieldType = null;
		prototype.fieldValue = null;
		prototype.getFieldName = function() {
			return this.fieldName;
		};
		prototype.getFieldType = function() {
			return this.fieldType;
		};
		prototype.getFieldValue = function() {
			return this.fieldValue;
		};
		prototype.setFieldValue = function(value) {
			this.fieldValue = value;
		};
		prototype.toString = function() {
			var descriptor = "";
			switch (this.getFieldType()) {
			case SearchFieldType.PROPERTY:
				descriptor = "with property '" + this.getFieldName() + "'";
				break;
			case SearchFieldType.ATTRIBUTE:
				descriptor = "with attribute '" + this.getFieldName() + "'";
				break;
			case SearchFieldType.ANY_PROPERTY:
				descriptor = "any property";
				break;
			case SearchFieldType.ANY_FIELD:
				descriptor = "any field";
				break;
			}
			return descriptor + " " + (this.getFieldValue() == null ? "" : this.getFieldValue().toString());
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AbstractFieldSearchCriteria;
})