define([ "jquery" ], function($) {

	//
	// CALLBACK MANAGER
	//

	function CallbackManager(callback) {
		this.init(callback);
	}

	$.extend(CallbackManager.prototype, {

		init : function(callback) {
			this.callback = callback;
			this.callbacks = {};
		},

		registerCallback : function(callback) {
			var manager = this;

			var wrapper = function() {
				callback.apply(this, arguments);

				delete manager.callbacks[callback]

				for (c in manager.callbacks) {
					return;
				}

				manager.callback();
			}

			this.callbacks[callback] = callback;
			return wrapper;
		}
	});

	return CallbackManager;

});