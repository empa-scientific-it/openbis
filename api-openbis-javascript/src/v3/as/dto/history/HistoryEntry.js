define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var HistoryEntry = function() {
	};
	stjs.extend(HistoryEntry, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.HistoryEntry';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.validFrom = null;
		prototype.validTo = null;
		prototype.author = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getValidFrom = function() {
			return this.validFrom;
		};
		prototype.setValidFrom = function(validFrom) {
			this.validFrom = validFrom;
		};
		prototype.getValidTo = function() {
			return this.validTo;
		};
		prototype.setValidTo = function(validTo) {
			this.validTo = validTo;
		};
		prototype.getAuthor = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasAuthor()) {
				return this.author;
			} else {
				throw new exceptions.NotFetchedException("Author has not been fetched.");
			}
		};
		prototype.setAuthor = function(author) {
			this.author = author;
		};
	}, {
		fetchOptions : "HistoryEntryFetchOptions",
		validFrom : "Date",
		validTo : "Date",
		author : "Person"
	});
	return HistoryEntry;
})