define([ "require", "stjs", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/NumberEqualToValue", "as/dto/common/search/NumberLessThanValue",
		"as/dto/common/search/NumberLessThanOrEqualToValue", "as/dto/common/search/NumberGreaterThanValue", "as/dto/common/search/NumberGreaterThanOrEqualToValue" ], function(require, stjs,
		AbstractFieldSearchCriteria) {
	var NumberFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NumberFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatEquals = function(number) {
			var NumberEqualToValue = require("as/dto/common/search/NumberEqualToValue");
			this.setFieldValue(new NumberEqualToValue(number));
		};
		prototype.thatIsLessThan = function(number) {
			var NumberLessThanValue = require("as/dto/common/search/NumberLessThanValue");
			this.setFieldValue(new NumberLessThanValue(number));
		};
		prototype.thatIsLessThanOrEqualTo = function(number) {
			var NumberLessThanOrEqualToValue = require("as/dto/common/search/NumberLessThanOrEqualToValue");
			this.setFieldValue(new NumberLessThanOrEqualToValue(number));
		};
		prototype.thatIsGreaterThan = function(number) {
			var NumberGreaterThanValue = require("as/dto/common/search/NumberGreaterThanValue");
			this.setFieldValue(new NumberGreaterThanValue(number));
		};
		prototype.thatIsGreaterThanOrEqualTo = function(number) {
			var NumberGreaterThanOrEqualToValue = require("as/dto/common/search/NumberGreaterThanOrEqualToValue");
			this.setFieldValue(new NumberGreaterThanOrEqualToValue(number));
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberFieldSearchCriteria;
})