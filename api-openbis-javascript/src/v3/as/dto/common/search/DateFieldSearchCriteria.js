define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/ServerTimeZone", "as/dto/common/search/ShortDateFormat",
		"as/dto/common/search/NormalDateFormat", "as/dto/common/search/LongDateFormat", "as/dto/common/search/DateEqualToValue", "as/dto/common/search/DateObjectEqualToValue",
		"as/dto/common/search/DateLaterThanValue", "as/dto/common/search/DateLaterThanOrEqualToValue",
		"as/dto/common/search/DateObjectLaterThanValue", "as/dto/common/search/DateObjectLaterThanOrEqualToValue", 
		"as/dto/common/search/DateEarlierThanValue", "as/dto/common/search/DateEarlierThanOrEqualToValue",
		"as/dto/common/search/DateObjectEarlierThanValue", "as/dto/common/search/DateObjectEarlierThanOrEqualToValue", 
		"as/dto/common/search/TimeZone", "as/dto/common/search/AbstractDateValue", "util/DateFormat" ], function(require, stjs, exceptions,
		AbstractFieldSearchCriteria, ServerTimeZone) {
	var DateFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		this.timeZone = new ServerTimeZone();
	};

	stjs.extend(DateFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		var ShortDateFormat = require("as/dto/common/search/ShortDateFormat");
		var NormalDateFormat = require("as/dto/common/search/NormalDateFormat");
		var LongDateFormat = require("as/dto/common/search/LongDateFormat");
		constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
		var value = function(DateValueClass, DateObjectValueClass, date) {
			if (date instanceof Date) {
				return new DateObjectValueClass(date);
			}
			return new DateValueClass(date);
		}
		prototype.thatEquals = function(date) {
			var DateEqualToValue = require("as/dto/common/search/DateEqualToValue");
			var DateObjectEqualToValue = require("as/dto/common/search/DateObjectEqualToValue");
			this.setFieldValue(value(DateEqualToValue, DateObjectEqualToValue, date));
		};
		prototype.thatIsLaterThanOrEqualTo = function(date) {
			var DateLaterThanOrEqualToValue = require("as/dto/common/search/DateLaterThanOrEqualToValue");
			var DateObjectLaterThanOrEqualToValue = require("as/dto/common/search/DateObjectLaterThanOrEqualToValue");
			this.setFieldValue(value(DateLaterThanOrEqualToValue, DateObjectLaterThanOrEqualToValue, date));
		};
		prototype.thatIsEarlierThan = function(date) {
			var DateEarlierThanValue = require("as/dto/common/search/DateEarlierThanValue");
			var DateObjectEarlierThanValue = require("as/dto/common/search/DateObjectEarlierThanValue");
			this.setFieldValue(value(DateEarlierThanValue, DateObjectEarlierThanValue, date));
		};
		prototype.thatIsLaterThan = function(date) {
			var DateLaterThanValue = require("as/dto/common/search/DateLaterThanValue");
			var DateObjectLaterThanValue = require("as/dto/common/search/DateObjectLaterThanValue");
			this.setFieldValue(value(DateLaterThanValue, DateObjectLaterThanValue, date));
		};
		prototype.thatIsEarlierThanOrEqualTo = function(date) {
			var DateEarlierThanOrEqualToValue = require("as/dto/common/search/DateEarlierThanOrEqualToValue");
			var DateObjectEarlierThanOrEqualToValue = require("as/dto/common/search/DateObjectEarlierThanOrEqualToValue");
			this.setFieldValue(value(DateEarlierThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, date));
		};
		prototype.withServerTimeZone = function() {
			this.timeZone = new ServerTimeZone();
			return this;
		};
		prototype.withTimeZone = function(hourOffset) {
			var TimeZone = require("as/dto/common/search/TimeZone");
			this.timeZone = new TimeZone(hourOffset);
			return this;
		};
		prototype.setTimeZone = function(timeZone) {
			this.timeZone = timeZone;
		};
		prototype.getTimeZone = function() {
			return this.timeZone;
		};
		prototype.setFieldValue = function(value) {
			DateFieldSearchCriteria.checkValueFormat(value);
			AbstractFieldSearchCriteria.prototype.setFieldValue.call(this, value);
		};
		constructor.checkValueFormat = function(value) {
			var AbstractDateValue = require("as/dto/common/search/AbstractDateValue");
			if (stjs.isInstanceOf(value.constructor, AbstractDateValue)) {
				var formats = DateFieldSearchCriteria.DATE_FORMATS;
				for (var i in formats) {
					if (formats.hasOwnProperty(i)) {
						var dateFormat = formats[i];
						if (this.formatValue(value, dateFormat)) {
							return;
						}
					}
				}
				throw new exceptions.IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: " + DateFieldSearchCriteria.DATE_FORMATS);
			}
		};
		constructor.formatValue = function(value, dateFormat) {
			try {
				var DateFormat = require("util/DateFormat");
				var dateFormat1 = new DateFormat(dateFormat.getFormat());
				dateFormat1.setLenient(false);
				return dateFormat1.parse(value.getValue());
			} catch (e) {
				return null;
			}
		};
	}, {
		DATE_FORMATS : {
			name : "List",
			arguments : [ "IDateFormat" ]
		},
		timeZone : "ITimeZone",
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});

	return DateFieldSearchCriteria;
})
