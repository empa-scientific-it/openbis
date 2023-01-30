define([ "jquery" ], function($) {

	//
	// CHANGE EVENT
	//

	function ChangeEvent(oldValue, newValue, field) {
		this.init(oldValue, newValue, field);
	}

	$.extend(ChangeEvent.prototype, {

		init : function(oldValue, newValue, field) {
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.field = field;
		},

		getOldValue : function() {
			return this.oldValue;
		},

		getNewValue : function() {
			return this.newValue;
		},

		getField : function() {
			return this.field;
		}

	});

	return ChangeEvent;

});