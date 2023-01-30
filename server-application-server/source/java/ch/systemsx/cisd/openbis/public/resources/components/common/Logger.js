define([ "jquery" ], function($) {

	//
	// LOGGER
	//

	function Logger() {
	}

	Logger.log = function(msg) {
		if (console) {
			var date = new Date();
			console.log(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "." + date.getMilliseconds() + " - " + msg);
		}
	}

	return Logger;

});