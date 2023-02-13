define(function() {
	function RuntimeException(message) {
	    this.name = "RuntimeException";
	    this.message = (message || "");
	}
	RuntimeException.prototype = Error.prototype;

	function IllegalArgumentException(message) {
	    this.name = "IllegalArgumentException";
	    this.message = (message || "");
	}
	IllegalArgumentException.prototype = Error.prototype;

	function NotFetchedException(message) {
	    this.name = "NotFetchedException";
	    this.message = (message || "");
	}
	NotFetchedException.prototype = Error.prototype;

	return {
		RuntimeException: RuntimeException,
		IllegalArgumentException: IllegalArgumentException,
		NotFetchedException: NotFetchedException
	}
})