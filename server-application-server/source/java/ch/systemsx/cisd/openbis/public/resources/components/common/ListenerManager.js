define([ "jquery" ], function($) {

	//
	// LISTENER MANAGER
	//

	function ListenerManager() {
		this.init();
	}

	$.extend(ListenerManager.prototype, {

		init : function() {
			this.listeners = {};
		},

		addListener : function(eventType, listener) {
			if (!this.listeners[eventType]) {
				this.listeners[eventType] = []
			}
			this.listeners[eventType].push(listener);
		},

		notifyListeners : function(eventType, event) {
			if (this.listeners[eventType]) {
				this.listeners[eventType].forEach(function(listener) {
					listener(event);
				});
			}
		}
	});

	return ListenerManager;

});