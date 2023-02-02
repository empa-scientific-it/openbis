define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// CHANNEL STACK CHOOSER VIEW
	//

	function ChannelStackChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelStackChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>");
		},

		render : function() {
			this.panel.append(this.controller.getWidget().render());
			return this.panel;
		}

	});

	return ChannelStackChooserView;

});