define([ "require", "stjs", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/StringEqualToValue", "as/dto/common/search/StringStartsWithValue",
		"as/dto/common/search/StringEndsWithValue", "as/dto/common/search/StringContainsValue", "as/dto/common/search/AnyStringValue", "as/dto/common/search/StringLessThanValue",
		"as/dto/common/search/StringLessThanOrEqualToValue", "as/dto/common/search/StringGreaterThanValue", "as/dto/common/search/StringGreaterThanOrEqualToValue" ], function(require, stjs, AbstractFieldSearchCriteria) {
	var StringFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		var AnyStringValue = require("as/dto/common/search/AnyStringValue");
		this.setFieldValue(new AnyStringValue());
	};
	stjs.extend(StringFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.useWildcards = false;
		prototype.thatEquals = function(string) {
			var StringEqualToValue = require("as/dto/common/search/StringEqualToValue");
			this.setFieldValue(new StringEqualToValue(string));
		};
		prototype.thatStartsWith = function(prefix) {
			var StringStartsWithValue = require("as/dto/common/search/StringStartsWithValue");
			this.setFieldValue(new StringStartsWithValue(prefix));
		};
		prototype.thatEndsWith = function(suffix) {
			var StringEndsWithValue = require("as/dto/common/search/StringEndsWithValue");
			this.setFieldValue(new StringEndsWithValue(suffix));
		};
		prototype.thatContains = function(string) {
			var StringContainsValue = require("as/dto/common/search/StringContainsValue");
			this.setFieldValue(new StringContainsValue(string));
		};
		prototype.thatIsLessThan = function(string) {
			var StringLessThanValue = require("as/dto/common/search/StringLessThanValue");
			this.setFieldValue(new StringLessThanValue(string));
		};
		prototype.thatIsLessThanOrEqualTo = function(string) {
			var StringLessThanOrEqualToValue = require("as/dto/common/search/StringLessThanOrEqualToValue");
			this.setFieldValue(new StringLessThanOrEqualToValue(string));
		};
		prototype.thatIsGreaterThan = function(string) {
			var StringGreaterThanValue = require("as/dto/common/search/StringGreaterThanValue");
			this.setFieldValue(new StringGreaterThanValue(string));
		};
		prototype.thatIsGreaterThanOrEqualTo = function(string) {
			var StringGreaterThanOrEqualToValue = require("as/dto/common/search/StringGreaterThanOrEqualToValue");
			this.setFieldValue(new StringGreaterThanOrEqualToValue(string));
		};
		prototype.withWildcards = function() {
			this.useWildcards = true;
			return this;
		}
		prototype.withoutWildcards = function() {
			this.useWildcards = false;
			return this;
		}
		prototype.isUseWildcards = function() {
			return this.useWildcards;
		}
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StringFieldSearchCriteria;
})
