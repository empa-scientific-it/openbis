define([ "jquery" ], function($) {

	//
	// DELAYED EXECUTOR
	//

	function DelayedExecutor(delay) {
		this.init(delay);
	}

	$.extend(DelayedExecutor.prototype, {

		init : function(delay) {
			this.delay = delay;
		},

		execute : function(action) {
			var thisExecutor = this;

			if (this.actionId) {
				clearTimeout(this.actionId);
			}

			this.actionId = setTimeout(function() {
				thisExecutor.actionId = null;
				action();
			}, this.delay);
		}

	});

	return DelayedExecutor;

});