define([ "jquery" ], function($) {

	//
	// ABSTRACT VIEW
	//

	function AbstractView(controller) {
		this.init(controller);
	}

	$.extend(AbstractView.prototype, {

		init : function(controller) {
			this.controller = controller;
		},

		render : function() {
			return null;
		},

		refresh : function() {

		}

	});

	return AbstractView;

});
