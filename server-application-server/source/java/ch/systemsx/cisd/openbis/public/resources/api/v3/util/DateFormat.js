define([ 'moment', 'util/Exceptions' ], function(moment, exceptions) {
	var DateFormat = function(format) {
		this.format = format;
		this.lenient = true;
	}

	DateFormat.prototype.setLenient = function(lenient) {
		this.lenient = lenient;
	};

	DateFormat.prototype.parse = function(str) {
		var m = moment(str, this.format, this.lenient === false);
		if (m.isValid()) {
			return m.toDate();
		}
		throw new exceptions.IllegalArgumentExcpetion("invalid date string: " + str);
	};

	return DateFormat;
})