define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// IMAGE PARAMETERS VIEW
	//

	function ImageParametersView(controller) {
		this.init(controller);
	}

	$.extend(ImageParametersView.prototype, AbstractView.prototype, {
		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("imageViewer");
		},

		render : function() {
			this.panel.append(this.controller.getChannelChooserWidget().render());
			this.panel.append(this.controller.getTransformationChooserWidget().render());
			this.panel.append(this.controller.getResolutionChooserWidget().render());
			this.panel.append(this.controller.getChannelStackChooserWidget().render());

			this.refresh();

			return this.panel;
		}

	});

	return ImageParametersView;

});