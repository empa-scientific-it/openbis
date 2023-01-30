define([ "jquery" ], function($) {

	//
	// LOADING VIEW
	//

	function LoadingView(controller) {
		this.init(controller);
	}

	$.extend(LoadingView.prototype, {

		init : function(controller) {
			this.controller = controller;
			this.panel = $("<div>");
		},

		render : function() {
			return this.panel;
		},

		refresh : function() {
			this.panel.text(this.controller.isLoading() ? "Loading..." : "");
		}

	});

	return LoadingView;

});