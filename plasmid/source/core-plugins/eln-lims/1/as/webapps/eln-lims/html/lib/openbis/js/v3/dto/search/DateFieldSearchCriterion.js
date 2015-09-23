define([ "stjs", "util/Exceptions", "util/DateFormat", "dto/search/AbstractFieldSearchCriterion", "dto/search/ServerTimeZone", "dto/search/DateObjectEqualToValue", "dto/search/DateEqualToValue",
		"dto/search/DateObjectLaterThanOrEqualToValue", "dto/search/DateLaterThanOrEqualToValue", "dto/search/DateObjectEarlierThanOrEqualToValue", "dto/search/DateEarlierThanOrEqualToValue",
		"dto/search/TimeZone", "dto/search/AbstractDateValue", "dto/search/ShortDateFormat", "dto/search/NormalDateFormat", "dto/search/LongDateFormat" ], function(stjs, exceptions, DateFormat,
		AbstractFieldSearchCriterion, ServerTimeZone, DateObjectEqualToValue, DateEqualToValue, DateObjectLaterThanOrEqualToValue, DateLaterThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue,
		DateEarlierThanOrEqualToValue, TimeZone, AbstractDateValue, ShortDateFormat, NormalDateFormat, LongDateFormat) {
	var DateFieldSearchCriterion = function(fieldName, fieldType) {
		AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
		this.timeZone = new ServerTimeZone();
	};

	stjs.extend(DateFieldSearchCriterion, AbstractFieldSearchCriterion, [ AbstractFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateFieldSearchCriterion';
		constructor.serialVersionUID = 1;
		constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
		var value = function(DateValueClass, DateObjectValueClass, date) {
			if (date instanceof Date) {
				return new DateObjectValueClass(date);
			}
			return new DateValueClass(date);
		}
		prototype.thatEquals = function(date) {
			this.setFieldValue(value(DateEqualToValue, DateObjectEqualToValue, date));
		};
		prototype.thatIsLaterThanOrEqualTo = function(date) {
			this.setFieldValue(value(DateLaterThanOrEqualToValue, DateObjectLaterThanOrEqualToValue, date));
		};
		prototype.thatIsEarlierThanOrEqualTo = function(date) {
			this.setFieldValue(value(DateEarlierThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, date));
		};
		prototype.withServerTimeZone = function() {
			this.timeZone = new ServerTimeZone();
			return this;
		};
		prototype.withTimeZone = function(hourOffset) {
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
			DateFieldSearchCriterion.checkValueFormat(value);
			AbstractFieldSearchCriterion.prototype.setFieldValue.call(this, value);
		};
		constructor.checkValueFormat = function(value) {
			if (stjs.isInstanceOf(value.constructor, AbstractDateValue)) {
				var formats = DateFieldSearchCriterion.DATE_FORMATS;
				for ( var i in formats) {
					var dateFormat = formats[i];
					try {
						var dateFormat = new DateFormat(dateFormat.getFormat());
						dateFormat.setLenient(false);
						dateFormat.parse(value.getValue());
						return;
					} catch (e) {
					}
				}
				throw new exceptions.IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: " + DateFieldSearchCriterion.DATE_FORMATS);
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

	return DateFieldSearchCriterion;
})
